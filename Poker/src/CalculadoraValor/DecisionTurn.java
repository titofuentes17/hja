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
// cada mano, se compara con el EM: si es mayor, Call; si no, Fold (el enunciado
// dice "si la media obtenida es mayor al EM"; si es igual, Fold).
public final class DecisionTurn {

    // Detalle de una mano concreta del rango del villano
    public static final class Linea {
        public final String mano;     // "AcAs" (en Omaha, 4 cartas: "AcAsKdQh")
        public final double outs;     // outs de hero contra esa mano (empates = 0,5)
        public final int cartasRestantes;
        public final String grupo;    // Omaha: clase del rango de la que sale la mano ("AA")

        Linea(String mano, double outs, int cartasRestantes) {
            this(mano, outs, cartasRestantes, null);
        }

        Linea(String mano, double outs, int cartasRestantes, String grupo) {
            this.mano = mano;
            this.outs = outs;
            this.cartasRestantes = cartasRestantes;
            this.grupo = grupo;
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
        return new Resultado(lineas, media, restantes, em, media > em);
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

    // =========================================================
    // OMAHA (práctica 3, apartado 2.3)
    // =========================================================
    //
    // Igual que en Hold'em, pero hero y villano tienen 4 cartas y cada mano se
    // forma con exactamente 2 cartas propias y exactamente 3 comunes.
    //
    // El rango del villano se escribe igual que en Hold'em (AA, QQ, 25 %...):
    // significa que el villano tiene una de esas combinaciones entre sus 4
    // cartas, y las otras 2 pueden ser cualesquiera. Cada mano de 4 cartas
    // distinta cuenta una vez en la media.

    // Las 10 formas de elegir 3 cartas de las 5 del board (4 del Turn + el River)
    private static final int[][] TRIOS_BOARD = {
        {0, 1, 2}, {0, 1, 3}, {0, 1, 4}, {0, 2, 3}, {0, 2, 4},
        {0, 3, 4}, {1, 2, 3}, {1, 2, 4}, {1, 3, 4}, {2, 3, 4}
    };

    // Las 6 parejas de cartas propias de una mano de 4 cartas
    private static final int[][] PAREJAS = {
        {0, 1}, {0, 2}, {0, 3}, {1, 2}, {1, 3}, {2, 3}
    };

    public static Resultado evaluarOmaha(List<Carta> hero, List<Carta> board, Rango villano, double em) {
        if (hero.size() != 4) {
            throw new IllegalArgumentException("En Omaha hero debe tener exactamente 4 cartas.");
        }
        if (board.size() != 4) {
            throw new IllegalArgumentException("El board debe tener 4 cartas (Turn).");
        }
        if (villano == null || villano.esVacio()) {
            throw new IllegalArgumentException("Indica el rango del villano.");
        }

        boolean[] usadas = new boolean[52];
        int[] heroIds = new int[4];
        int[] b = new int[5];   // b[4] es la carta del River
        for (int i = 0; i < 4; i++) {
            heroIds[i] = Cartas.id(hero.get(i));
            if (usadas[heroIds[i]]) throw new IllegalArgumentException("Carta repetida en hero: " + hero.get(i));
            usadas[heroIds[i]] = true;
        }
        for (int i = 0; i < 4; i++) {
            b[i] = Cartas.id(board.get(i));
            if (usadas[b[i]]) throw new IllegalArgumentException("Carta repetida entre hero y board: " + board.get(i));
            usadas[b[i]] = true;
        }

        // Hero es siempre el mismo: su mejor jugada para cada River se calcula una vez
        int[] heroConRiver = new int[52];
        for (int river = 0; river < 52; river++) {
            if (usadas[river]) continue;
            b[4] = river;
            heroConRiver[river] = mejorJugada(heroIds, b);
        }

        // Manos posibles del villano: cada combinación del rango más 2 cartas
        // cualesquiera. Se guardan sin repetir (clave = las 4 cartas ordenadas).
        java.util.Map<Long, String> manos = new java.util.LinkedHashMap<>();
        for (int[] combo : villano.getCombos()) {
            if (usadas[combo[0]] || usadas[combo[1]]) continue;
            String clase = Rango.clase(Cartas.carta(combo[0]), Cartas.carta(combo[1]));
            for (int c = 0; c < 52; c++) {
                if (usadas[c] || c == combo[0] || c == combo[1]) continue;
                for (int d = c + 1; d < 52; d++) {
                    if (usadas[d] || d == combo[0] || d == combo[1]) continue;
                    int[] mano = { combo[0], combo[1], c, d };
                    java.util.Arrays.sort(mano);
                    manos.putIfAbsent(clave(mano), clase);
                }
            }
        }
        if (manos.isEmpty()) {
            throw new IllegalArgumentException(
                    "El rango del villano no tiene ninguna mano posible con esas cartas.");
        }

        // La mejor jugada de una pareja de cartas del villano con un River se repite
        // en muchas manos: se guarda la primera vez que se calcula (-1 = sin calcular)
        int[] parejaConRiver = new int[52 * 52 * 52];
        java.util.Arrays.fill(parejaConRiver, -1);

        List<Linea> lineas = new ArrayList<>();
        double suma = 0;
        int[] mano = new int[4];
        for (java.util.Map.Entry<Long, String> e : manos.entrySet()) {
            long k = e.getKey();
            for (int i = 0; i < 4; i++) mano[i] = (int) (k >> (6 * i)) & 63;

            double outs = 0;
            int restantes = 0;
            for (int river = 0; river < 52; river++) {
                if (usadas[river] || river == mano[0] || river == mano[1]
                        || river == mano[2] || river == mano[3]) continue;
                restantes++;
                b[4] = river;
                int v = -1;
                for (int[] p : PAREJAS) {
                    int indice = (mano[p[0]] * 52 + mano[p[1]]) * 52 + river;
                    if (parejaConRiver[indice] < 0) {
                        parejaConRiver[indice] = mejorConPareja(mano[p[0]], mano[p[1]], b);
                    }
                    v = Math.max(v, parejaConRiver[indice]);
                }
                int h = heroConRiver[river];
                if (h > v) outs += 1;
                else if (h == v) outs += 0.5;
            }

            StringBuilder texto = new StringBuilder();
            for (int id : mano) texto.append(Cartas.carta(id));
            lineas.add(new Linea(texto.toString(), outs, restantes, e.getValue()));
            suma += outs;
        }

        double media = suma / lineas.size();
        // Todas las manos dejan 40 cartas posibles en el River (52 - 4 - 4 - 4)
        int restantes = lineas.get(0).cartasRestantes;
        return new Resultado(lineas, media, restantes, em, media > em);
    }

    // Mejor de las 60 manos de Omaha: 6 parejas propias x 10 tríos del board
    private static int mejorJugada(int[] mano, int[] b) {
        int mejor = -1;
        for (int[] p : PAREJAS) mejor = Math.max(mejor, mejorConPareja(mano[p[0]], mano[p[1]], b));
        return mejor;
    }

    // Mejor mano con esas 2 cartas propias y 3 de las 5 comunes
    private static int mejorConPareja(int x, int y, int[] b) {
        int[] c = new int[5];
        c[0] = x;
        c[1] = y;
        int mejor = -1;
        for (int[] t : TRIOS_BOARD) {
            c[2] = b[t[0]];
            c[3] = b[t[1]];
            c[4] = b[t[2]];
            mejor = Math.max(mejor, EvaluadorRapido.evaluar(c, 5));
        }
        return mejor;
    }

    // 4 ids de carta (0..51, ordenados) en un solo número, 6 bits cada uno
    private static long clave(int[] mano) {
        long k = 0;
        for (int i = 0; i < 4; i++) k |= (long) mano[i] << (6 * i);
        return k;
    }

    // Resumen de Omaha: por cada clase del rango, cuántas manos salen y su media de outs
    //   AA: 900 manos, media 12,5 outs
    //   Media = 20,31 outs
    public static String resumenOmaha(Resultado r) {
        java.util.LinkedHashMap<String, double[]> grupos = new java.util.LinkedHashMap<>();
        for (Linea l : r.lineas) {
            double[] g = grupos.computeIfAbsent(l.grupo, k -> new double[2]);
            g[0] += 1;        // manos
            g[1] += l.outs;   // suma de outs
        }
        StringBuilder sb = new StringBuilder();
        sb.append(r.lineas.size()).append(" manos posibles del villano\n");
        for (var e : grupos.entrySet()) {
            double[] g = e.getValue();
            sb.append(e.getKey()).append(": ").append((long) g[0]).append(" manos, media ")
              .append(num2(g[1] / g[0]).replace('.', ',')).append(" outs\n");
        }
        sb.append(String.format(java.util.Locale.ROOT, "Media = %s outs (de %d cartas, %.2f%%)",
                num2(r.mediaOuts), r.cartasRestantes, r.equityMedia).replace('.', ','));
        return sb.toString();
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
