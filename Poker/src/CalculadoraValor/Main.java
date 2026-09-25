package CalculadoraValor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
                    lineasSalida.add("- Best hand: " + mejorMano);
                    
                    List<String> draws = Evaluador.obtenerDraws(cartas);
                    for (String draw : draws) {
                        lineasSalida.add("- " + draw);
                    }
                    
                    lineasSalida.add("");
                }
            } 
        
            else if (apartado == 2) {
                for (String linea : lineasEntrada) {
                    // Formato: cartasJugador;numComunes;cartasComunes
                    String[] partes = linea.split(";");
                    List<Carta> propias = Utils.parsearCartas(partes[0]);
                    List<Carta> mesa = Utils.parsearCartas(partes[2]);

                    // El jugador se encarga de calcular su mejor mano y sus draws
                    Jugador jugador = new Jugador(propias, mesa);

                    lineasSalida.add(linea);
                    lineasSalida.add("- Best hand: " + jugador.getMejorManoTexto());
                    for (String draw : jugador.getDraws()) {
                        lineasSalida.add("- " + draw);
                    }

                    // Línea en blanco entre manos
                    lineasSalida.add("");
                }
                
                
                
                
                
                
                
                
                
            }
            
            
            else if (apartado == 3) {
                for (String linea : lineasEntrada) {
                    // Formato: N;J1CartasJug1;...;JNCartasJugN;CartasComunes
                    String[] partes = linea.split(";");
                    int numJugadores = Integer.parseInt(partes[0]);
                    List<Carta> mesa = Utils.parsearCartas(partes[partes.length - 1]);

                    // Cada jugador: el id es todo menos las 4 últimas letras (sus 2 cartas)
                    List<Jugador> jugadores = new ArrayList<>();
                    for (int i = 1; i <= numJugadores; i++) {
                        String parte = partes[i];
                        String id = parte.substring(0, parte.length() - 4);
                        List<Carta> propias = Utils.parsearCartas(parte.substring(parte.length() - 4));
                        jugadores.add(new Jugador(id, propias, mesa));
                    }

                    // Ordenamos de mejor a peor mano. En caso de empate se
                    // mantiene el orden de entrada (sort es estable)
                    jugadores.sort(Collections.reverseOrder());

                    lineasSalida.add(linea);
                    for (Jugador j : jugadores) {
                        lineasSalida.add(j.getId() + ": "
                                + Utils.cartasAString(j.getMejorMano().getCartas())
                                + " (" + j.getNombreMano() + ")");
                    }

                    // Línea en blanco entre manos
                    lineasSalida.add("");
                }
            }
            
            else if (apartado == 4) {
            	System.out.println("El apartado " + apartado + " aún no está implementado.");
                return;
            	
            	
            }
            
            
            
            else if (apartado == 5) {
            	System.out.println("El apartado " + apartado + " aún no está implementado.");
                return;
            	
            	
            }
            
            
            else {
                System.out.println("El aparatado no existe");
                return;
            }

            Utils.escribirFichero(ficheroSalida, lineasSalida);
            System.out.println("Proceso finalizado correctamente.");

        } catch (IOException e) {
            System.err.println("Error al procesar los ficheros: " + e.getMessage());
        }
    }
}