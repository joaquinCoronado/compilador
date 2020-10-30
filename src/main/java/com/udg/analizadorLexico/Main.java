package main.java.com.udg.analizadorLexico;

/**	Juan Manuel Banda Avalos - 211674941
	Traductores de Lenguajes 2**/

public class Main {

	public static void main(String[] args) {
		AnalizadorLexico lexico = new AnalizadorLexico();
		if(!lexico.analizar("entrada.txt")){
			//System.out.println("Archivo analizado correctamente");
		}
		else{
			//System.out.println("Archivo invalido");
		}
	}
}