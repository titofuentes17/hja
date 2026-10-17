package CalculadoraValor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Utilidades para pasar de Carta a un identificador numérico (0..51) y para
// validar texto introducido por el usuario (p.ej. "AhKd" o "Ts9h2c").
// id = indiceRango * 4 + indicePalo   (rango: 0 = '2' ... 12 = 'A')
public final class Cartas {

    public static final String RANGOS = "23456789TJQKA";
    public static final String PALOS = "cdhs";

    private Cartas() {}

    public static int id(Carta c) {
        return RANGOS.indexOf(c.getValor()) * 4 + PALOS.indexOf(c.getPalo());
    }

    public static Carta carta(int id) {
        return new Carta(RANGOS.charAt(id >> 2), PALOS.charAt(id & 3));
    }

    // Convierte texto en cartas validando formato y repeticiones.
    // Admite mayúsculas/minúsculas y separadores (espacios, comas o ;).
    // Lanza IllegalArgumentException con un mensaje apto para el usuario.
    public static List<Carta> parsear(String texto) {
        String t = texto == null ? "" : texto.replaceAll("[\\s,;]", "");

        if (t.length() % 2 != 0) {
            throw new IllegalArgumentException("Texto de cartas incompleto: \"" + t + "\"");
        }

        List<Carta> resultado = new ArrayList<>();
        Set<Integer> vistos = new HashSet<>();

        for (int i = 0; i < t.length(); i += 2) {
            char v = Character.toUpperCase(t.charAt(i));
            char p = Character.toLowerCase(t.charAt(i + 1));

            if (RANGOS.indexOf(v) < 0 || PALOS.indexOf(p) < 0) {
                throw new IllegalArgumentException("Carta no válida: \"" + t.substring(i, i + 2) + "\"");
            }

            Carta c = new Carta(v, p);
            if (!vistos.add(id(c))) {
                throw new IllegalArgumentException("Carta repetida: " + c);
            }
            resultado.add(c);
        }

        return resultado;
    }
}
