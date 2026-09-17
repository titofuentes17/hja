package CalculadoraValor;

import java.util.ArrayList;
import java.util.List;

public class Utils {
	
	private Utils() {}
	
	public static List<Carta> parsearCartas(String s) {
		List<Carta> lista = new ArrayList<>();
		for (int i = 0; i < s.length(); i += 2) {
			lista.add(new Carta(s.charAt(i), s.charAt(i+1)));
		}
		return lista;
	}
	
	
	public static String cartasAString(List<Carta> cartas) {
		StringBuilder sb = new StringBuilder();
		for(Carta c: cartas) {
			sb.append(c.toString());
		}
		return sb.toString();
	}
	
	public static void ordenarCartas(List<Carta> cartas) {
		cartas.sort((c1, c2) -> c2.getValorNumerico() - c1.getValorNumerico());
	}

}
