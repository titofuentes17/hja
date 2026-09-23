package CalculadoraValor;

import java.util.List;

public class ValorMano {

    // Categoría de la mano:
    // 1 = Carta alta
    // 2 = Pareja
    // 3 = Doble pareja
    // 4 = Trío
    // 5 = Escalera
    // 6 = Color
    // 7 = Full House
    // 8 = Póker
    // 9 = Escalera de color
    // 10 = Royal Flush
	
    private int categoria;

    // Las 5 cartas que forman la mano
    private List<Carta> cartas;

    // Valores utilizados para desempatar
    private List<Integer> desempate;


    // Constructor
    public ValorMano(int categoria, List<Carta> cartas,
                     List<Integer> desempate) {

        this.categoria = categoria;
        this.cartas = cartas;
        this.desempate = desempate;
    }


    public int getCategoria() {
        return categoria;
    }

    public List<Carta> getCartas() {
        return cartas;
    }

    public List<Integer> getDesempate() {
        return desempate;
    }
    
    public int compararCon(ValorMano otra) {

        // Primero comparamos la categoría
        if (this.categoria != otra.categoria) {
            return Integer.compare(
                    this.categoria,
                    otra.categoria
            );
        }

        // Si tienen la misma categoría,
        // comparamos los valores de desempate
        for (int i = 0; i < this.desempate.size(); i++) {

            int comparacion = Integer.compare(
                    this.desempate.get(i),
                    otra.desempate.get(i)
            );

            // Si encontramos una diferencia,
            // ya sabemos cuál de las dos manos es mejor
            if (comparacion != 0) {
                return comparacion;
            }
        }

        // Las dos manos tienen exactamente el mismo valor
        return 0;
    }
}