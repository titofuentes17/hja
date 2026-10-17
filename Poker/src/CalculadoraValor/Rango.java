package CalculadoraValor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Conjunto de manos iniciales (2 cartas) que puede tener un jugador.
//
// Sintaxis de texto admitida (separada por comas, espacios o ;):
//   AA          pareja de ases
//   AKs / AKo   suited / offsuit      AK = ambas
//   77+         pareja de sietes o mejor (77,88,...,AA)
//   ATs+        ATs, AJs, AQs, AKs
//   A5s-A2s     rango entre dos manos con la misma carta alta (o mismo hueco)
//   22-55       rango de parejas
//   AhKd        mano concreta
public final class Rango {

    public static final int TOTAL_COMBOS = 1326;

    // Cada combo son dos ids de carta {a, b} con a < b
    private final List<int[]> combos;

    private Rango(Set<Integer> claves) {
        List<int[]> lista = new ArrayList<>();
        for (int clave : claves) {
            lista.add(new int[] {clave / 64, clave % 64});
        }
        this.combos = Collections.unmodifiableList(lista);
    }

    public List<int[]> getCombos() {
        return combos;
    }

    public int size() {
        return combos.size();
    }

    public boolean esVacio() {
        return combos.isEmpty();
    }

    // Porcentaje de las 1326 manos posibles que contiene el rango
    public double porcentaje() {
        return 100.0 * combos.size() / TOTAL_COMBOS;
    }

    public List<Carta> primerCombo() {
        int[] c = combos.get(0);
        return new ArrayList<>(List.of(Cartas.carta(c[0]), Cartas.carta(c[1])));
    }


    // =========================================================
    // CONSTRUCCIÓN
    // =========================================================

    public static Rango todas() {
        Set<Integer> s = new LinkedHashSet<>();
        for (int a = 0; a < 52; a++) {
            for (int b = a + 1; b < 52; b++) {
                s.add(a * 64 + b);
            }
        }
        return new Rango(s);
    }

    // Una única mano concreta
    public static Rango deCartas(Carta c1, Carta c2) {
        Set<Integer> s = new LinkedHashSet<>();
        anadir(s, Cartas.id(c1), Cartas.id(c2));
        return new Rango(s);
    }

