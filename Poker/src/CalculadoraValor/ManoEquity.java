package CalculadoraValor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Estado de seis jugadores; el board futuro se reserva pero nunca se filtra al cálculo. */
public final class ManoEquity {
    public static final int JUGADORES = 6;
    private final List<List<Carta>> manos;
    private final List<Carta> boardCompleto;
    private Calle calle = Calle.PREFLOP;

    public ManoEquity(List<List<Carta>> manos, List<Carta> boardCompleto) {
        if (manos.size() != JUGADORES || boardCompleto.size() != 5) {
            throw new IllegalArgumentException("Se necesitan seis manos y cinco comunitarias reservadas.");
        }
        Set<Integer> usadas = new HashSet<>();
        List<List<Carta>> copia = new ArrayList<>();
        for (List<Carta> mano : manos) {
            if (mano == null || mano.size() != 2) {
                throw new IllegalArgumentException("Cada jugador debe tener exactamente dos cartas.");
            }
            validar(mano, usadas);
            copia.add(List.copyOf(mano));
        }
        validar(boardCompleto, usadas);
        this.manos = List.copyOf(copia);
        this.boardCompleto = List.copyOf(boardCompleto);
    }

    static void validar(List<Carta> cartas, Set<Integer> usadas) {
        for (Carta carta : cartas) {
            if (carta == null || Cartas.RANGOS.indexOf(carta.getValor()) < 0
                    || Cartas.PALOS.indexOf(carta.getPalo()) < 0) {
                throw new IllegalArgumentException("Carta no válida: " + carta);
            }
            if (!usadas.add(Cartas.id(carta))) {
                throw new IllegalArgumentException("Carta repetida: " + carta);
            }
        }
    }

    public List<List<Carta>> getManos() {
        return manos;
    }

    public Calle getCalle() {
        return calle;
    }

    public List<Carta> getBoardVisible() {
        return List.copyOf(boardCompleto.subList(0, calle.getComunitarias()));
    }

    public boolean puedeAvanzar() {
        return calle != Calle.RIVER;
    }

    /** Devuelve exclusivamente las cartas que se acaban de revelar. */
    public List<Carta> avanzar() {
        int anteriores = calle.getComunitarias();
        calle = calle.siguiente();
        return List.copyOf(boardCompleto.subList(anteriores, calle.getComunitarias()));
    }

    public List<Rango> getRangosConcretos() {
        return manos.stream().map(m -> Rango.deCartas(m.get(0), m.get(1))).toList();
    }
}
