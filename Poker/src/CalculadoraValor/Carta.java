package CalculadoraValor;

import java.util.ArrayList;
import java.util.List;

public class Carta {
	
	private char valor;
	private char palo;
	
	//Constructor
	public Carta(char valor, char palo) {
		this.valor= valor;
		this.palo = palo;
	}
	
	//Constructor String
	public Carta(String s) {
		this.valor = s.charAt(0);
		this.valor = s.charAt(1);
	}
	
	
	//Getters
	public char getValor() {return valor;}
	public char getPalo() {return palo;}
	
	
	public int getValorNumerico() {
		switch(valor) {
		case 'A': return 14;
		case 'K': return 13;
		case 'Q': return 12;
		case 'J': return 11;
		case 'T': return 10;
		default: return Character.getNumericValue(valor);
		}
	}
	
	
	public String toString() {
		return "" + valor + palo;
	}
	

}


