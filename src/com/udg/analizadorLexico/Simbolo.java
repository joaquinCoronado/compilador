package com.udg.analizadorLexico;

/**Cada simbolo ayuda en el analizador semantico
 para comprobar operaciones entre variables del
 mismo tipo**/

public class Simbolo {
	
	String nombre,tipo;

	public Simbolo(String nombre, String tipo) {
		this.nombre=nombre;
		this.tipo=tipo;
	}

}
