package CalculadoraValor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Rankings de las 169 manos iniciales (de mejor a peor), usados para elegir
// un rango por porcentaje ("el 15 % de mejores manos").
//
//  - EQUITY: ordenadas por su equity frente a una mano aleatoria
//            (calculada por simulación Monte Carlo con 200.000 reparticiones por mano)
//  - CHEN:   ordenadas por la puntuación de la fórmula de Bill Chen
public final class RankingManos {

    public static final String NOMBRE_EQUITY = "Equity vs mano aleatoria";
    public static final String NOMBRE_CHEN = "Fórmula de Chen";

    public static final List<String> NOMBRES = List.of(NOMBRE_EQUITY, NOMBRE_CHEN);

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

    public static List<String> obtener(String nombre) {
        if (NOMBRE_CHEN.equals(nombre)) {
            return porChen();
        }
        return Collections.unmodifiableList(Arrays.asList(EQUITY));
    }

    // =========================================================
    // FÓRMULA DE CHEN
    // =========================================================

    private static List<String> porChen() {
        List<String> clases = new ArrayList<>();
        for (int alta = 12; alta >= 0; alta--) {
            for (int baja = alta; baja >= 0; baja--) {
                if (alta == baja) {
                    clases.add("" + Cartas.RANGOS.charAt(alta) + Cartas.RANGOS.charAt(baja));
                } else {
                    clases.add("" + Cartas.RANGOS.charAt(alta) + Cartas.RANGOS.charAt(baja) + "s");
                    clases.add("" + Cartas.RANGOS.charAt(alta) + Cartas.RANGOS.charAt(baja) + "o");
                }
            }
        }

        // Mayor puntuación primero; a igualdad, cartas más altas y suited antes
        clases.sort((x, y) -> {
            int c = Double.compare(puntuacionChen(y), puntuacionChen(x));
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

    private static double valorChen(int indiceRango) {
        switch (indiceRango) {
            case 12: return 10;  // A
            case 11: return 8;   // K
            case 10: return 7;   // Q
            case 9:  return 6;   // J
            default: return (indiceRango + 2) / 2.0;
        }
    }

    static double puntuacionChen(String clase) {
        int alta = indiceAlta(clase);
        int baja = indiceBaja(clase);
        boolean suited = clase.endsWith("s");

        double puntos = valorChen(alta);

        if (alta == baja) {
            return Math.max(5, puntos * 2);
        }

        if (suited) {
            puntos += 2;
        }

        int hueco = alta - baja - 1;
        if (hueco == 1) puntos -= 1;
        else if (hueco == 2) puntos -= 2;
        else if (hueco == 3) puntos -= 4;
        else if (hueco >= 4) puntos -= 5;

        // Bonus por conectar sin pasar de la Q
        if (hueco <= 1 && alta < 10) {
            puntos += 1;
        }

        return Math.ceil(puntos);
    }
}
