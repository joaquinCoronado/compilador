package main.java.com.udg.analizadorLexico;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/*El analizador semantico esta en esta clase, junto al analizador sintactico*/

public class AnalizadorSintactico {
	private Nodo raiz = new Nodo(null, null, new Componente("PROGRAMA", "PROGRAMA"), true); //raiz del arbol
	private ArrayList<Componente> listaComponentes = new ArrayList<Componente>(); //Arreglo que proviene del analizador lexico
	/*Un map nos permite representar una estructura de datos para�almacenar 
	  pares "clave/valor"; de tal manera que para una clave solamente tenemos un valor.
	  el hashmap simbolos ayuda sobre todo en el analisis a las variables,
	  el poder comprobar si una variable ya ha sido declarada, o no, o si esta duplicada */
	Map<String, Simbolo> simbolos=new HashMap<>();
	Stack<String> tipos=new Stack<>(); //Pila que ayuda con la comprobacion de operaciones del mismo tipo
	private boolean error; //Determina si encuentra un error en el archivo
	private int puntero; //Puntero que RECORRE EL ARREGLO de compontentes, no la entrada
	private String cadena, tipo;
	
	//constructor
	public AnalizadorSintactico(ArrayList<Componente> listaComponentes){
		this.listaComponentes=listaComponentes;
		error=false;
		puntero=0;
	}
	
	//Regresa la siguiente cadena del arreglo
	String getSiguienteCadena(){
		if(puntero<listaComponentes.size()){
			return listaComponentes.get(puntero++).cadena;
		}
		return null;
	}
	
	//Se obtiene el componente actual
	Componente getComponenteActual(int puntero){
		return listaComponentes.get(puntero-1);
	}
	
	//Verifica si es un operador relacional
	boolean esOperadorRelacional(String palabra){
		return palabra.equals("<") || palabra.equals(">") || palabra.equals("<=") || palabra.equals(">=") ||
				 palabra.equals("==") || palabra.equals("<>") || palabra.equals("!=");
	}
	
	//Verifica si la cadena es un tipo de variable (entero/real)
	boolean esTipoNumerico(String palabra){
		return  palabra.equals("entero") || palabra.equals("real");
	}
	
	//Regresa el token del componente actual (en la posicion-1 puesto que el puntero avanza
	String getToken(){
		return listaComponentes.get(puntero-1).token;
	}
	
	/*Esta funcion regresa un nodo dependiendo de la cadena actual.
	  Cada uno de estos nodos tiene sus propios hijos ya asignados*/
	Nodo getNodo(){
		if(error){
			return null;
		}
		Nodo variables, nodo1, nodo2;
		/*Si la cadena es la palabra reservada entero/real, entra.
		  Esta parte de la funcion se encarga de crear un nodo
		  de variables, que como su nombre lo dice, tendra los nodos
		  de cada una de las variables declaradas*/
		if(esTipoNumerico(cadena)){
			tipo=cadena;
			variables=nodoVariables();
			if(cadena.equals(";")){
				if(cadena.equals(";")){
					cadena=getSiguienteCadena();
				}
				return variables;
			}
			else{
				System.out.println("->Error: falta ; en "+cadena);
				error=true;
				return null;
			}
		}
		
		/*Esta parte de la funcion se encarga de la asignacion de 
		  valores a las variables declaradas, donde dicho valor puede
		  ser un entero, real, u operaciones con otras variables,
		  por ello involucra funciones declaradas mas abajo*/
		else if(getToken().equals("Identificador")){
			String cadena1, cadena2;
			Simbolo simbolo;
			cadena1=cadena;
			cadena=getSiguienteCadena();
			cadena2=cadena;
			simbolo=simbolos.get(cadena1); //Se revisa si la variable ha sido declarada anteriormente
			if(simbolo==null){
				//Si no se marca un error
				System.out.println("->Error: "+cadena1+" no ha sido declarado");
				error=true;
				return null;
			}
			tipos.push(simbolo.tipo);
			nodo1=new Nodo(new Nodo(null, null, new Componente(cadena1, "ID"), false), null, new Componente(cadena2, "ASIGNACION"), false);
			if(cadena.equals("=")){
				cadena=getSiguienteCadena();
				nodo2=nodoExpresionAritmetica();
				if(cadena.equals(";")){
					if(cadena.equals(";"))
					cadena=getSiguienteCadena();
					nodo1.derecha=nodo2;
					return nodo1;
				}
				else{
					System.out.println("->Error: falta ; en "+cadena);
					error=true;
					return null;
				}
			}
		}
		
		//Regresa el nodo mientras, la funcion esta mas abajo
		else if(cadena.equals("mientras")){
			return nodoMientras();
		}
		
		//Regresa el nodo si, la funcion esta mas abajo
		else if(cadena.equals("si")){
			return nodoSi();
		}
		
		//Regresa el nodo imprime, la funcion esta mas abajo
		else if(cadena.equals("imprime")){
			nodo1=nodoImprime();
			if(cadena.equals(";")){
				if(cadena.equals(";"))
				cadena=getSiguienteCadena();
				return nodo1;
			}
			else{
				System.out.println("->Error: falta ; o )");
				error=true;
				return null;
			}
		}
		else{
			error=true;
			System.out.println("->Error: "+cadena);
		}
		return null;
	}
	
