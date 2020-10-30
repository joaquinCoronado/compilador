package main.java.com.udg.analizadorLexico;

public class GeneradorXml {
	private String xml; //codigo
	private Nodo raiz = new Nodo(null, null, new Componente("PROGRAMA" ,"PROGRAMA"),true); //Raiz proveniente del AS, es el punto de partida para recorrer el arbol y generar el codigo
	
	//Constructor
	public GeneradorXml(Nodo raiz){
		xml="";
		this.raiz=raiz;
	}
	
	
	public void guardaXml(){
		xml+=generaXml(raiz);
		System.out.println(xml);
	}
	
	//Funcion que genera el codigo, se genera en base al token de cada nodo del arbol
	public String generaXml(Nodo nodoActual){
		String codigo, cadena, token;
		int i;
		codigo="";
		if(nodoActual!=null){
			cadena=nodoActual.componente.cadena;
			token=nodoActual.componente.token.toUpperCase();
			if(!token.equals("EXPRESION") && !token.equals("MULT") && !token.equals("SIGNO") && !token.equals("SUMA")){
				codigo+="<"+token+">";
			}
			if(!token.equals("ID") && !token.equals("REAL") && !token.equals("ENTERO") && !token.equals("TIPO") && !token.equals("EXPRESION")
					&& !token.equals("MULT") && !token.equals("SIGNO") && !token.equals("SUMA")){
				codigo+="\r\n";
			}
			if(token.equals("PROGRAMA")){
				for(i=0; i<nodoActual.hijos.size(); i++){
					codigo+=generaXml(nodoActual.hijos.get(i));
				}
			}
			else if(token.equals("ASIGNACION")){
				codigo+=generaXml(nodoActual.izquierda);
				codigo+=generaXml(nodoActual.derecha);
			}
			else if(token.equals("DEFVAR")){
				codigo+="<TIPO>"+nodoActual.hijos.get(0).componente.token.toLowerCase()+"</TIPO>\r\n";
				for(i=0; i<nodoActual.hijos.size(); i++){
					codigo+="<ID>"+nodoActual.hijos.get(i).componente.cadena+"</ID>\r\n";
				}
			}
			else if(token.equals("MIENTRAS") || token.equals("SI")){
				codigo+=generaXml(nodoActual.izquierda);
				codigo+="<BLOQUE>\r\n";
				for(i=0; i<nodoActual.hijos.size(); i++){
					codigo+=generaXml(nodoActual.hijos.get(i));
				}
				codigo+="</BLOQUE>\r\n";
				codigo+=generaXml(nodoActual.derecha);
			}
			else if(token.equals("OTRO")){
				for(i=0; i<nodoActual.hijos.size(); i++){
					codigo+=generaXml(nodoActual.hijos.get(i));
				}
			}
			else if(token.equals("SUMA") || token.equals("MULT") || token.equals("SIGNO")||
					token.equals("EXPRESION") || token.equals("LOGICO")){
				if(cadena.equals(">")){
					cadena="&gt;";
				}
				else if(cadena.equals("<")){
					cadena="&lt;";
				}
				else if(cadena.contains(">")){
					cadena="&gt;"+cadena.charAt(cadena.length()-1);
				}
				else if(cadena.contains("<")){
					cadena="&lt;"+cadena.charAt(cadena.length()-1);
				}
				codigo+="<"+token+" value=\""+cadena+"\">\r\n";
				codigo+=generaXml(nodoActual.izquierda);
				codigo+=generaXml(nodoActual.derecha);
			}
			else if(token.equals("IMPRIME")){
				codigo+="<EXPRESION>\r\n";
				codigo+=generaXml(nodoActual.izquierda);
				codigo+="</EXPRESION>\r\n";
			}
			else if(nodoActual.hijos.size()>0){
				for(i=0; i<nodoActual.hijos.size(); i++){
					codigo+=generaXml(nodoActual.hijos.get(i));
				}
			}
			else{
				codigo+=cadena;
				codigo+=generaXml(nodoActual.izquierda);
				codigo+=generaXml(nodoActual.derecha);
			}
			codigo+="</"+token+">"+"\r\n";
		}
		return codigo;
	}
}