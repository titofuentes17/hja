package CalculadoraValor;

import java.util.ArrayList;
import java.util.List;

// Representa a un jugador con sus cartas propias y las cartas comunes de la mesa
public class Jugador {

	private String id;             // Identificador del jugador (J1, J2...). Puede ser null
	private List<Carta> propias;    // Cartas propias del jugador
	private List<Carta> mesa;       // Cartas comunes (3, 4 o 5)

	// Se calcula una sola vez, la primera vez que se pide
	private ValorMano mejorMano;


	// Constructor sin identificador (apartado 2)
	public Jugador(List<Carta> propias, List<Carta> mesa) {
		this(null, propias, mesa);
	}

	// Constructor con identificador (para cuando haya varios jugadores)
	public Jugador(String id, List<Carta> propias, List<Carta> mesa) {
		this.id = id;
		this.propias = propias;
		this.mesa = mesa;
	}


	//Getters
	public String getId() {return id;}
	public List<Carta> getPropias() {return propias;}
	public List<Carta> getMesa() {return mesa;}


	// Todas las cartas que puede usar el jugador: las suyas + las de la mesa
	public List<Carta> getDisponibles() {
		List<Carta> disponibles = new ArrayList<>(propias);
		disponibles.addAll(mesa);
		return disponibles;
	}


	// Devuelve la mejor mano de 5 cartas que puede formar el jugador
	public ValorMano getMejorMano() {
		if (mejorMano == null) {
			mejorMano = calcularMejorMano();
		}
		return mejorMano;
	}

	// Prueba todas las combinaciones de 5 cartas y se queda con la mejor
	private ValorMano calcularMejorMano() {
		ValorMano mejor = null;

		for (List<Carta> combinacion : Utils.generarCombinaciones5(getDisponibles())) {
			ValorMano actual = Evaluador.evaluarMano(combinacion);

			if (mejor == null || actual.compararCon(mejor) > 0) {
				mejor = actual;
			}
		}
		return mejor;
	}


	// Devuelve los draws posibles (vacío si ya están las 5 cartas comunes)
	public List<String> getDraws() {
		return Evaluador.obtenerDrawsApartado2(getDisponibles(), mesa.size());
	}


	// Texto de la mejor mano, p.ej. "Pair of Aces with AhAcQhJhTh"
	public String getMejorManoTexto() {
		return Evaluador.obtenerMejorManoTexto(getMejorMano().getCartas(), true);
	}

}