	/*Esta funcion se encarga de crear un nodo
	  de variables, que como su nombre lo dice, tendra los nodos
	  de cada una de las variables declaradas*/
	Nodo nodoVariables(){
		String id, tipoVariable;
		Nodo nodo=new Nodo(null, null, new Componente("DEFVAR","DEFVAR"), false);

		if(cadena != null){
			while(!cadena.equals(";")){
				cadena=getSiguienteCadena(); //Identificador
				id=cadena;
				if(getToken().equals("Identificador")){
					cadena=getSiguienteCadena();
					if(cadena.equals(",") || cadena.equals(";")){
						nodo.hijos.add(new Nodo(null, null, new Componente(id,tipo), false));
						if(tipo.equals("entero")){
							tipoVariable="E";
						}
						else{
							tipoVariable="R";
						}

						//Si la variable no ha sido declarada se agrega a simbolos:
						if(simbolos.get(id)==null){

							///AQUI ME QUEDE..
							simbolos.put(id, new Simbolo(id,tipoVariable));
						}
						//De lo contrario mostramos el error
						else{
							error=true;
							System.out.println("->Error: identificador "+simbolos.get(id).nombre+" ya ha sido declarado");
						}
						continue;
					}
					else{
						System.out.println("->Error: hace falta , o ; en "+cadena);
						error=true;
						break;
					}
				}
				else{
					System.out.println("->Error: "+cadena+" no es un identificador");
					error=true;
				}
			}
		}


		if(!error){
			return nodo;
		}
		return null;
	}
	
	
	//Esta funcion se encarga de regresar el tipo de resultado de la operacion.
	public String getTipo(String tp){
		if(tp.equals("E=E") || tp.equals("R=R") || tp.equals("L=L") || tp.equals("R=E")){
			return "I"; //Indefinido
		}
		else if(tp.equals("E+E") || tp.equals("E-E") || tp.equals("E*E") || tp.equals("-E")){
			return "E"; //Entero
		}
		else if(tp.equals("E+R") || tp.equals("R+E") || tp.equals("R+R") || tp.equals("E-R") ||
				tp.equals("R-E") || tp.equals("R-R") || tp.equals("E*R") || tp.equals("R*E") ||
				tp.equals("R*R") || tp.equals("E/E") || tp.equals("E/R") || tp.equals("R/E") ||
				tp.equals("R/R") || tp.equals("-R")){
			return "R"; //Real
		}
		else if(tp.equals("LyL") || tp.equals("LoL") || tp.equals("noL") || tp.equals("E>E") ||
				tp.equals("R>E") || tp.equals("E>R") || tp.equals("R>R") || tp.equals("E<E") ||
				tp.equals("R<E") || tp.equals("E<R") || tp.equals("R<R") || tp.equals("E>=E") ||
				tp.equals("R>=E") || tp.equals("E>=R") || tp.equals("R>=R") || tp.equals("E<=E") ||
				tp.equals("R<=E") || tp.equals("E<=R") || tp.equals("R<=R") || tp.equals("E<>E") ||
				tp.equals("R<>E") || tp.equals("E<>R") || tp.equals("R<>R") || tp.equals("E==E") ||
				tp.equals("R==E") || tp.equals("E==R") || tp.equals("R==R")){
			return "L"; //Logico
		}
		else{
			return "-1";
		}
	}
	
