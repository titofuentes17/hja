package CalculadoraValor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
	
	//Lectura fichero
	public static List<String> leerFichero(String ruta) throws IOException {
	    List<String> lineas = new ArrayList<>();
	    try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
	        String linea;
	        while ((linea = br.readLine()) != null) {
	            linea = linea.trim();
	            if (!linea.isEmpty()) {
	                lineas.add(linea);
	            }
	        }
	    }
	    return lineas;
	}
	
	//Escritura fichero
	public static void escribirFichero(String ruta, List<String> contenido) throws IOException {
	    try (BufferedWriter bw = new BufferedWriter(new FileWriter(ruta))) {
	        for (String linea : contenido) {
	            bw.write(linea);
	            bw.newLine();
	        }
	    }
	}

}
