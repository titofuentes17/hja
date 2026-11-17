package CalculadoraValor;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

// Rondas de apuestas simplificadas de NLHE (práctica 3, apartado 2.1).
//
//  - Acciones posibles:
//      * Bet:  3 BB. Solo si no hay una apuesta previa en la calle actual.
//      * Call: iguala la apuesta previa.
//      * Fold: el jugador se retira (sus cartas quedan fuera del mazo).
//  - Al empezar la mano se ponen las ciegas (SB 0,5 BB y BB 1 BB). No cuentan
//    como apuesta previa: en preflop el primero que entra hace Bet.
//  - En preflop habla primero UTG; en el resto de calles, el primer jugador
//    en mano empezando por la Small Blind.
//  - Cada jugador habla una vez por calle (no hay subidas).
//
// Guarda el bote y el turno; las cartas y los Fold los lleva la ManoEquity.
public final class RondaApuestas {

    public enum Accion { BET, CALL, FOLD }

    // Cantidades en ciegas grandes (BB)
    public static final double CIEGA_PEQUENA = 0.5;
    public static final double CIEGA_GRANDE = 1;
    public static final double APUESTA = 3;

    // Posición de cada asiento de la mesa (los asientos van en sentido horario)
    public static final String[] POSICIONES = {
        "Highjack", "Cut-off", "Button", "Small Blind", "Big Blind", "UTG"
    };
    public static final int SMALL_BLIND = 3;
    public static final int BIG_BLIND = 4;
    public static final int UTG = 5;

    private final ManoEquity mano;
    private final Deque<Integer> pendientes = new ArrayDeque<>();   // asientos que faltan por hablar
    private final double[] apuestaCalle = new double[ManoEquity.JUGADORES];   // lo puesto en esta calle
    private double bote;
    private boolean hayApuesta;

    public RondaApuestas(ManoEquity mano) {
        this.mano = mano;
        empezarCalle();

        // Ciegas
        apuestaCalle[SMALL_BLIND] = CIEGA_PEQUENA;
        apuestaCalle[BIG_BLIND] = CIEGA_GRANDE;
        bote = CIEGA_PEQUENA + CIEGA_GRANDE;
    }

    // Prepara el orden de la calle actual de la mano. Se llama después de mano.avanzar()
    public void empezarCalle() {
        pendientes.clear();
        Arrays.fill(apuestaCalle, 0);
        hayApuesta = false;

        int primero = mano.getCalle() == Calle.PREFLOP ? UTG : SMALL_BLIND;
        for (int i = 0; i < ManoEquity.JUGADORES; i++) {
            int asiento = (primero + i) % ManoEquity.JUGADORES;
            if (!mano.estaRetirado(asiento)) pendientes.add(asiento);
        }
    }

    // Asiento al que le toca hablar, o -1 si la calle ya ha terminado
    public int getTurno() {
        return calleTerminada() ? -1 : pendientes.peekFirst();
    }

    // Ya han hablado todos, o solo queda un jugador en la mano
    public boolean calleTerminada() {
        return pendientes.isEmpty() || mano.getJugadoresEnMano().size() <= 1;
    }

    // Solo queda un jugador, o ya han hablado todos en el river
    public boolean manoTerminada() {
        return mano.getJugadoresEnMano().size() <= 1
                || (mano.getCalle() == Calle.RIVER && pendientes.isEmpty());
    }

    // El Bet solo se permite si no hay una apuesta previa en la calle
    public boolean puedeBet() {
        return !calleTerminada() && !hayApuesta;
    }

    public boolean hayApuesta() {
        return hayApuesta;
    }

    public double getBote() {
        return bote;
    }

    // Decisión automática, tal y como la describe el enunciado:
    //   - Fold si la mano no está en el rango o el equity no llega al EM
    //   - si no, Bet (no hay apuesta previa) o Call (sí la hay)
    public Accion decidir(boolean enRango, double equity, double equityMinimo) {
        if (!enRango || equity < equityMinimo) return Accion.FOLD;
        return hayApuesta ? Accion.CALL : Accion.BET;
    }

    // Aplica la acción del jugador que tiene el turno.
    // Devuelve lo que ese jugador pone en el bote (en BB).
    public double aplicar(Accion accion) {
        if (calleTerminada()) throw new IllegalStateException("No le toca hablar a nadie.");
        int asiento = pendientes.peekFirst();
        double pone = 0;

        switch (accion) {
            case BET -> {
                if (hayApuesta) throw new IllegalStateException("Ya hay una apuesta en esta calle.");
                pone = igualar(asiento);
                hayApuesta = true;
            }
            case CALL -> {
                if (!hayApuesta) throw new IllegalStateException("No hay ninguna apuesta que igualar.");
                pone = igualar(asiento);
            }
            case FOLD -> mano.retirar(asiento);
        }

        pendientes.removeFirst();
        return pone;
    }

    // El jugador completa la apuesta de la calle (3 BB). Las ciegas ya puestas
    // cuentan: la SB añade 2,5 BB y la BB añade 2 BB.
    private double igualar(int asiento) {
        double pone = APUESTA - apuestaCalle[asiento];
        apuestaCalle[asiento] = APUESTA;
        bote += pone;
        return pone;
    }
}