    // Texto -> rango. Lanza IllegalArgumentException si no es válido.
    public static Rango desdeTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El rango está vacío");
        }

        Set<Integer> s = new LinkedHashSet<>();
        for (String token : texto.trim().split("[\\s,;]+")) {
            procesarToken(token, s);
        }

        if (s.isEmpty()) {
            throw new IllegalArgumentException("El rango está vacío");
        }
        return new Rango(s);
    }

    // Las mejores manos según un ranking, hasta alcanzar el porcentaje pedido.
    // 'ranking' es la lista de clases ("AA", "AKs", "AKo"...) de mejor a peor.
    public static Rango desdePorcentaje(double porcentaje, List<String> ranking) {
        if (!(porcentaje > 0) || porcentaje > 100) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100");
        }

        double objetivo = porcentaje / 100.0 * TOTAL_COMBOS;
        Set<Integer> s = new LinkedHashSet<>();

        for (String clase : ranking) {
            if (s.size() >= objetivo - 1e-9) {
                break;
            }
            procesarToken(clase, s);
        }
        return new Rango(s);
    }


    // =========================================================
    // PARSEO
    // =========================================================

    private static final class Clase {
        int alta;
        int baja;
        char tipo; // 's', 'o' o ' ' (ambas / par)
    }

    private static IllegalArgumentException error(String token) {
        return new IllegalArgumentException("Token de rango no válido: \"" + token + "\"");
    }

    private static boolean esManoConcreta(String t) {
        return t.length() == 4
                && Cartas.RANGOS.indexOf(Character.toUpperCase(t.charAt(0))) >= 0
                && Cartas.PALOS.indexOf(Character.toLowerCase(t.charAt(1))) >= 0
                && Cartas.RANGOS.indexOf(Character.toUpperCase(t.charAt(2))) >= 0
                && Cartas.PALOS.indexOf(Character.toLowerCase(t.charAt(3))) >= 0;
    }

    private static Clase parseClase(String t, String original) {
        if (t.length() < 2 || t.length() > 3) {
            throw error(original);
        }

        int a = Cartas.RANGOS.indexOf(Character.toUpperCase(t.charAt(0)));
        int b = Cartas.RANGOS.indexOf(Character.toUpperCase(t.charAt(1)));
        if (a < 0 || b < 0) {
            throw error(original);
        }

        char tipo = ' ';
        if (t.length() == 3) {
            tipo = Character.toLowerCase(t.charAt(2));
            if (tipo != 's' && tipo != 'o') {
                throw error(original);
            }
        }

        Clase k = new Clase();
        k.alta = Math.max(a, b);
        k.baja = Math.min(a, b);
        k.tipo = tipo;

        if (k.alta == k.baja && tipo != ' ') {
            throw error(original);
        }
        return k;
    }

    private static void procesarToken(String token, Set<Integer> out) {
        String t = token.trim();
        if (t.isEmpty()) {
            return;
        }

        // Mano concreta, p.ej. AhKd
        if (esManoConcreta(t)) {
            List<Carta> cartas = Cartas.parsear(t);
            anadir(out, Cartas.id(cartas.get(0)), Cartas.id(cartas.get(1)));
            return;
        }

        // Rango con guion, p.ej. A5s-A2s o 22-55
        int guion = t.indexOf('-');
        if (guion > 0) {
            Clase x = parseClase(t.substring(0, guion), token);
            Clase y = parseClase(t.substring(guion + 1), token);
            expandirGuion(x, y, out, token);
            return;
        }

        // Con '+', p.ej. 77+ o ATs+
        if (t.endsWith("+")) {
            Clase k = parseClase(t.substring(0, t.length() - 1), token);
            if (k.alta == k.baja) {
                for (int r = k.alta; r <= 12; r++) {
                    anadirClase(out, r, r, ' ');
                }
            } else {
                for (int baja = k.baja; baja < k.alta; baja++) {
                    anadirClase(out, k.alta, baja, k.tipo);
                }
            }
            return;
        }

        // Clase simple
        Clase k = parseClase(t, token);
        anadirClase(out, k.alta, k.baja, k.tipo);
    }

    private static void expandirGuion(Clase x, Clase y, Set<Integer> out, String token) {
        boolean xPar = x.alta == x.baja;
        boolean yPar = y.alta == y.baja;

        if (xPar != yPar || x.tipo != y.tipo) {
            throw error(token);
        }

        if (xPar) {
            for (int r = Math.min(x.alta, y.alta); r <= Math.max(x.alta, y.alta); r++) {
                anadirClase(out, r, r, ' ');
            }
            return;
        }

        if (x.alta == y.alta) {
            // Misma carta alta: varía la baja (A5s-A2s)
            for (int baja = Math.min(x.baja, y.baja); baja <= Math.max(x.baja, y.baja); baja++) {
                anadirClase(out, x.alta, baja, x.tipo);
            }
        } else if (x.alta - x.baja == y.alta - y.baja) {
            // Mismo hueco: se desplaza el conjunto (T9s-54s)
            int hueco = x.alta - x.baja;
            for (int alta = Math.min(x.alta, y.alta); alta <= Math.max(x.alta, y.alta); alta++) {
                anadirClase(out, alta, alta - hueco, x.tipo);
            }
        } else {
            throw error(token);
        }
    }

    private static void anadirClase(Set<Integer> out, int alta, int baja, char tipo) {
        if (alta == baja) {
            for (int s1 = 0; s1 < 4; s1++) {
                for (int s2 = s1 + 1; s2 < 4; s2++) {
                    anadir(out, alta * 4 + s1, alta * 4 + s2);
                }
            }
            return;
        }

        for (int s1 = 0; s1 < 4; s1++) {
            for (int s2 = 0; s2 < 4; s2++) {
                boolean suited = s1 == s2;
                if (tipo == 's' && !suited) continue;
                if (tipo == 'o' && suited) continue;
                anadir(out, alta * 4 + s1, baja * 4 + s2);
            }
        }
    }

    private static void anadir(Set<Integer> out, int x, int y) {
        int a = Math.min(x, y);
        int b = Math.max(x, y);
        out.add(a * 64 + b);
    }
}
