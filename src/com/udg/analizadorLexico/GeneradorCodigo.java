package com.udg.analizadorLexico;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class GeneradorCodigo {
	private Map<String,Simbolo> simbolos = new HashMap<String,Simbolo>(); //Proveniente del AS, se usa en la declaracion de variables del asm
	private Nodo raiz = new Nodo(null, null, new Componente("PROGRAMA" ,"PROGRAMA"),true); //Raiz proveniente del AS, es el punto de partida para recorrer el arbol y generar el codigo
	private int numEtiqueta, numEtiquetaVerdadero, numEtiquetaFin; //Contadores de etiquetas (evitar que se repitan)
	private String codigo; //Codigo que se genera y se guardara en el .asm
	
	//Constructor
	public GeneradorCodigo(Nodo raiz, Map<String,Simbolo> simbolos){
		numEtiqueta=numEtiquetaVerdadero=numEtiquetaFin=0;
		this.raiz=raiz;
		this.simbolos=simbolos;
	}
	
	//Funcion que se encarga de generar el codigo de la declaracion de variables
	String codigoVariables(){
		String codigo="";
		for(@SuppressWarnings("rawtypes") Map.Entry m:simbolos.entrySet()){  
	    	 codigo+="_"+m.getKey()+" dword 0"+"\r\n"; //Se agrega _ al nombre de las variables para evitar problemas
	    }
		return codigo;
	}
	
	//Funcion que se encarga de generar todo el codigo del asm
	String getCodigo(){
		String codigo="";
		codigo+=".386"+"\r\n";
		codigo+=".model flat, stdcall"+"\r\n";
		codigo+="option casemap:none"+"\r\n";
		codigo+="include \\masm32\\macros\\macros.asm"+"\r\n";
		codigo+="include \\masm32\\include\\masm32.inc"+"\r\n";
		codigo+="include \\masm32\\include\\kernel32.inc"+"\r\n";
		codigo+="include \\masm32\\macros\\macros.asm"+"\r\n";
		codigo+="includelib \\masm32\\lib\\masm32.lib"+"\r\n";
		codigo+="includelib \\masm32\\lib\\kernel32.lib"+"\r\n"+"\r\n";
		codigo+=".data"+"\r\n";
		codigo+=codigoVariables(); //Declaracion de variables
		codigo+=".code"+"\r\n";
		codigo+="inicio:"+"\r\n";
		codigo+=codigoNodo(raiz); //Codigo principal
		codigo+="exit"+"\r\n";
		codigo+="end inicio"+"\r\n";
	    return codigo;
	}
	
	//regresa la siguiente etiqueta, usada en ciclos y validaciones
	String getSiguienteEtiqueta(){
		return "E"+(numEtiqueta++);
	}
	
	//Regresa la siguiente etiqueta de verdaderi
	String getEtiquetaVerdadero(){
		return "V"+(numEtiquetaVerdadero++);
	}
	
	//Regresa la siguiente etiqueta de fin
	String getEtiquetaFin(){
		return "F"+(numEtiquetaFin++);
	}
	
	//Regresa el tipo de salto dependiendo del operador
	String getSalto(String operadorLogico){
		if(operadorLogico.equals("<")){
			return "jl";
		}
		if(operadorLogico.equals("<=")){
			return "jle";
		}
		if(operadorLogico.equals(">")){
			return "jg";
		}
		if(operadorLogico.equals(">=")){
			return "jge";
		}
		if(operadorLogico.equals("==")){
			return "je";
		}
		if(operadorLogico.equals("<>")){
			return "jne";
		}
		return "-1";
	}
	
	/*Funcion recursiva que va recorriendo el arbol para generar el
	  codigo principal, dependiendo del nodo actual y del tipo de nodo que sea*/
	String codigoNodo(Nodo nodo){
		String codigo, inicio, fin, cadena, tipo;
		codigo="";
		int i,j;
		if(nodo!=null){
			cadena=nodo.componente.cadena;
			tipo=nodo.componente.token;
			if(cadena.equals("PROGRAMA") || cadena.equals("OTRO")){
				for(i=0,j=nodo.hijos.size();i<j;i++){
					codigo+=codigoNodo(nodo.hijos.get(i));
				}
			}
			else if(tipo.equals("SUMA") || tipo.equals("MULT")){
				codigo+=codigoNodo(nodo.izquierda);
				codigo+=codigoNodo(nodo.derecha);
				if(cadena.equals("+")){
					codigo+="pop    ebx"+"\r\n"+"pop    eax"+"\r\n"+"add    eax, ebx"+"\r\n"+
							"push   eax"+"\r\n";
				}
				else if(cadena.equals("-")){
					codigo+="pop    ebx"+"\r\n"+"pop    eax"+"\r\n"+"sub    eax, ebx"+"\r\n"+
							"push   eax"+"\r\n";
				}
				else if(cadena.equals("*")){
					codigo+="pop    ebx"+"\r\n"+"pop    eax"+"\r\n"+"mul ebx"+"\r\n"+
							"push   eax"+"\r\n";
				}
				else if(cadena.equals("/")){
					codigo+="pop    ebx"+"\r\n"+"pop    eax"+"\r\n"+"div ebx"+"\r\n"+
							"push   eax"+"\r\n";
				}
			}
			else if(tipo.equals("ID") || tipo.equals("REAL") || tipo.equals("ENTERO")){
				if(tipo.equals("ID")){
					codigo+="push _"+nodo.componente.cadena+"\r\n";
				}
				else{
					codigo+="push "+nodo.componente.cadena+"\r\n";
				}
			}
			else if(tipo.equals("SIGNO")){
				if(cadena.equals("-")){
					codigo+=codigoNodo(nodo.izquierda);
					codigo+="pop eax"+"\r\n"+"mul -1"+"\r\n"+"push eax"+"\r\n";
				}
			}
			else if(tipo.equals("IMPRIME")){
				codigo+=codigoNodo(nodo.izquierda);
				codigo+="pop eax"+"\r\n"+"print str$(eax)"+"\r\n"+"print chr$(10)"+"\r\n";
			}
			else if(tipo.equals("ASIGNACION")){
				codigo+=codigoNodo(nodo.derecha);
				codigo+="pop _"+nodo.izquierda.componente.cadena+"\r\n";
			}
			else if(tipo.equals("LOGICO")){
				codigo+=codigoNodo(nodo.izquierda);
				codigo+=codigoNodo(nodo.derecha);
				if(cadena.equals("y")){
					codigo+="pop    ebx"+"\r\n"+"pop    eax"+"\r\n"+"and eax,ebx"+"\r\n"+
							"push   eax"+"\r\n";
				}else if(cadena.equals("o")){
					codigo+="pop    ebx"+"\r\n"+"pop    eax"+"\r\n"+"or eax,ebx"+"\r\n"+
							"push   eax"+"\r\n";
				}else if(cadena.equals("no")){
					codigo+="pop    eax"+"\r\n"+"not    eax"+"\r\n"+"push eax";
				}
			}
			else if(tipo.equals("EXPRESION")){
				String etVerdadero, etFin;
				codigo+=codigoNodo(nodo.izquierda);
				codigo+=codigoNodo(nodo.derecha);
				etVerdadero=getEtiquetaVerdadero();
				etFin=getEtiquetaFin();
				codigo+="pop    ebx"+"\r\n"+"pop    eax"+"\r\n"+"cmp eax,ebx"+"\r\n"+
						getSalto(cadena)+" "+etVerdadero+"\r\n"+
						"push 0"+"\r\n"+
						"jmp "+etFin+"\r\n"+
						etVerdadero+":"+"\r\n"+
						"push 1"+"\r\n"+
						etFin+":"+"\r\n";
			}
			else if(tipo.equals("MIENTRAS")){
				inicio=getSiguienteEtiqueta();
				fin=getSiguienteEtiqueta();
				codigo+=inicio+":"+"\r\n";
				codigo+=codigoNodo(nodo.izquierda);
				codigo+="pop eax"+"\r\n";
				codigo+="cmp eax,1"+"\r\n";
				codigo+="jne "+fin+"\r\n";
				for(i=0,j=nodo.hijos.size();i<j;i++){
					codigo+=codigoNodo(nodo.hijos.get(i));
				}
				codigo+="jmp "+inicio+"\r\n";
				codigo+=fin+":"+"\r\n";
			}
			else if(tipo.equals("SI")){
				String otro;
				inicio=getSiguienteEtiqueta();
				fin=getSiguienteEtiqueta();
				otro=getSiguienteEtiqueta();
				codigo+=codigoNodo(nodo.izquierda);
				codigo+="pop eax"+"\r\n";
				codigo+="cmp eax,1"+"\r\n";
				codigo+="jne "+otro+"\r\n";
				for(i=0,j=nodo.hijos.size();i<j;i++){
					codigo+=codigoNodo(nodo.hijos.get(i));
				}
				codigo+="jmp "+fin+"\r\n";
				codigo+=otro+":"+"\r\n";
				codigo+=codigoNodo(nodo.derecha);
				codigo+=fin+":"+"\r\n";
			}
		}
		return codigo;
	}
	
	//Funcion que genera el archivo asm
	public void generarCodigo() throws IOException{
		codigo=getCodigo();
		try{
			PrintWriter escribe=new PrintWriter(new FileWriter("salida.asm"));
			escribe.print(codigo);
			escribe.close();
		}catch(IOException e){
			e.printStackTrace(System.err);
		}
		@SuppressWarnings("unused")
		//Ensamblamos el archivo asm generado, y creamos el ejecutable
		Process process;
		Runtime runtime = Runtime.getRuntime();
		process = runtime.exec("\\masm32\\bin\\ml /c  /coff  /Cp salida.asm");
		esperarSegundo(); //Pausa para esperar a que se genere el archivo salida.obj necesario para la siguiente instruccion
		process = runtime.exec("\\masm32\\bin\\link /SUBSYSTEM:CONSOLE /LIBPATH:.\\lib salida");
		esperarSegundo();
		process = runtime.exec("salida");
	}
	
	//Pausa de un segundo
	public static void esperarSegundo(){ 
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
}