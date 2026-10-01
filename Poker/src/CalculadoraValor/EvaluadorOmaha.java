package CalculadoraValor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class EvaluadorOmaha {

    private EvaluadorOmaha() {}

    // Genera todas las combinaciones posibles de Omaha:
    // Obligatoriamente 2 cartas de la mano y 3 cartas de la mesa
    public static List<List<Carta>> generarCombinacionesOmaha(List<Carta> propias, List<Carta> mesa) {
        List<List<Carta>> combinaciones = new ArrayList<>();

        if (propias.size() < 2 || mesa.size() < 3) {
            return combinaciones;
        }

        // Elegimos 2 cartas de las cartas propias (4 en total)
        for (int i = 0; i < propias.size() - 1; i++) {
            for (int j = i + 1; j < propias.size(); j++) {

                // Elegimos 3 cartas de la mesa (3, 4 o 5 cartas)
                for (int a = 0; a < mesa.size() - 2; a++) {
                    for (int b = a + 1; b < mesa.size() - 1; b++) {
                        for (int c = b + 1; c < mesa.size(); c++) {

                            List<Carta> combinacion = new ArrayList<>();
                            combinacion.add(mesa.get(a));
                            combinacion.add(mesa.get(b));
                            combinacion.add(mesa.get(c));
                            combinacion.add(propias.get(i));
                            combinacion.add(propias.get(j));

                            combinaciones.add(combinacion);
                        }
                    }
                }
            }
        }

        return combinaciones;
    }

    // Formato de texto para la mejor mano
    public static String formatearMejorMano(ValorMano valorMano, List<Carta> cartas5) {
        int categoria = valorMano.getCategoria();
        String nombre = Evaluador.obtenerNombreMano(new ArrayList<>(cartas5));

        // En el enunciado del apartado 4 piden "2's full of Aces" con apóstrofo
        if (Character.isDigit(nombre.charAt(0)) && nombre.length() > 1 && nombre.charAt(1) == 's') {
            nombre = nombre.charAt(0) + "'s" + nombre.substring(2);
        }

        // Si es Full House, ponemos las 3 cartas del trío primero y luego la pareja
        if (categoria == 7) {
            char valorTrio = ' ';
            for (Carta c1 : cartas5) {
                int repeticiones = 0;
                for (Carta c2 : cartas5) {
                    if (c1.getValor() == c2.getValor()) {
                        repeticiones++;
                    }
                }
                if (repeticiones == 3) {
                    valorTrio = c1.getValor();
                    break;
                }
            }

            List<Carta> ordenadas = new ArrayList<>();
            for (Carta c : cartas5) {
                if (c.getValor() == valorTrio) {
                    ordenadas.add(c);
                }
            }
            for (Carta c : cartas5) {
                if (c.getValor() != valorTrio) {
                    ordenadas.add(c);
                }
            }
            return nombre + " with " + Utils.cartasAString(ordenadas);
        }

        // Para el resto de manos, las ordenamos de mayor a menor valor
        List<Carta> ordenadas = new ArrayList<>(cartas5);
        Utils.ordenarCartas(ordenadas);
        return nombre + " with " + Utils.cartasAString(ordenadas);
    }

    // Calcula los proyectos (draws) en Omaha
    public static List<String> obtenerDrawsOmaha(List<Carta> propias, List<Carta> mesa, int categoriaMejorMano) {
        List<String> draws = new ArrayList<>();

        // Con 5 cartas comunes en la mesa ya no hay draws
        if (mesa.size() >= 5) {
            return draws;
        }

        // 1. Flush Draw:
        // En Omaha necesitamos tener al menos 2 cartas del mismo palo en la mano
        // y exactamente 2 cartas de ese palo en la mesa
        char[] palos = {'h', 'd', 'c', 's'};
        for (char p : palos) {
            int enMano = 0;
            for (Carta c : propias) {
                if (c.getPalo() == p) {
                    enMano++;
                }
            }

            int enMesa = 0;
            for (Carta c : mesa) {
                if (c.getPalo() == p) {
                    enMesa++;
                }
            }

            if (enMano >= 2 && enMesa == 2) {
                if (!draws.contains("Draw: Flush")) {
                    draws.add("Draw: Flush");
                }
            }
        }

        // 2. Straight Draw:
        // 2 cartas de la mano + 2 cartas de la mesa que formen proyecto de escalera
        boolean hayOpenEnded = false;
        boolean hayGutshot = false;

        for (int i = 0; i < propias.size() - 1; i++) {
            for (int j = i + 1; j < propias.size(); j++) {
                for (int a = 0; a < mesa.size() - 1; a++) {
                    for (int b = a + 1; b < mesa.size(); b++) {

                        List<Carta> cuatro = new ArrayList<>();
                        cuatro.add(propias.get(i));
                        cuatro.add(propias.get(j));
                        cuatro.add(mesa.get(a));
                        cuatro.add(mesa.get(b));

                        Set<Integer> valoresSet = new TreeSet<>();
                        for (Carta c : cuatro) {
                            valoresSet.add(c.getValorNumerico());
                            if (c.getValorNumerico() == 14) {
                                valoresSet.add(1); // El As también puede valer 1
                            }
                        }

                        if (valoresSet.size() >= 4) {
                            List<Integer> vals = new ArrayList<>(valoresSet);
                            for (int k = 0; k <= vals.size() - 4; k++) {
                                int v1 = vals.get(k);
                                int v2 = vals.get(k + 1);
                                int v3 = vals.get(k + 2);
                                int v4 = vals.get(k + 3);

                                if (v4 - v1 == 3 && v2 - v1 == 1 && v3 - v2 == 1) {
                                    hayOpenEnded = true;
                                } else if (v4 - v1 == 4) {
                                    hayGutshot = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (hayOpenEnded) {
            draws.add("Draw: Straight Open-ended");
        } else if (hayGutshot) {
            draws.add("Draw: Straight Gutshot");
        }

        // Si hay Open-ended, no mostramos también Gutshot
        if (draws.contains("Draw: Straight Open-ended")) {
            draws.remove("Draw: Straight Gutshot");
        }

        // Quitamos los draws que ya están completados en la mejor mano actual
        List<String> resultado = new ArrayList<>();
        for (String d : draws) {
            boolean drawEscalera = d.startsWith("Draw: Straight");
            boolean drawColor = d.equals("Draw: Flush");

            if (drawEscalera && categoriaMejorMano >= 5) continue;
            if (drawColor && categoriaMejorMano >= 6) continue;

            resultado.add(d);
        }

        return resultado;
    }
}
