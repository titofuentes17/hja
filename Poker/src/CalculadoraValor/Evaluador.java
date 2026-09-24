package CalculadoraValor;
import java.util.*;

public class Evaluador {
	
	private Evaluador() {}
	
	
	public static List<String> obtenerDraws(List<Carta> cartas) {
        List<String> draws = new ArrayList<>();

        // Flush Draw
        Map<Character, Integer> palos = new HashMap<>();
        for (Carta c : cartas) {
            palos.put(c.getPalo(), palos.getOrDefault(c.getPalo(), 0) + 1);
        }
        if (palos.containsValue(4)) {
            draws.add("Draw: Flush");
        }

        // Straight Draw 
        Set<Integer> valoresSet = new TreeSet<>();
        for (Carta c : cartas) {
            valoresSet.add(c.getValorNumerico());
            if (c.getValorNumerico() == 14) {
                valoresSet.add(1); // El As también actúa como 1
            }
        }

        List<Integer> vals = new ArrayList<>(valoresSet);
        boolean hayOpenEnded = false;
        boolean hayGutshot = false;

        // Comprobamos todas las combinaciones de 4 cartas
        for (int i = 0; i <= vals.size() - 4; i++) {
            int v1 = vals.get(i);
            int v2 = vals.get(i + 1);
            int v3 = vals.get(i + 2);
            int v4 = vals.get(i + 3);

            // 4 cartas consecutivas
            if (v4 - v1 == 3 && v2 - v1 == 1 && v3 - v2 == 1) {
            	hayOpenEnded = true;
            } 
            // 4 cartas contenidas en un rango de 4 posiciones
            else if (v4 - v1 == 4) {
                hayGutshot = true;
            }
        }

        if (hayOpenEnded) {
            draws.add("Draw: Straight Open-ended");
        } else if (hayGutshot) {
            draws.add("Draw: Straight Gutshot");
        }

        return draws;
    }
	
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
	
	
	//Sobrecarga para el apartado 2
	public static String obtenerMejorManoTexto(
	        List<Carta> cartas,
	        boolean usarWith) {

	    if (usarWith) {
	        return obtenerNombreMano(cartas) + " with " + Utils.cartasAString(cartas);
	    }

	    return obtenerMejorManoTexto(cartas);
	}


	//Nombre de la jugada sin las cartas, p.ej. "Pair of Aces" (apartados 2 y 3)
	public static String obtenerNombreMano(List<Carta> cartas) {

	    String resultado = obtenerMejorManoTexto(cartas);

	    // Quitamos el último paréntesis, que es el de las cartas
	    int posicion = resultado.lastIndexOf(" (");
	    if (posicion != -1) {
	        resultado = resultado.substring(0, posicion);
	    }

	    return resultado;
	}
	
	
	//Evaluamos la mano y le ponemos un numero a la mano y devolvemos las cartas
	public static ValorMano evaluarMano(List<Carta> cartas) {

	    // Ordenamos las cartas de mayor a menor
	    Utils.ordenarCartas(cartas);

	    // Contamos cuántas cartas hay de cada valor
	    Map<Character, Integer> valores = new HashMap<>();

	    for (Carta c : cartas) {
	        valores.put(c.getValor(),
	                valores.getOrDefault(c.getValor(), 0) + 1);
	    }

	    // Contamos cuántas cartas hay de cada palo
	    Map<Character, Integer> palos = new HashMap<>();

	    for (Carta c : cartas) {
	        palos.put(c.getPalo(),
	                palos.getOrDefault(c.getPalo(), 0) + 1);
	    }

	    boolean esColor = palos.containsValue(5);
	    boolean esEscalera = comprobarEscalera(cartas);


	    // Escalera de color / Royal Flush
	    
	    if (esColor && esEscalera) {

	        int valorEscalera =
	                obtenerValorEscalera(cartas);

	        List<Integer> desempate =
	                new ArrayList<>();

	        desempate.add(valorEscalera);


	        // Royal Flush
	        if (valorEscalera == 14) {

	            return new ValorMano(
	                    10,
	                    cartas,
	                    desempate
	            );
	        }


	        // Escalera de Color
	        return new ValorMano(
	                9,
	                cartas,
	                desempate
	        );
	    }


	    // POKER
	    
	    if (valores.containsValue(4)) {

	        int valorPoker =
	                buscarValorConCantidad(valores, 4);

	        List<Integer> desempate = new ArrayList<>();

	        // Primero importa el valor del póker
	        desempate.add(valorPoker);

	        // Después el kicker
	        desempate.addAll(
	                obtenerOtrosValores(cartas, valorPoker)
	        );

	        return new ValorMano(
	                8,
	                cartas,
	                desempate
	        );
	    }


	    // FULL 
	    
	    if (valores.containsValue(3)
	            && valores.containsValue(2)) {

	        int valorTrio =
	                buscarValorConCantidad(valores, 3);

	        int valorPareja =
	                buscarValorConCantidad(valores, 2);

	        List<Integer> desempate = new ArrayList<>();

	        desempate.add(valorTrio);
	        desempate.add(valorPareja);

	        return new ValorMano(
	                7,
	                cartas,
	                desempate
	        );
	    }


	    // COLOR
	    
	    if (esColor) {

	        List<Integer> desempate =
	                obtenerValoresOrdenados(cartas);

	        return new ValorMano(
	                6,
	                cartas,
	                desempate
	        );
	    }


	    // ESCALERA
	    
	    if (esEscalera) {

	        int valorEscalera =
	                obtenerValorEscalera(cartas);

	        List<Integer> desempate =
	                new ArrayList<>();

	        desempate.add(valorEscalera);

	        return new ValorMano(
	                5,
	                cartas,
	                desempate
	        );
	    }


	    // TRIO
	    
	    if (valores.containsValue(3)) {

	        int valorTrio =
	                buscarValorConCantidad(valores, 3);

	        List<Integer> desempate = new ArrayList<>();

	        desempate.add(valorTrio);

	        // Añadimos después los kickers
	        desempate.addAll(
	                obtenerOtrosValores(cartas, valorTrio)
	        );

	        return new ValorMano(
	                4,
	                cartas,
	                desempate
	        );
	    }


	    // Contamos las parejas
	    int numParejas = 0;

	    for (int cantidad : valores.values()) {
	        if (cantidad == 2) {
	            numParejas++;
	        }
	    }


	    // DOBLE PAREJA
	    
	    if (numParejas == 2) {

	        List<Integer> parejas = new ArrayList<>();
	        int kicker = 0;

	        for (Carta carta : cartas) {

	            int cantidad =
	                    valores.get(carta.getValor());

	            if (cantidad == 2) {

	                int valor =
	                        carta.getValorNumerico();

	                if (!parejas.contains(valor)) {
	                    parejas.add(valor);
	                }

	            } else {

	                kicker = carta.getValorNumerico();
	            }
	        }

	        // Pareja más alta primero
	        parejas.sort(Collections.reverseOrder());

	        List<Integer> desempate = new ArrayList<>();

	        desempate.add(parejas.get(0));
	        desempate.add(parejas.get(1));
	        desempate.add(kicker);

	        return new ValorMano(
	                3,
	                cartas,
	                desempate
	        );
	    }


	    // PAREJA
	    
	    if (numParejas == 1) {

	        int valorPareja =
	                buscarValorConCantidad(valores, 2);

	        List<Integer> desempate = new ArrayList<>();

	        // Primero el valor de la pareja
	        desempate.add(valorPareja);

	        // Después los tres kickers
	        desempate.addAll(
	                obtenerOtrosValores(cartas, valorPareja)
	        );

	        return new ValorMano(
	                2,
	                cartas,
	                desempate
	        );
	    }
	    
	 
	    // CARTA ALTA
	    
	    List<Integer> desempate =
	            obtenerValoresOrdenados(cartas);

	    return new ValorMano(
	            1,
	            cartas,
	            desempate
	    );
	}
	
