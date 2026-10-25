package CalculadoraValor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Rankings de las 169 manos iniciales (de mejor a peor), usados para elegir
// un rango por porcentaje ("el 15 % de mejores manos").
//
//  - SKLANSKY: ordenadas por su número de Sklansky-Chubukov (tabla clásica)
//  - EQUITY:   ordenadas por su equity frente a una mano aleatoria
//              (calculada por simulación Monte Carlo con 200.000 reparticiones por mano)
public final class RankingManos {

    public static final String NOMBRE_SKLANSKY = "Sklansky-Chubukov";
    public static final String NOMBRE_EQUITY = "Equity vs mano aleatoria";

    public static final List<String> NOMBRES = List.of(NOMBRE_SKLANSKY, NOMBRE_EQUITY);

    // Tabla de Sklansky-Chubukov, con la misma disposición que en el enunciado.
    // Filas y columnas en orden A K Q J T 9 8 7 6 5 4 3 2:
    //   - encima de la diagonal: manos suited   (fila = carta alta, columna = carta baja)
    //   - debajo de la diagonal: manos offsuit  (columna = carta alta, fila = carta baja)
    //   - en la diagonal: parejas (AA es infinito)
    private static final double INF = Double.POSITIVE_INFINITY;

    private static final double[][] SKLANSKY = {
        /* A */ { INF, 277, 137, 91.6, 69.5, 52,   44.9, 39.5, 35.3, 36.1, 33.3, 31.1, 29   },
        /* K */ { 166, 477, 43.3, 36.3, 31.4, 23.9, 19.9, 18.6, 17.4, 16.1, 15,   14.1, 13.3 },
        /* Q */ { 96,  29.3, 239, 24.7, 21.9, 16.2, 13.3, 11.3, 10.9, 10.1, 9.4,  8.8,  8.3  },
        /* J */ { 68.1, 25.4, 16.4, 159, 18,  12.8, 10.3, 8.5,  7.3,  7,    6.4,  6,    5.5  },
        /* T */ { 53.1, 22.4, 14.8, 11.5, 120, 11.2, 8.7,  7,    5.9,  4.9,  4.6,  4.2,  3.7  },
        /* 9 */ { 40.8, 17.8, 11.7, 8.8,  7.4, 95.7, 7.6,  6.1,  5,    4.1,  3.2,  3,    2.6  },
        /* 8 */ { 35.4, 15.2, 9.9,  7.4,  6,   5.1,  79.6, 5.5,  4.5,  3.6,  2.8,  2.8,  2.2  },
        /* 7 */ { 31.3, 14.2, 8.5,  6.3,  5.1, 4.2,  3.7,  67.4, 4.1,  3.2,  2.5,  2,    1.6  },
        /* 6 */ { 28,   13.3, 8.1,  5.3,  4.2, 3.5,  3,    2.7,  57.6, 3.1,  2.3,  1.8,  1.5  },
        /* 5 */ { 28.2, 12.3, 7.5,  5,    3.4, 2.8,  2.4,  2.1,  1.9,  49.3, 2.4,  1.9,  1.5  },
        /* 4 */ { 25.9, 11.4, 6.8,  4.4,  3.1, 2.1,  1.8,  1.6,  1.5,  1.6,  40.9, 1.7,  1.4  },
        /* 3 */ { 24.2, 10.6, 6.2,  3.9,  2.7, 2,    1.5,  1.3,  1.3,  1.3,  1.1,  32.7, 1.2  },
        /* 2 */ { 22.5, 10,   5.6,  3.44, 2.4, 1.8,  1.4,  1.1,  1,    1,    0.9,  0.9,  24   }
    };

