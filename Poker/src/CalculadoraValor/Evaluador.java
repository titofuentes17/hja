package CalculadoraValor;
import java.util.*;

public class Evaluador {
	
	private Evaluador() {}
	
	public static String obtenerMejorManoTexto(List<Carta> cartas) {
		
		Utils.ordenarCartas(cartas);
		
		//Map de los distintos valores de las cartas
		Map<Character, Integer> valores = new HashMap<>();
		for (Carta c: cartas) {
			valores.put(c.getValor(), valores.getOrDefault(c.getValor(), 0) + 1);
		}
		
		//Map de los distintos palos de las cartas
		Map<Character, Integer> palos = new HashMap<>();
        for (Carta c : cartas) {
            palos.put(c.getPalo(), palos.getOrDefault(c.getPalo(), 0) + 1);
        }
        
        
        boolean esColor = palos.containsValue(5);
        boolean esEscalera = comprobarEscalera(cartas);
        
        if (esColor && esEscalera) {
            if (cartas.get(0).getValor() == 'A' && cartas.get(1).getValor() == 'K') {
                return "Royal Flush (" + Utils.cartasAString(cartas) + ")";
            }
            return "Straight Flush (" + Utils.cartasAString(cartas) + ")";
        }
        
        if (valores.containsValue(4)) {
            char valor = obtenerValorPorCantidad(valores, 4);
            String cartasPoker = obtenerCartasPorValor(cartas, valor);
            return "Four of a Kind (" + nombrePluralValor(valor) + ") (" + cartasPoker + ")";
        }
        
        if (valores.containsValue(3) && valores.containsValue(2)) {
            char trio = obtenerValorPorCantidad(valores, 3);
            char par = obtenerValorPorCantidad(valores, 2);
            String cartasFull = obtenerCartasPorValor(cartas, trio) + obtenerCartasPorValor(cartas, par);
            return nombrePluralValor(trio) + " full of " + nombrePluralValor(par) + " (" + cartasFull + ")";
        }
        
        if (esColor) {
            return "Flush (" + Utils.cartasAString(cartas) + ")";
        }

        if (esEscalera) {
            return "Straight (" + Utils.cartasAString(cartas) + ")";
        }

        if (valores.containsValue(3)) {
            char valor = obtenerValorPorCantidad(valores, 3);
            String cartasTrio = obtenerCartasPorValor(cartas, valor);
            return "Three of a kind (" + nombrePluralValor(valor) + ") (" + cartasTrio + ")";
        }

        int numParejas = 0;
        for (int count : valores.values()) {
            if (count == 2) numParejas++;
        }

        if (numParejas == 2) {
            String cartasDoblePar = obtenerCartasDoblePareja(cartas, valores);
            return "Two Pair (" + cartasDoblePar + ")";
        }

        if (numParejas == 1) {
            char valor = obtenerValorPorCantidad(valores, 2);
            String cartasPareja = obtenerCartasPorValor(cartas, valor);
            return "Pair of " + nombrePluralValor(valor) + " (" + cartasPareja + ")";
        }

        Carta cartaAlta = cartas.get(0);
        return "High Card " + nombreValor(cartaAlta.getValor()) + " (" + cartaAlta.toString() + ")";
	}
	
	
	private static boolean comprobarEscalera(List<Carta> cartas) {
	    Set<Integer> valoresSet = new TreeSet<>();
	    for (Carta c : cartas) {
	        valoresSet.add(c.getValorNumerico());
	    }

	    if (valoresSet.size() < 5) return false; // Hay parejas, no puede ser escalera

	    List<Integer> vals = new ArrayList<>(valoresSet);
	    
	    // Caso cartas consecutivas
	    if (vals.get(4) - vals.get(0) == 4) return true;

	    // Caso A-2-3-4-5
	    if (vals.equals(Arrays.asList(2, 3, 4, 5, 14))) return true;

	    return false;
	}


	private static char obtenerValorPorCantidad(Map<Character, Integer> freq, int n) {
	    for (Map.Entry<Character, Integer> entry : freq.entrySet()) {
	        if (entry.getValue() == n) return entry.getKey();
	    }
	    return '0';
	}

	private static String nombreValor(char v) {
	    switch (v) {
	        case 'A': return "Ace";
	        case 'K': return "King";
	        case 'Q': return "Queen";
	        case 'J': return "Jack";
	        case 'T': return "Ten";
	        default: return String.valueOf(v);
	    }
	}

	private static String nombrePluralValor(char v) {
	    String nombre = nombreValor(v);
	    if (v == '6') return "Sixes";
	    return nombre + "s";
	}
	
	
	private static String obtenerCartasPorValor(List<Carta> cartas, char valor) {
	    StringBuilder sb = new StringBuilder();
	    for (Carta c : cartas) {
	        if (c.getValor() == valor) {
	            sb.append(c.toString());
	        }
	    }
	    return sb.toString();
	}
	
	private static String obtenerCartasDoblePareja(List<Carta> cartas, Map<Character, Integer> valores) {
	    StringBuilder sb = new StringBuilder();
	    for (Carta c : cartas) {
	        if (valores.get(c.getValor()) == 2) {
	            sb.append(c.toString());
	        }
	    }
	    return sb.toString();
	}
	
	
	

}



