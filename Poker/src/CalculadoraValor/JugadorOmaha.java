package CalculadoraValor;

import java.util.ArrayList;
import java.util.List;

// Representa a un jugador en la modalidad Omaha.
// Tiene 4 cartas propias y debe usar obligatoriamente 2 de ellas y 3 de la mesa.
public class JugadorOmaha implements Comparable<JugadorOmaha> {

    private String id;
    private List<Carta> propias;
    private List<Carta> mesa;

    private ValorMano mejorMano;
    private List<Carta> mejorCombinacion;

    // Constructor sin id
    public JugadorOmaha(List<Carta> propias, List<Carta> mesa) {
        this(null, propias, mesa);
    }

    // Constructor con id
    public JugadorOmaha(String id, List<Carta> propias, List<Carta> mesa) {
        this.id = id;
        this.propias = propias;
        this.mesa = mesa;
    }

    // Getters
    public String getId() {
        return id;
    }

    public List<Carta> getPropias() {
        return propias;
    }

    public List<Carta> getMesa() {
        return mesa;
    }

    // Devuelve la mejor mano de Omaha
    public ValorMano getMejorMano() {
        if (mejorMano == null) {
            calcularMejorMano();
        }
        return mejorMano;
    }

    public List<Carta> getMejorCombinacion() {
        if (mejorCombinacion == null) {
            calcularMejorMano();
        }
        return mejorCombinacion;
    }

    // Prueba todas las combinaciones válidas de Omaha y se queda con la mejor
    private void calcularMejorMano() {
        List<List<Carta>> combinaciones = EvaluadorOmaha.generarCombinacionesOmaha(propias, mesa);
        ValorMano mejor = null;
        List<Carta> mejorComb = null;

        for (List<Carta> comb : combinaciones) {
            // Pasamos copia porque evaluarMano modifica el orden de la lista
            ValorMano actual = Evaluador.evaluarMano(new ArrayList<>(comb));

            if (mejor == null || actual.compararCon(mejor) > 0) {
                mejor = actual;
                mejorComb = comb;
            }
        }

        this.mejorMano = mejor;
        this.mejorCombinacion = mejorComb;
    }

    // Devuelve el texto con el nombre de la mano y las cartas usadas
    public String getMejorManoTexto() {
        return EvaluadorOmaha.formatearMejorMano(getMejorMano(), getMejorCombinacion());
    }

    // Devuelve solo el nombre de la jugada
    public String getNombreMano() {
        return Evaluador.obtenerNombreMano(new ArrayList<>(getMejorCombinacion()));
    }

    // Devuelve los proyectos posibles
    public List<String> getDraws() {
        return EvaluadorOmaha.obtenerDrawsOmaha(propias, mesa, getMejorMano().getCategoria());
    }

    @Override
    public int compareTo(JugadorOmaha otro) {
        return getMejorMano().compararCon(otro.getMejorMano());
    }
}
