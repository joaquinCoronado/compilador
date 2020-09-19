package com.udg.analizadorLexico;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class AnalizadorLexico {
	private String entrada, cadena, token;
	private int puntero; //puntero que recorre cada caracter de la entrada
	private boolean error; //determina si encuentra un error en el archivo
	/*Arreglo que guarda cada una de las cadenas, cada elemento del arreglo
	 tiene una cadena y un token (valor o significado de la cadena)*/
	private ArrayList<Componente> listaComponentes = new ArrayList<Componente>();
	final private int ESTADO_ERROR=-1; //Estado que determina que una cadena es invalida
	final private int ACEPTACION=100; //Estado que determina que una cadena es valida

	//Constructor
	public AnalizadorLexico() {
		entrada="";
		puntero=0;
		error=false;
	}
	
	//Funcion que guarda toda la entrada en una variable
	void cargarArchivo(String nombreArchivo){
		Scanner archivoLeer;
		try{
			archivoLeer=new Scanner(new File(nombreArchivo));
			while(archivoLeer.hasNextLine()){
				entrada+=archivoLeer.nextLine();
				entrada+="\r\n";
			}
		}catch(FileNotFoundException e){
			e.printStackTrace(System.err);
		}
	}

	//Verifica si la cadena es una palabra del lenguaje
	boolean esPalabraReservada(String palabra){
		return palabra.equals("entero") || palabra.equals("real") || palabra.equals("imprime") || palabra.equals("mientras") || 
				palabra.equals("si") || palabra.equals("otro") || palabra.equals("hacer") || palabra.equals("sino") || 
				palabra.equals("osi") || palabra.equals("cadena") || palabra.equals("constante") || palabra.equals("caracter") ||
				palabra.equals("double") || palabra.equals("booleano") || palabra.equals("enum") || palabra.equals("define") ||
				palabra.equals("flotante");
	}
	
	//Verifica si es un operador logico
	boolean esOperadorLogico(String palabra){
		return palabra.equals("y") || palabra.equals("o") || palabra.equals("no");
	}

	//Verifica si el puntero llego al final de la entrada
	boolean esFinalArchivo(){
		return puntero>=entrada.length()-1;
	}

	//Funcion que revisa la entrada, si encuentra un error
	public boolean analizar(String nombreArchivo){
		cargarArchivo(nombreArchivo);
		int estado,longitud, estadoAnterior;
		char c;
		while(!esFinalArchivo() && !error){
			cadena="";
			token="desconocido";
			estado=estadoAnterior=0;
			longitud=entrada.length();
			while((estado!=ESTADO_ERROR) && (estado!=ACEPTACION) && (puntero!=longitud)){
				c=entrada.charAt(puntero++);
				estadoAnterior=estado; //Se el estado en la variable para poder asignar el token posteriormente
				switch(estado){
				case 0: //Estado inicial
					if(Character.isLetter(c)) estado=1;
					else if(Character.isDigit(c)) estado=2;
					else if(c == '_') estado=1;
					else if(c == '!') estado=10;
					else if(c == '<') estado=5;
					else if(c == '>') estado=8;
					else if(c == '=') estado=10;
					else if(c == '.') estado=13;
					else if(c == '+' || c == '-' || c == '*' || c == '/') estado=12;
					else if(c == '(' || c == ')' || c == ',' || c == ';' ||
							c == '{' || c == '}') estado=13;
					else if(c == ' ' || c == '\t' || c == '\n'|| c == '\r')	estado=0; 
					else estado=ESTADO_ERROR;
					break;
				case 1: //Identificador, palabra reservada u operador logico
					if(Character.isLetter(c)) estado=1;
					else if(Character.isDigit(c)) estado=1;
					else if(c == '_') estado=1;
					else if(c == '<' || c == '>' || c == '=' || c == '.' || c == '.' ||
							c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || 
							c == ')' || c == ',' || c == ';' || c == '{' || c == '}' || 
							c == ' ' || c == '\t' || c == '\n'|| c == '\r') estado=ACEPTACION;
					else estado=ESTADO_ERROR;
					break;
				case 2: //Numero entero
					if(Character.isDigit(c)) estado=2;
					else if(c=='.') estado=3;
					else if(c == '<' || c == '>' || c == '=' || Character.isLetter(c) || c == '.' ||
							c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == '_' ||
							c == ')' || c == ',' || c == ';' || c == '{' || c == '}' || 
							c == ' ' || c == '\t' || c == '\n'|| c == '\r') estado=ACEPTACION;
					else estado=ESTADO_ERROR;
					break;
				case 3:
					if(Character.isDigit(c)) estado=4;
					else estado=ESTADO_ERROR;
					break;
				case 4: //Numero real
					if(Character.isDigit(c)) estado=4;
					else if(c == '<' || c == '>' || c == '=' || Character.isLetter(c) || c == '.' ||
							c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == '_' ||
							c == ')' || c == ',' || c == ';' || c == '{' || c == '}' || c == '.' ||
							c == ' ' || c == '\t' || c == '\n'|| c == '\r') estado=ACEPTACION;
					else estado=ESTADO_ERROR;
					break;
				case 5:
					if(c=='>') estado=7;
					else if(c=='=') estado=6;
					else if(c == '<' || Character.isLetter(c) || c == '.' || Character.isDigit(c) ||
							c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == '_' ||
							c == ')' || c == ',' || c == ';' || c == '{' || c == '}' || c == '.' ||
							c == ' ' || c == '\t' || c == '\n'|| c == '\r') estado=ACEPTACION;
					else estado=ESTADO_ERROR;
					break;
				case 6:
					if(c == '<' || c == '>' || c == '=' || Character.isLetter(c) || c == '.' ||
					c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == '_' ||
					c == ')' || c == ',' || c == ';' || c == '{' || c == '}' || c == '.' ||
					c == ' ' || c == '\t' || c == '\n'|| c == '\r' || Character.isDigit(c) ) estado=ACEPTACION;
					else estado=ESTADO_ERROR;
					break;
				case 7:
					if(c == '<' || c == '>' || c == '=' || Character.isLetter(c) || c == '.' ||
					c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == '_' ||
					c == ')' || c == ',' || c == ';' || c == '{' || c == '}' || c == '.' ||
					c == ' ' || c == '\t' || c == '\n'|| c == '\r' || Character.isDigit(c) ) estado=ACEPTACION;
					else estado=ESTADO_ERROR;
					break;
				case 8:
					if(c=='=') estado=9;
					else if(c == '<' || c == '>' || Character.isLetter(c) || c == '.' ||
							c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == '_' ||
							c == ')' || c == ',' || c == ';' || c == '{' || c == '}' || c == '.' ||
							c == ' ' || c == '\t' || c == '\n'|| c == '\r' || Character.isDigit(c) ) estado=ACEPTACION;
					else estado=ESTADO_ERROR;
					break;
				case 9:
					if(c == '<' || c == '>' || c == '=' || Character.isLetter(c) || c == '.' ||
					c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == '_' ||
					c == ')' || c == ',' || c == ';' || c == '{' || c == '}' || c == '.' ||
					c == ' ' || c == '\t' || c == '\n'|| c == '\r' || Character.isDigit(c) ) estado=ACEPTACION;
					else estado=ESTADO_ERROR;
					break;
				case 10: //Operador relacional
					if(c=='=') estado=11;
					else if(c == '<' || c == '>' || Character.isLetter(c) || c == '.' ||
							c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == '_' ||
							c == ')' || c == ',' || c == ';' || c == '{' || c == '}' || c == '.' ||
							c == ' ' || c == '\t' || c == '\n'|| c == '\r' || Character.isDigit(c) ) estado=ACEPTACION;
					else estado=ESTADO_ERROR;
					break;
				case 11:
					if(c == '<' || c == '>' || c == '=' || Character.isLetter(c) || c == '.' ||
					c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == '_' ||
					c == ')' || c == ',' || c == ';' || c == '{' || c == '}' || c == '.' ||
					c == ' ' || c == '\t' || c == '\n'|| c == '\r' || Character.isDigit(c) ) estado=ACEPTACION;
					else estado=ESTADO_ERROR;
					break;
				case 12: //Operador aritmetico
					if(c == '<' || c == '>' || c == '=' || Character.isLetter(c) || c == '.' ||
					c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == '_' ||
					c == ')' || c == ',' || c == ';' || c == '{' || c == '}' || c == '.' ||
					c == ' ' || c == '\t' || c == '\n'|| c == '\r' || Character.isDigit(c) ) estado=ACEPTACION;
					else estado=ESTADO_ERROR;
					break;
				case 13: //Delimitador
					if(c == '<' || c == '>' || c == '=' || Character.isLetter(c) || c == '.' ||
					c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == '_' ||
					c == ')' || c == ',' || c == ';' || c == '{' || c == '}' || c == '.' ||
					c == ' ' || c == '\t' || c == '\n'|| c == '\r' || Character.isDigit(c) ) estado=ACEPTACION;
					else estado=ESTADO_ERROR;
					break;
				default:
					estado=ESTADO_ERROR;
				}
				if(estado!=ESTADO_ERROR && estado!=ACEPTACION && estado!=0){
					cadena+=c;
				}
				if(estado!=ESTADO_ERROR && estado!=ACEPTACION){
					estadoAnterior=estado;
				}
			}
			if(estado==ESTADO_ERROR||estado==ACEPTACION){
				puntero--;
				if(estado==ESTADO_ERROR){
					error=true;
				}
			}
			else{
				estadoAnterior=estado;
			}
			//Se asigna el token de la cadena
			switch(estadoAnterior){
			case 1:
				token="Identificador";
				if(esPalabraReservada(cadena)){
					token="PalabraReservada";
				}
				else if(esOperadorLogico(cadena)){
					token="OperadorLogico";
				}
				break;
			case 2:		
				token="NumeroEntero";
				break;
			case 4:		
				token="NumeroReal";
				break;
			case 5:	case 6:	case 7:	case 8:	
			case 9:	case 11:
				token="OperadorRelacional";				
				break;
			case 10:	
				token="OperadorAsignacion";
				break;
			case 12:	
				token="OperadorAritmetico";	
				break;
			case 13:	
				token="Delimitador";
				break;
			default:	
				token=" ";
			}
			Componente componente = new Componente(cadena, token);
			System.out.println("Cadena: "+cadena+" ->Token: "+token);
			listaComponentes.add(componente); //Guardo en el arreglo
		}
		/*En caso de encontrar un error manda el mensaje, caso contrario
		  procede al analizador sintactico*/
		if(error){
			System.out.println("Error encontrado en analizador lexico");
			return error;
		}
		else{
			AnalizadorSintactico sintactico = new AnalizadorSintactico(listaComponentes);
			return sintactico.analizar();
		}
	}
}