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
            } 
        
            else if (apartado == 2) {
                for (String linea : lineasEntrada) {
                    // Formato: cartasJugador;numComunes;cartasComunes
                    String[] partes = linea.split(";");
                    List<Carta> propias = Utils.parsearCartas(partes[0]);
                    List<Carta> mesa = Utils.parsearCartas(partes[2]);

                    // El jugador se encarga de calcular su mejor mano y sus draws
                    Jugador jugador = new Jugador(propias, mesa);

<<<<<<< HEAD
=======
                    // Convertimos los Strings en listas de Carta
                    List<Carta> jugador =
                            Utils.parsearCartas(cartasJugador);

                    List<Carta> comunes =
                            Utils.parsearCartas(cartasComunes);


                    // Creamos una lista con todas las cartas disponibles
                    List<Carta> disponibles = new ArrayList<>();

                    // Añadimos las dos cartas del jugador
                    disponibles.addAll(jugador);

                    // Añadimos las cartas de la mesa
                    disponibles.addAll(comunes);


                    // Generamos todas las combinaciones posibles de 5 cartas
                    List<List<Carta>> combinaciones =
                            Utils.generarCombinaciones5(disponibles);


                    // Buscamos la mejor combinación
                    ValorMano mejorMano = null;

                    for (List<Carta> combinacion : combinaciones) {

                        ValorMano manoActual =
                                Evaluador.evaluarMano(combinacion);

                        if (mejorMano == null) {

                            mejorMano = manoActual;

                        } else if (manoActual.compararCon(mejorMano) > 0) {

                            mejorMano = manoActual;
                        }
                    }
                    

                    

                    // Buscamos los posibles draws
                    List<String> draws =
                            Evaluador.obtenerDrawsApartado2(
                                    disponibles,
                                    numComunes
                            );


                    // -----------------------------------------
                    // GUARDAMOS EL RESULTADO EN EL FICHERO
                    // -----------------------------------------

                    // Añadimos la línea original
>>>>>>> 83a34e4dad2ac9984a857a68af1c313dfad25178
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