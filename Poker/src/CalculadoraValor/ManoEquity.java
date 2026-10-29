package CalculadoraValor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Estado de seis jugadores; el board futuro se reserva pero nunca se filtra al cálculo.
 // Cada jugador tiene 2 cartas (Hold'em) o 4 (Omaha).


//Guarda el estado de una mano: cartas de jugadores, comunitarias y calle actual

public final class ManoEquity {
	
	
    public static final int JUGADORES = 6;
    private final List<List<Carta>> manos;
    private final int cartasPorJugador;
    private final List<Carta> boardCompleto;
    private Calle calle = Calle.PREFLOP;
    private final boolean[] retirados = new boolean[JUGADORES];//Indica jugadores que han hecho fold

    public ManoEquity(List<List<Carta>> manos, List<Carta> boardCompleto) {
        if (manos.size() != JUGADORES || boardCompleto.size() != 5) {
            throw new IllegalArgumentException("Se necesitan seis manos y cinco comunitarias reservadas.");
        }
        cartasPorJugador = manos.get(0) == null ? 0 : manos.get(0).size();
        if (cartasPorJugador != 2 && cartasPorJugador != 4) {
            throw new IllegalArgumentException("Cada jugador debe tener 2 cartas (Hold'em) o 4 (Omaha).");
        }
        Set<Integer> usadas = new HashSet<>();//Comprobamos que no esten usadas
        List<List<Carta>> copia = new ArrayList<>();
        for (List<Carta> mano : manos) {
            if (mano == null || mano.size() != cartasPorJugador) {
                throw new IllegalArgumentException("Todos los jugadores deben tener " + cartasPorJugador + " cartas.");
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

    public boolean isOmaha() {
        return cartasPorJugador == 4;
    }

    public Calle getCalle() {
        return calle;
    }

    public List<Carta> getBoardVisible() {
        return List.copyOf(boardCompleto.subList(0, calle.getComunitarias()));
    }

    public boolean puedeAvanzar() {
        return calle != Calle.RIVER && getJugadoresEnMano().size() > 1;
    }

    public boolean estaRetirado(int indice) {
        return retirados[indice];
    }

    // Índices de asiento estables: retirar a alguien no desplaza al resto
    
    public List<Integer> getJugadoresEnMano() {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < JUGADORES; i++) if (!retirados[i]) indices.add(i);
        return List.copyOf(indices);
    }

    // Sus cartas siguen siendo conocidas y nunca vuelven al mazo
    public List<Carta> getCartasRetiradas() {
        List<Carta> cartas = new ArrayList<>();
        for (int i = 0; i < JUGADORES; i++) if (retirados[i]) cartas.addAll(manos.get(i));
        return List.copyOf(cartas);
    }

    public void retirar(int indice) {
        if (indice < 0 || indice >= JUGADORES) throw new IllegalArgumentException("Asiento no válido.");
        
        if (retirados[indice]) throw new IllegalStateException("El jugador ya ha hecho Fold.");
        
        if (!puedeAvanzar()) throw new IllegalStateException("La mano ya ha finalizado.");
        
        retirados[indice] = true;
    }

    // Devuelve exclusivamente las cartas que se acaban de revelar
    
    public List<Carta> avanzar() {
        if (!puedeAvanzar()) throw new IllegalStateException("La mano ya ha finalizado.");
        int anteriores = calle.getComunitarias();
        
        calle = calle.siguiente();
        return List.copyOf(boardCompleto.subList(anteriores, calle.getComunitarias()));
    }

    // Solo jugadores en mano, en el orden de getJugadoresEnMano()
    
    public List<Rango> getRangosConcretos() {
        return getJugadoresEnMano().stream().map(manos::get)
                .map(m -> Rango.deCartas(m.get(0), m.get(1))).toList();
    }
}
