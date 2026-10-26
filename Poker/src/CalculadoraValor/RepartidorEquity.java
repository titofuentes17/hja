package CalculadoraValor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


public final class RepartidorEquity {
    private RepartidorEquity() { }

    // cartasPorJugador: 2 en Hold'em, 4 en Omaha
    public static ManoEquity nuevaMano(List<List<Carta>> manuales, List<Rango> rangos,
            List<Carta> boardReservado, boolean dentroDeRango, int cartasPorJugador, Random rnd) {
        if (manuales.size() != ManoEquity.JUGADORES || rangos.size() != ManoEquity.JUGADORES) {
            throw new IllegalArgumentException("La mano debe comenzar con seis jugadores.");
        }
        int n = boardReservado.size();
        if (n != 0 && n != 3 && n != 4 && n != 5) {
            throw new IllegalArgumentException("El board manual debe tener 0, 3, 4 o 5 cartas.");
        }
        Set<Integer> fijas = new HashSet<>();
        ManoEquity.validar(boardReservado, fijas);
        for (List<Carta> mano : manuales) {
            if (mano != null) {
                if (mano.size() != cartasPorJugador) throw new IllegalArgumentException(
                        "Escribe exactamente " + cartasPorJugador + " cartas por jugador.");
                ManoEquity.validar(mano, fijas);
            }
        }

        // Filtrar una sola vez. Reintentar el reparto completo evita que la elección
        // de un rango bloquee permanentemente otro rango compatible.
        List<List<int[]>> opciones = new ArrayList<>();
        for (int i = 0; i < manuales.size(); i++) {
            List<int[]> libres = new ArrayList<>();
            Rango rango = rangos.get(i);
            if (manuales.get(i) == null && dentroDeRango && rango != null) {
                for (int[] combo : rango.getCombos()) {
                    if (!fijas.contains(combo[0]) && !fijas.contains(combo[1])) libres.add(combo);
                }
                if (libres.isEmpty()) {
                    throw new IllegalArgumentException("El rango del jugador " + (i + 1)
                            + " no es compatible con las cartas manuales o el board reservado.");
                }
            }
            opciones.add(libres);
        }

        for (int intento = 0; intento < 100_000; intento++) {
            if (Thread.currentThread().isInterrupted()) throw new java.util.concurrent.CancellationException();
            Set<Integer> usadas = new HashSet<>(fijas);
            List<List<Carta>> manos = new ArrayList<>(manuales);
            boolean compatible = true;
            for (int i = 0; i < manos.size(); i++) {
                List<int[]> lista = opciones.get(i);
                if (lista.isEmpty()) continue;
                int[] combo = lista.get(rnd.nextInt(lista.size()));
                if (usadas.contains(combo[0]) || usadas.contains(combo[1])) {
                    compatible = false;
                    break;
                }
                usadas.add(combo[0]);
                usadas.add(combo[1]);
                manos.set(i, List.of(Cartas.carta(combo[0]), Cartas.carta(combo[1])));
            }
            if (!compatible) continue;

            List<Carta> mazo = new ArrayList<>();
            for (int id = 0; id < 52; id++) if (!usadas.contains(id)) mazo.add(Cartas.carta(id));
            Collections.shuffle(mazo, rnd);
            int siguiente = 0;
            for (int i = 0; i < manos.size(); i++) {
                if (manos.get(i) == null) {
                    manos.set(i, List.copyOf(mazo.subList(siguiente, siguiente + cartasPorJugador)));
                    siguiente += cartasPorJugador;
                }
            }
            List<Carta> board = new ArrayList<>(boardReservado);
            while (board.size() < 5) board.add(mazo.get(siguiente++));
            return new ManoEquity(manos, board);
        }
        throw new IllegalArgumentException("No se encontró un reparto compatible con los rangos. "
                + "Revisa los rangos o vuelve a intentarlo.");
    }
}