	/*Este nodo, como su nombre lo dice, almacena una expresion
	  aritmetica, puede contener multiplicaciones, divisiones, sumas
	  restas o ser solo una asignacion de un valor entero/real a una variable*/
	Nodo nodoExpresionAritmetica(){
		Nodo nodo;
		String aux, op, tp, tp1, encontrado;
		if(!error){
			nodo=nodoMultiplicacion();
			while(cadena.equals("+") || cadena.equals("-")){ //Si la operacion involucra sumas o restas
				op=cadena;
				aux=cadena;
				cadena=getSiguienteCadena();
				nodo=new Nodo(nodo, nodoMultiplicacion(), new Componente(aux,"SUMA"), false);
				tp=tipos.pop();
				tp1=tipos.pop();
				tp=tp1+op+tp;
				encontrado=getTipo(tp);
				if(!encontrado.equals("-1")){ //Si las variables con las que se quiere hacer la operacion son del mismo tipo
					tipos.push(getTipo(tp));
				}
				else{
					error=true;
					System.out.println("->Error: los tipos no coinciden "+op+" <"+tp+">");
					tipos.push("I");
				}
			}
			return nodo; //O se puede regresar unicamente el nodo obtenido de nodoMultiplicacion, ya que puede tratarse de solo una asignacion
		}else{
			return null;
		}
	}
	
	/*Este nodo regresa un nodo de multiplicacion SIEMPRE Y CUANDO se trate 
	  de una multiplicacion o de una division, de lo contrario regresa solo el
	  nodo de la asignacion*/
	Nodo nodoMultiplicacion(){
		Nodo nodo;
		String aux,op,tp,tp1, correcto;
		if(!error){
			nodo=nodoVariable();
			while(cadena.equals("*") || cadena.equals("/")){
				op=cadena;
				aux=cadena;
				cadena=getSiguienteCadena();
				nodo=new Nodo(nodo,nodoVariable(),new Componente(aux,"MULT"), false);///Futuro parametro
				tp=tipos.pop();
				tp1=tipos.pop();
				tp=tp1+op+tp;
				correcto=getTipo(tp);
				if(!correcto.equals("-1")){//Si las variables con las que se quiere hacer la operacion son del mismo tipo
					tipos.push(correcto);
				}
				else{
					error=true;
					System.out.println("->Error: los tipos no coinciden "+op+" <"+tp+">");
					tipos.push("I");
				}
			}
			return nodo; //O se puede regresar unicamente el nodo obtenido de nodoMultiplicacion, ya que puede tratarse de solo una asignacion
		}
		else{
			return null;
		}
	}
	
	/*Este nodo */
	Nodo nodoVariable(){
		Nodo nodo1,nodo2;
		String op,tp, correcto;
		if(!error){
			if(cadena.equals("+") || cadena.equals("-")){
				op=cadena;
				nodo1=nodoSigno();
				nodo2=nodoVariable();
				tp=tipos.pop();
				tp=op+tp;
				correcto=getTipo(tp);
				if(!correcto.equals("-1")){
					tipos.push(correcto);
				}
				else{
					error=true;
					System.out.println("->Error: los tipos no coinciden "+op+" <"+tp+">");
					tipos.push("I");
				}
				nodo1.izquierda=nodo2;
				return nodo1;
			}
			else{
				return nodoFin();
			}
		}else{
			return null;
		}
	}
	