    private static final String[] EQUITY = {
        "AA", "KK", "QQ", "JJ", "TT", "99", "88", "AKs", "77", "AQs", "AKo", "AJs",
        "ATs", "AQo", "AJo", "KQs", "66", "ATo", "A9s", "KJs", "A8s", "KTs", "KQo", "A7s",
        "A9o", "55", "KJo", "QJs", "A6s", "K9s", "KTo", "A5s", "A8o", "QTs", "A4s", "A7o",
        "QJo", "K8s", "A3s", "A6o", "K9o", "Q9s", "A5o", "K7s", "JTs", "A2s", "QTo", "44",
        "A4o", "K6s", "K8o", "A3o", "Q8s", "J9s", "K5s", "Q9o", "JTo", "K7o", "K4s", "A2o",
        "K6o", "Q7s", "J8s", "T9s", "K3s", "33", "Q8o", "Q6s", "J9o", "K5o", "K2s", "Q5s",
        "J7s", "T8s", "K4o", "Q4s", "J8o", "T9o", "Q7o", "K3o", "Q6o", "Q3s", "J6s", "98s",
        "T7s", "K2o", "22", "Q2s", "Q5o", "J5s", "J7o", "T8o", "Q4o", "97s", "T6s", "J4s",
        "J3s", "98o", "Q3o", "T7o", "87s", "J6o", "J2s", "96s", "J5o", "Q2o", "T5s", "T4s",
        "86s", "97o", "J4o", "T6o", "T3s", "95s", "76s", "J3o", "87o", "T2s", "96o", "85s",
        "J2o", "T5o", "94s", "75s", "T4o", "65s", "93s", "86o", "84s", "95o", "T3o", "92s",
        "76o", "74s", "T2o", "54s", "85o", "64s", "83s", "94o", "82s", "75o", "73s", "53s",
        "93o", "65o", "84o", "63s", "92o", "43s", "74o", "72s", "54o", "64o", "52s", "62s",
        "83o", "82o", "42s", "73o", "53o", "63o", "32s", "43o", "72o", "52o", "62o", "42o",
        "32o"
    };

    private RankingManos() {}

    // Texto con el ranking completo: posición, mano, combinaciones y porcentaje
    // acumulado de manos que se cubre hasta esa posición.
    public static String tabla(String nombre) {
        List<String> ranking = obtener(nombre);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%4s  %-5s %7s  %10s%n", "#", "Mano", "Combos", "Acumulado"));

        int acumulado = 0;
        for (int i = 0; i < ranking.size(); i++) {
            String clase = ranking.get(i);
            int combos = clase.length() == 2 ? 6 : (clase.endsWith("s") ? 4 : 12);
            acumulado += combos;

            sb.append(String.format("%4d  %-5s %7d  %8.2f %%%n",
                    i + 1, clase, combos, 100.0 * acumulado / Rango.TOTAL_COMBOS));
        }
        return sb.toString();
    }

    public static List<String> obtener(String nombre) {
        if (NOMBRE_SKLANSKY.equals(nombre)) {
            return porSklansky();
        }
        return Collections.unmodifiableList(Arrays.asList(EQUITY));
    }

    // =========================================================
    // SKLANSKY-CHUBUKOV
    // =========================================================

    // Recorre la tabla, da nombre a cada clase (AA, AKs, AKo...) y las ordena
    // de mayor a menor número de Sklansky-Chubukov. Los empates se resuelven
    // con carta alta mayor, carta baja mayor y suited antes que offsuit.
    private static List<String> porSklansky() {
        final String orden = "AKQJT98765432";

        List<String> clases = new ArrayList<>();
        final Map<String, Double> valor = new HashMap<>();

        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 13; j++) {
                char a = orden.charAt(i);
                char b = orden.charAt(j);

                String clase;
                if (i == j) {
                    clase = "" + a + a;              // pareja
                } else if (i < j) {
                    clase = "" + a + b + "s";        // encima de la diagonal: suited
                } else {
                    clase = "" + b + a + "o";        // debajo de la diagonal: offsuit
                }

                valor.put(clase, SKLANSKY[i][j]);
                clases.add(clase);
            }
        }

        clases.sort((x, y) -> {
            int c = Double.compare(valor.get(y), valor.get(x));
            if (c != 0) return c;
            c = Integer.compare(indiceAlta(y), indiceAlta(x));
            if (c != 0) return c;
            c = Integer.compare(indiceBaja(y), indiceBaja(x));
            if (c != 0) return c;
            return Boolean.compare(y.endsWith("s"), x.endsWith("s"));
        });

        return clases;
    }

    private static int indiceAlta(String clase) {
        return Cartas.RANGOS.indexOf(clase.charAt(0));
    }

    private static int indiceBaja(String clase) {
        return Cartas.RANGOS.indexOf(clase.charAt(1));
    }
}
