package main.java.com.udg.analizadorLexico;

/*Esta clase representa una cadena, con un token (significado
  o representacion de la cadena).
  Ej: Cadena: mientras
  	  Token: PalabraReservada*/
public class Componente{
	
	String cadena, token;
	public Componente(String cadena, String token){
		this.cadena=cadena;
		this.token=token;
	}

	@Override
	public String toString() {
		return "Componente{" +
				"cadena='" + cadena + '\'' +
				", token='" + token + '\'' +
				'}';
	}
}
