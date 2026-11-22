package CalculadoraValor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Práctica 3, apartado 2.2: decisión en el Turn, hero contra UN villano al que
// se le asigna un rango (la mano concreta del villano no interviene).
//
// Para cada mano posible del rango del villano (que no choque con las cartas
// de hero ni con el board) se cuentan las "outs" de hero: cartas del River
// con las que hero gana. Un empate cuenta como media out (el bote se reparte).
// La media aritmética de outs, ponderada por el número de combinaciones de
// cada mano, se compara con el EM: si lo alcanza o supera, Call; si no, Fold.
public final class DecisionTurn {

    // Detalle de una mano concreta del rango del villano
    public static final class Linea {
        public final String mano;     // "AcAs"
        public final double outs;     // outs de hero contra esa mano (empates = 0,5)
        public final int cartasRestantes;

        Linea(String mano, double outs, int cartasRestantes) {
            this.mano = mano;
            this.outs = outs;
            this.cartasRestantes = cartasRestantes;
        }
    }

    // Resumen por clase (AA, QQ, AKs...): todas sus combinaciones tienen las mismas outs
    // salvo casos raros (colores), por lo que se agrupan y se muestran con su media
    public static final class Resultado {
        public final List<Linea> lineas;
        public final double mediaOuts;
        public final int cartasRestantes;   // cartas que pueden salir en el River
        public final double equityMedia;    // mediaOuts / cartasRestantes * 100
        public final double em;
        public final boolean call;

        Resultado(List<Linea> lineas, double mediaOuts, int restantes, double em, boolean call) {
            this.lineas = lineas;
            this.mediaOuts = mediaOuts;
            this.cartasRestantes = restantes;
            this.equityMedia = restantes == 0 ? 0 : 100.0 * mediaOuts / restantes;
            this.em = em;
            this.call = call;
        }
    }

    private DecisionTurn() {}

    public static Resultado evaluar(List<Carta> hero, List<Carta> board, Rango villano, double em) {
        if (hero.size() != 2) {
            throw new IllegalArgumentException("Hero debe tener exactamente 2 cartas.");
        }
        if (board.size() != 4) {
            throw new IllegalArgumentException("El board debe tener 4 cartas (Turn).");
        }
        if (villano == null || villano.esVacio()) {
            throw new IllegalArgumentException("Indica el rango del villano.");
        }

        Set<Integer> usadas = new HashSet<>();
        for (Carta c : hero) usadas.add(Cartas.id(c));
        for (Carta c : board) {
            if (!usadas.add(Cartas.id(c))) {
                throw new IllegalArgumentException("Carta repetida entre hero y board: " + c);
            }
        }

        List<Linea> lineas = new ArrayList<>();
        double suma = 0;

        for (int[] combo : villano.getCombos()) {
            if (usadas.contains(combo[0]) || usadas.contains(combo[1])) continue;

            double outs = 0;
            int restantes = 0;
            for (int river = 0; river < 52; river++) {
                if (usadas.contains(river) || river == combo[0] || river == combo[1]) continue;
                restantes++;
                int h = puntuar(hero, board, river);
                int v = puntuar(combo, board, river);
                if (h > v) outs += 1;
                else if (h == v) outs += 0.5;
            }
            lineas.add(new Linea(Cartas.carta(combo[0]).toString() + Cartas.carta(combo[1]),
                    outs, restantes));
            suma += outs;
        }

        if (lineas.isEmpty()) {
            throw new IllegalArgumentException(
                    "El rango del villano no tiene ninguna mano posible con esas cartas.");
        }

        double media = suma / lineas.size();
        // Todas las manos dejan 44 cartas posibles en el River
        int restantes = lineas.get(0).cartasRestantes;
        return new Resultado(lineas, media, restantes, em, media >= em);
    }

    private static int puntuar(List<Carta> hero, List<Carta> board, int river) {
        int[] c = new int[7];
        c[0] = Cartas.id(hero.get(0));
        c[1] = Cartas.id(hero.get(1));
        for (int i = 0; i < 4; i++) c[2 + i] = Cartas.id(board.get(i));
        c[6] = river;
        return EvaluadorRapido.evaluar(c, 7);
    }

    private static int puntuar(int[] villano, List<Carta> board, int river) {
        int[] c = new int[7];
        c[0] = villano[0];
        c[1] = villano[1];
        for (int i = 0; i < 4; i++) c[2 + i] = Cartas.id(board.get(i));
        c[6] = river;
        return EvaluadorRapido.evaluar(c, 7);
    }

    // Texto agrupado por clase de mano, como en el enunciado:
    //   AA: 4 outs
    //   QQ: 41 outs x 6
    //   Media = 35,71 outs
    public static String resumen(Resultado r) {
        java.util.LinkedHashMap<String, List<Linea>> grupos = new java.util.LinkedHashMap<>();
        for (Linea l : r.lineas) {
            Carta a = new Carta(l.mano.substring(0, 2));
            Carta b = new Carta(l.mano.substring(2, 4));
            grupos.computeIfAbsent(Rango.clase(a, b), k -> new ArrayList<>()).add(l);
        }
        StringBuilder sb = new StringBuilder();
        for (var e : grupos.entrySet()) {
            List<Linea> ls = e.getValue();
            // Si todas las combinaciones tienen las mismas outs, se resume en una línea
            boolean iguales = true;
            for (Linea l : ls) if (l.outs != ls.get(0).outs) iguales = false;
            if (iguales) {
                sb.append(e.getKey()).append(": ").append(num(ls.get(0).outs)).append(" outs");
                if (ls.size() > 1) sb.append("  x  ").append(ls.size()).append(" combos");
                sb.append('\n');
            } else {
                for (Linea l : ls) {
                    sb.append(e.getKey()).append(" (").append(l.mano).append("): ")
                      .append(num(l.outs)).append(" outs\n");
                }
            }
        }
        sb.append(String.format(java.util.Locale.ROOT, "Media = %s outs (de %d cartas, %.2f%%)",
                num2(r.mediaOuts), r.cartasRestantes, r.equityMedia).replace('.', ','));
        return sb.toString();
    }

    private static String num(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v).replace('.', ',');
    }

    private static String num2(double v) {
        return String.format(java.util.Locale.ROOT, "%.2f", v);
    }
}