	/*Se encarga de regresar un nodo, esta funcion se utiliza cuando
	  se le asignara valor a alguna variable, que puede ser un valor
	  entero, real o un valor proveniente de otra variable*/
	Nodo nodoFin(){
		Nodo nodo,aux;
		String token=getToken();
		String cadenaAux, tokenAux="";
		Simbolo simbolo;
		if(!error){
			if(token.equals("NumeroEntero") || token.equals("NumeroReal") || token.equals("Identificador")){
				switch(token){
					case "NumeroEntero":	
						tokenAux="ENTERO";	
						break;
					case "NumeroReal":	
						tokenAux="REAL";	
						break;
					case "Identificador":		
						tokenAux="ID";		
						break;
				}
				if(token.equals("Identificador")){
					simbolo=simbolos.get(cadena);
					if(simbolo!=null){
						tipos.push(simbolo.tipo);
					}
					else{
						error=true;
						tipos.push("I");
						System.out.println("->Error: "+cadena+" no ha sido declarado");
					}
				}
				else if(token.equals("NumeroEntero")){
					tipos.push("E");
				}
				else if(token.equals("NumeroReal")){
					tipos.push("R");
				}
				cadenaAux=cadena;
				nodo=new Nodo(null,null,new Componente(cadenaAux,tokenAux), false);
				cadena=getSiguienteCadena();
				return nodo;
			}
			else if(cadena.equals("(")){
				cadena=getSiguienteCadena();
				aux=nodoExpresion();
				if(cadena.equals(")")){
					cadena=getSiguienteCadena();
					return aux;
				}
				else{
					error=true;
					cadena=getSiguienteCadena();
					System.out.println("->Error: "+cadena);
					return null;
				}
			}
			else{
				System.out.println("->Error: "+cadena);
				return null;
			}
		}
		else{
			return null;
		}
	}
	
	/*Este arreglo es necesario para crear los nodos del codigo
	  que puede estar dentro de un mientras, un si o un otro*/
	ArrayList<Nodo> bloque(){
		ArrayList<Nodo> nodos=new ArrayList<Nodo>();
		while(!cadena.equals("}") && !error){
			nodos.add(getNodo());
		}
		if(cadena.equals("}")){
			cadena=getSiguienteCadena();
			return nodos;
		}else{
			error=true;
			System.out.println("->Error: falta } en "+cadena);
			cadena=getSiguienteCadena();
			return null;
		}
	}
	
	/*Regresa un nodo mientras (ciclo), el cual tiene un bloque de codigo, el bloque
	  como se puede apreciar en su funcion, es un arreglo de nodos*/
	Nodo nodoMientras(){
		Nodo nodo;
		String tp;
		nodo=new Nodo(null,null,new Componente("MIENTRAS","MIENTRAS"), false);
		cadena=getSiguienteCadena();
		if(cadena.equals("(")){
			nodo.izquierda=nodoExpresion();
			tp=tipos.pop();
			if(tp.equals("I")){
				error=true;
				System.out.println(tp);
				System.out.println("->Error: se necesita expresion logica");
			}
			if(cadena.equals("{")){
				cadena=getSiguienteCadena();
				nodo.hijos=bloque();
			}
			else{
				nodo.hijos.add(getNodo());
			}
			return nodo;
		}
		else{
			error=true;
			cadena=getSiguienteCadena();
			System.out.println("->Error: falta ( en mientras "+cadena);
			return null;
		}
	}
	
	/*Regresa un nodo si, el cual tiene un bloque de codigo, el bloque
	  como se puede apreciar en su funcion, es un arreglo de nodos*/
	Nodo nodoSi(){
		Nodo nodo;
		String tp;
		nodo=new Nodo(null,null,new Componente("SI","SI"), false);
		cadena=getSiguienteCadena();
		if(cadena.equals("(")){
			nodo.izquierda=nodoExpresion();
			tp=tipos.pop();
			if(tp.equals("I")){
				error=true;
				System.out.println(tp);
				System.out.println("->Error: se necesita expresion logica");
			}
			if(cadena.equals("{")){
				cadena=getSiguienteCadena();
				nodo.hijos=bloque();
			}
			else{
				nodo.hijos.add(getNodo());
			}
			nodo.derecha=nodoOtro();
			return nodo;
		}else{
			error=true;
			System.out.println("->Error: falta ( en si "+cadena);
			cadena=getSiguienteCadena();
			return null;
		}
	}
	
	/*Regresa un nodo otro, el cual tiene un bloque de codigo, el bloque
	  como se puede apreciar en su funcion, es un arreglo de nodos*/
	Nodo nodoOtro(){
		if(cadena.equals("otro")){
			Nodo nodo=new Nodo(null,null,new Componente("OTRO","OTRO"), false);
			cadena=getSiguienteCadena();
			if(cadena.equals("{")){
				cadena=getSiguienteCadena();
				nodo.hijos=bloque();
			}
			else{
				nodo.hijos.add(getNodo());
			}
			return nodo;
		}else return null;
	}
	