	//ObtenerDraws para el apartado 2
	public static List<String> obtenerDrawsApartado2(
	        List<Carta> disponibles,
	        int numComunes) {

	    List<String> resultado = new ArrayList<>();


	    // Con 5 cartas comunitarias ya no hay draws
	    if (numComunes == 5) {
	        return resultado;
	    }


	    // Si tenemos 5 cartas disponibles,
	    // podemos utilizar directamente el método del apartado 1
	    if (disponibles.size() == 5) {

	        resultado.addAll(
	                obtenerDraws(disponibles)
	        );

	        return resultado;
	    }


	    // Si tenemos 6 cartas disponibles,
	    // generamos todas las combinaciones de 5
	    List<List<Carta>> combinaciones =
	            Utils.generarCombinaciones5(disponibles);


	    // Buscamos los draws de cada combinación
	    for (List<Carta> combinacion : combinaciones) {

	        List<String> draws =
	                obtenerDraws(combinacion);

	        for (String draw : draws) {

	            // Evitamos añadir el mismo draw varias veces
	            if (!resultado.contains(draw)) {
	                resultado.add(draw);
	            }
	        }
	    }

	    
	 // Si tenemos un proyecto de escalera abierta,
	 // no mostramos también Gutshot
	 if (resultado.contains("Draw: Straight Open-ended")) {
	     resultado.remove("Draw: Straight Gutshot");
	 }
	 
	 
	    return resultado;
	}
	
	
	
	
	
	//Ordena la mano 
	private static List<Integer> obtenerValoresOrdenados(List<Carta> cartas) {

	    List<Integer> valores = new ArrayList<>();

	    for (Carta carta : cartas) {
	        valores.add(carta.getValorNumerico());
	    }

	    // Ordenamos de mayor a menor
	    valores.sort(Collections.reverseOrder());

	    return valores;
	}
	
	//El palo no importa
	
	private static int buscarValorConCantidad(
	        Map<Character, Integer> valores,
	        int cantidad) {

	    for (Map.Entry<Character, Integer> entrada : valores.entrySet()) {

	        if (entrada.getValue() == cantidad) {

	            Carta carta = new Carta(
	                    entrada.getKey(),
	                    'h'
	            );

	            return carta.getValorNumerico();
	        }
	    }

	    return -1;
	}
	
	//obtenemos valores que no pertenecen a cierto grupo
	private static List<Integer> obtenerOtrosValores(
	        List<Carta> cartas,
	        int valorExcluido) {

	    List<Integer> resultado = new ArrayList<>();

	    for (Carta carta : cartas) {

	        if (carta.getValorNumerico() != valorExcluido) {
	            resultado.add(carta.getValorNumerico());
	        }
	    }

	    resultado.sort(Collections.reverseOrder());

	    return resultado;
	}
	
	private static int obtenerValorEscalera(List<Carta> cartas) {

	    List<Integer> valores =
	            obtenerValoresOrdenados(cartas);

	    // Caso especial:
	    // A 5 4 3 2
	    // El As actúa como 1, por lo que la escalera
	    // tiene valor 5 y no 14
	    if (valores.get(0) == 14
	            && valores.get(1) == 5
	            && valores.get(2) == 4
	            && valores.get(3) == 3
	            && valores.get(4) == 2) {

	        return 5;
	    }

	    // En cualquier otra escalera,
	    // devolvemos la carta más alta
	    return valores.get(0);
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



