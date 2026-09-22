package CalculadoraValor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: java -jar Proyecto.jar <apartado> <fichero_entrada> <fichero_salida>");
            return;
        }

        int apartado = Integer.parseInt(args[0]);
        String ficheroEntrada = args[1];
        String ficheroSalida = args[2];

        try {
            List<String> lineasEntrada = Utils.leerFichero(ficheroEntrada);
            List<String> lineasSalida = new ArrayList<>();

            if (apartado == 1) {
                for (String linea : lineasEntrada) {
                    if (linea.length() < 10) continue;
                    
                    lineasSalida.add(linea);
                    List<Carta> cartas = Utils.parsearCartas(linea);
                    
                    String mejorMano = Evaluador.obtenerMejorManoTexto(cartas);
                    lineasSalida.add("Best hand: " + mejorMano);
                    
                    List<String> draws = Evaluador.obtenerDraws(cartas);
                    for (String draw : draws) {
                        lineasSalida.add(draw);
                    }
                    
                    lineasSalida.add("");
                }
            } else {
                System.out.println("El apartado " + apartado + " aún no está implementado.");
                return;
            }

            Utils.escribirFichero(ficheroSalida, lineasSalida);
            System.out.println("Proceso finalizado correctamente.");

        } catch (IOException e) {
            System.err.println("Error al procesar los ficheros: " + e.getMessage());
        }
    }
}