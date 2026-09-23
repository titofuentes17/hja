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
	
	
	//Genera todas las posibles combinaciones de 5 cartas dadas 5, 6 o 7 cartas
	public static List<List<Carta>> generarCombinaciones5(List<Carta> cartas) {

	    // Aquí guardaremos todas las combinaciones de 5 cartas
	    List<List<Carta>> combinaciones = new ArrayList<>();

	    // Elegimos 5 posiciones distintas
	    for (int i = 0; i < cartas.size() - 4; i++) {
	        for (int j = i + 1; j < cartas.size() - 3; j++) {
	            for (int k = j + 1; k < cartas.size() - 2; k++) {
	                for (int l = k + 1; l < cartas.size() - 1; l++) {
	                    for (int m = l + 1; m < cartas.size(); m++) {

	                        // Creamos una combinación de 5 cartas
	                        List<Carta> combinacion = new ArrayList<>();

	                        combinacion.add(cartas.get(i));
	                        combinacion.add(cartas.get(j));
	                        combinacion.add(cartas.get(k));
	                        combinacion.add(cartas.get(l));
	                        combinacion.add(cartas.get(m));

	                        // La guardamos
	                        combinaciones.add(combinacion);
	                    }
	                }
	            }
	        }
	    }

	    return combinaciones;
	}

}