	/*Regresa un nodo imprime, junto con la expresion correspondiente*/
	Nodo nodoImprime(){
		Nodo nodo;
		nodo=new Nodo(null,null,new Componente("IMPRIME","IMPRIME"), false);
		if(!error){
			cadena=getSiguienteCadena();
			if(cadena.equals("(")){
				nodo.izquierda=nodoExpresion();
				return nodo;
			}
			else{
				error=true;
				System.out.println("->Error: falta ( en imprime "+cadena);
				cadena=getSiguienteCadena();
				return null;
			}
		}
		else{
			return null;
		}
	}
	
	//Regresa un nodo expresion
	Nodo nodoExpresion(){
		Componente componenteAux;
		Nodo nodo;
		if(!error){
			nodo=nodoY();
			while(cadena.equals("o")){
				componenteAux=getComponenteActual(puntero);
				cadena=getSiguienteCadena();
				nodo=new Nodo(nodo,nodoY(),new Componente(componenteAux.cadena,"LOGICO"), false);
			}
			return nodo;
		}
		else{
			return null;
		}
	}
	
	//Regresa un nodo y
	Nodo nodoY(){
		Componente componenteAux;
		Nodo nodo;
		if(!error){
			nodo=nodoNo();
			while(cadena.equals("y")){
				componenteAux=getComponenteActual(puntero);
				cadena=getSiguienteCadena();
				nodo=new Nodo(nodo,nodoNo(),new Componente(componenteAux.cadena,"LOGICO"), false);
			}
			return nodo;
		}else{
			return null;
		}
	}
	
	//Regresa un nodo no
	Nodo nodoNo(){
		Nodo n1,n2;
		if(!error){
			if(cadena.equals("no")){
				n1=nodoNegacion();
				n2=nodoRelacional();
				n1.izquierda=n2;
				return n1;
			}
			else{
				return nodoRelacional();
			}
		}else{
			return null;
		}
	}
	
	//Regresa un nodo negacion
	Nodo nodoNegacion(){
		Nodo nodo;
		if(!error){
			if(cadena.equals("no")){
				nodo=new Nodo(null,null,new Componente(cadena,"LOGICO"), false);
				cadena=getSiguienteCadena();
				return nodo;
			}
			return null;
		}else{
			return null;
		}
	}
	
	//Regresa un nodo relacional, que tener una expresion aritmetica
	Nodo nodoRelacional(){
		Nodo nodo;
		String aux,op,tp,tp1, correcto;
		if(!error){
			nodo=nodoExpresionAritmetica();
			while(esOperadorRelacional(cadena)){
				op=cadena;
				aux=cadena;
				cadena=getSiguienteCadena();
				nodo=new Nodo(nodo,nodoExpresionAritmetica(),new Componente(aux,"EXPRESION"), false);
				tp=tipos.pop();
				tp1=tipos.pop();
				tp=tp1+op+tp;
				correcto=getTipo(tp);
				if(!correcto.equals("-1")){
					tipos.push(correcto);
				}
				else{
					error=true;
					System.out.println("->Error: los tipos no coinciden "+op+" <"+tp+">");
					tipos.push("I");
				}
			}
			return nodo;
		}else{
			return null;
		}
	}
	
	//Regresa un nodo signo
	Nodo nodoSigno(){
		Nodo nodo;
		if(!error){
			if(cadena.equals("+") || cadena.equals("-")){
				nodo=new Nodo(null,null,new Componente(cadena,"SIGNO"), false);
				cadena=getSiguienteCadena();
				return nodo;
			}
			return null;
		}
		else{
			return null;
		}
	}
	
	public boolean analizar(){
		if(listaComponentes.size()>0){ //Se toma la primer cadena
			cadena=getSiguienteCadena();
		}
		while(puntero<listaComponentes.size()-1 && !error){ //Se van agregando nodos al arbol
			raiz.agregaNodo(getNodo());
		}
		if(!error){
			System.out.println("\nAnálizis sintáctico finalizado con éxito!!");
			/*GeneradorCodigo generadorCodigo = new GeneradorCodigo(raiz, simbolos);
			try {
				generadorCodigo.generarCodigo();
			} catch (IOException e) {
				e.printStackTrace();
			}*/
		}
		else{
			System.out.println("Se encontraron errores");
		}

		System.out.println("raiz: " + new Gson().toJson(raiz));
		//System.out.println("resultado: " + new Gson().toJson(listaComponentes));
		return error;
	}
}