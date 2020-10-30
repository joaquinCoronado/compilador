package main.java.com.udg.analizadorLexico;

import java.util.ArrayList;
//Clase que representa cada nodo del arbol
public class Nodo {
	ArrayList<Nodo>	hijos = new ArrayList<>();
	Nodo izquierda,derecha;
	Componente componente;
	boolean esRaiz;
	
	public Nodo(Nodo izquierda, Nodo derecha, Componente componente, boolean esRaiz){
		this.izquierda=izquierda;
		this.derecha=derecha;
		this.componente=componente;
		this.esRaiz=esRaiz;
	}
	
	//Se agregan hijos al nodo
	public void agregaNodo(Nodo nodo){
		if(nodo!=null){
			hijos.add(nodo);
		}
	}

	@Override
	public String toString() {
		return "Nodo{" +
				"componente=" + componente +
				'}';
	}
}
