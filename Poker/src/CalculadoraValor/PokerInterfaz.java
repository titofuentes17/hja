package CalculadoraValor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PokerInterfaz extends Application {

    // Variables que mantienen el estado y los elementos principales de la interfaz.
    private int indiceManoActual = 0;
    private MesaPokerVista mesaPoker;
    private CrupierVista crupier;
    private JugadorVista jugadorVistaApartado2;
    private JugadorVista jugadorVistaApartado4;
    private List<JugadorVista> jugadoresVistaApartado3 = new ArrayList<>();

    // Construye la ventana principal y configura todos los controles.
    @Override
    public void start(Stage escenario) {
        // Fondo general de la aplicación.
        StackPane fondoGeneral = new StackPane();

        fondoGeneral.setStyle("-fx-background-color: " + "linear-gradient(to bottom, #071A14, #0B2B20, #07110E);");

        // Contenedor principal de la interfaz.
        BorderPane raiz = new BorderPane();

        raiz.setPadding(new Insets(8));

        raiz.setStyle("-fx-background-color: transparent;");

        // Título y selector del apartado que se quiere visualizar.
        Label titulo = new Label("♠  CALCULADORA DE PÓKER  ♥");

        titulo.setStyle("-fx-font-size: 21px;" + "-fx-font-weight: bold;" + "-fx-text-fill: #F5F5F5;");

        ComboBox<String> selectorApartado = new ComboBox<>();

        selectorApartado.getItems().addAll("Apartado 1 - Mano de 5 cartas", "Apartado 2 - Texas Hold'em", "Apartado 3 - Varios jugadores", "Apartado 4 - Omaha");

        selectorApartado.setValue("Apartado 1 - Mano de 5 cartas");

        // Botón para cargar un fichero de entrada distinto al fichero de prueba.
        Button botonFichero = new Button("Seleccionar fichero");

        botonFichero.setStyle("-fx-background-color: #153C2D;" + "-fx-text-fill: white;" + "-fx-font-weight: bold;" + "-fx-border-color: #C9A227;" + "-fx-border-width: 1;" + "-fx-border-radius: 6;" + "-fx-background-radius: 6;" + "-fx-padding: 5 12 5 12;" + "-fx-cursor: hand;");

        HBox opciones = new HBox(10, selectorApartado, botonFichero);

        opciones.setAlignment(Pos.CENTER);

        VBox parteSuperior = new VBox(6, titulo, opciones);

        parteSuperior.setAlignment(Pos.CENTER);

        raiz.setTop( parteSuperior);

        // Elementos principales de la mesa.

        // Elementos visuales de la mesa, el crupier y los jugadores.
        crupier = new CrupierVista();

        CartaVista mazo = crupier.getMazo();

        mesaPoker = new MesaPokerVista();

        jugadorVistaApartado2 = new JugadorVista("TÚ");

        Label mejorManoApartado1 = new Label();

        mejorManoApartado1.setStyle("-fx-font-size: 14px;" + "-fx-font-weight: bold;" + "-fx-text-fill: #D6B85A;");

        mejorManoApartado1.setVisible(false);

        mejorManoApartado1.setManaged(false);

        Label mejorManoApartado2 = new Label();

        mejorManoApartado2.setStyle("-fx-font-size: 14px;" + "-fx-font-weight: bold;" + "-fx-text-fill: #D6B85A;");

        mejorManoApartado2.setVisible(false);

        mejorManoApartado2.setManaged(false);

        Label mejorManoApartado4 = new Label();

        mejorManoApartado4.setStyle("-fx-font-size: 14px;-fx-font-weight: bold;-fx-text-fill: #D6B85A;");

        mejorManoApartado4.setVisible(false);

        mejorManoApartado4.setManaged(false);

        Label contadorManos = new Label("Mano 1");

        contadorManos.setStyle("-fx-font-size: 12px;" + "-fx-font-weight: bold;" + "-fx-text-fill: #E2E2E2;");

        Button botonRepartir = new Button("▶  REPARTIR");

        botonRepartir.setStyle("-fx-background-color: #C9A227;" + "-fx-text-fill: #111111;" + "-fx-font-weight: bold;" + "-fx-font-size: 13px;" + "-fx-background-radius: 8;" + "-fx-padding: 7 22 7 22;" + "-fx-cursor: hand;");

        VBox zonaJuego = new VBox(2);

        zonaJuego.setAlignment(Pos.CENTER);

        raiz.setCenter( zonaJuego);

        Label tituloEntrada = new Label("FICHERO DE ENTRADA");

        tituloEntrada.setStyle("-fx-font-size: 12px;" + "-fx-font-weight: bold;" + "-fx-text-fill: #D6B85A;");

        TextArea entrada = new TextArea();

        entrada.setEditable(false);

        entrada.setPrefHeight(80);

        entrada.setPrefWidth(410);

        entrada.setStyle("-fx-control-inner-background: #F4F4F4;" + "-fx-font-family: 'Consolas';" + "-fx-font-size: 12px;" + "-fx-background-radius: 6;");

        VBox zonaEntrada = new VBox( 5, tituloEntrada, entrada);

        zonaEntrada.setPadding(new Insets(8));

        zonaEntrada.setStyle("-fx-background-color: rgba(5,18,14,0.92);" + "-fx-border-color: #C9A227;" + "-fx-border-width: 2;" + "-fx-border-radius: 10;" + "-fx-background-radius: 10;");

        Label tituloResultado = new Label("RESULTADO");

        tituloResultado.setStyle("-fx-font-size: 12px;" + "-fx-font-weight: bold;" + "-fx-text-fill: #D6B85A;");

        TextArea resultado = new TextArea();

        resultado.setEditable(false);

        resultado.setPrefHeight(80);

        resultado.setPrefWidth(410);

        resultado.setStyle("-fx-control-inner-background: #F4F4F4;" + "-fx-font-family: 'Consolas';" + "-fx-font-size: 12px;" + "-fx-background-radius: 6;");

        VBox zonaResultado = new VBox( 5, tituloResultado, resultado);

        zonaResultado.setPadding(new Insets(8));

        zonaResultado.setStyle("-fx-background-color: rgba(5,18,14,0.92);" + "-fx-border-color: #C9A227;" + "-fx-border-width: 2;" + "-fx-border-radius: 10;" + "-fx-background-radius: 10;");

        HBox parteInferior = new HBox( 12, zonaEntrada, zonaResultado);

        parteInferior.setAlignment(Pos.CENTER);

        parteInferior.setPadding(new Insets( 6, 0, 0, 0));

        raiz.setBottom( parteInferior);

        fondoGeneral.getChildren().add(raiz);

        mostrarApartado1(zonaJuego, mejorManoApartado1, contadorManos, botonRepartir);

        cargarFichero("test1.txt", entrada, resultado, contadorManos);

        // Cambia la mesa y carga el fichero correspondiente al apartado seleccionado.

        selectorApartado.setOnAction(evento -> {
            indiceManoActual = 0;

            resultado.clear();

            ocultarResultadoVisual(mejorManoApartado1);

            ocultarResultadoVisual(mejorManoApartado2);

            ocultarResultadoVisual(mejorManoApartado4);

            String apartado = selectorApartado.getValue();

            if (apartado.startsWith("Apartado 1")) {
                mostrarApartado1(zonaJuego, mejorManoApartado1, contadorManos, botonRepartir); cargarFichero("test1.txt", entrada, resultado, contadorManos);
            }
            else if (apartado.startsWith( "Apartado 2")) {
                mostrarApartado2( zonaJuego, mejorManoApartado2, contadorManos, botonRepartir); cargarFichero("test2.txt", entrada, resultado, contadorManos);
            }
            else if (apartado.startsWith("Apartado 3")) {
                mostrarApartado3(zonaJuego, contadorManos, botonRepartir); cargarFichero("test3.txt", entrada, resultado, contadorManos);
            }
            else if (apartado.startsWith("Apartado 4")) {
                mostrarApartado4(zonaJuego, mejorManoApartado4, contadorManos, botonRepartir); cargarFichero("test4.txt", entrada, resultado, contadorManos);
            }
        }

        );

        // Permite utilizar un fichero distinto a los test por defecto.

        // Permite seleccionar manualmente un fichero de entrada.
        botonFichero.setOnAction(evento -> {
            FileChooser selector = new FileChooser(); selector.setTitle( "Seleccionar fichero de entrada"); selector.getExtensionFilters().add( new FileChooser.ExtensionFilter( "Ficheros de texto (*.txt)", "*.txt")); File fichero = selector.showOpenDialog( escenario); if (fichero != null) {
                try {
                    List<String> lineas = Utils.leerFichero( fichero.getAbsolutePath()); entrada.clear(); for (String linea : lineas) {
                        entrada.appendText( linea + System.lineSeparator());
                    }

                    indiceManoActual = 0; resultado.clear(); contadorManos.setText( "Mano 1 / " + lineas.size()); mesaPoker.reiniciarCartasCentro(); jugadorVistaApartado2.reiniciar();
                }
                catch (IOException e) {
                    entrada.setText( "Error al leer el fichero: " + e.getMessage());
                }
            }
        }

        );

        // Ejecuta el reparto del apartado que esté seleccionado.

        // Ejecuta el reparto correspondiente al apartado seleccionado.
        botonRepartir.setOnAction(evento -> {
            if (entrada.getText().isBlank()) {
                resultado.setText( "No hay ningún fichero cargado."); return;
            }

            String apartado = selectorApartado.getValue(); if (apartado.startsWith( "Apartado 1")) {
                repartirApartado1( entrada, resultado, contadorManos, botonRepartir, mazo, mejorManoApartado1);
            }
            else if (apartado.startsWith( "Apartado 2")) {
                repartirApartado2( entrada, resultado, contadorManos, botonRepartir, mazo, mejorManoApartado2);
            }
            else if (apartado.startsWith("Apartado 3")) {
                repartirApartado3(entrada, resultado, contadorManos, botonRepartir, mazo);
            }
            else if (apartado.startsWith("Apartado 4")) {
                repartirApartado4(entrada, resultado, contadorManos, botonRepartir, mazo, mejorManoApartado4);
            }
        }

        );

        Scene escena = new Scene( fondoGeneral, 950, 720);

        escenario.setTitle( "Calculadora de Poker");

        escenario.setScene( escena);

        escenario.show();
    }

    // Prepara la zona de juego para una mano simple de cinco cartas.
    private void mostrarApartado1(VBox zona, Label mejor, Label contador, Button boton) {
        zona.getChildren().clear();

        mesaPoker.prepararApartado1();

        zona.getChildren().addAll( crupier, mesaPoker, mejor, contador, boton);
    }

    // Prepara la mesa para Texas Hold'em con un único jugador.
    private void mostrarApartado2( VBox zona, Label mejor, Label contador, Button boton) {
        zona.getChildren().clear();

        jugadorVistaApartado2 = new JugadorVista("TÚ");

        mesaPoker.prepararApartado2(jugadorVistaApartado2);

        zona.getChildren().addAll( crupier, mesaPoker, mejor, contador, boton);
    }

    // Prepara la mesa para mostrar varios jugadores y su clasificación.
    private void mostrarApartado3( VBox zona, Label contador, Button boton) {
        zona.getChildren().clear();

        jugadoresVistaApartado3.clear();

        mesaPoker.prepararApartado3(jugadoresVistaApartado3);

        zona.getChildren().addAll( crupier, mesaPoker, contador, boton);
    }

    // Prepara Omaha: cuatro cartas propias y cartas comunitarias.
    private void mostrarApartado4(VBox zona, Label mejor, Label contador, Button boton) {
        zona.getChildren().clear();

        jugadorVistaApartado4 = new JugadorVista("TÚ", 4);

        mesaPoker.prepararApartado2(jugadorVistaApartado4);

        zona.getChildren().addAll(crupier, mesaPoker, mejor, contador, boton);
    }

    // Reparte y evalúa una mano de cinco cartas.

    // Lee una mano del apartado 1, reparte sus cartas y muestra el resultado.
    private void repartirApartado1( TextArea entrada, TextArea resultado, Label contador, Button boton, CartaVista mazo, Label mejorVisual) {
        List<String> lineas = obtenerLineas( entrada);

        if (lineas.isEmpty()) {
            return;
        }

        if (indiceManoActual >= lineas.size()) {
            indiceManoActual = 0;
        }

        String linea = lineas.get( indiceManoActual);

        contador.setText( "Mano " + (indiceManoActual + 1) + " / " + lineas.size());

        List<Carta> cartas = Utils.parsearCartas( linea);

        mesaPoker.prepararApartado1();

        resultado.clear();

        ocultarResultadoVisual(mejorVisual);

        boton.setDisable( true);

        for (int i = 0; i < cartas.size() && i < 5; i++) {
            final int indice = i;

            PauseTransition pausa = new PauseTransition( Duration.millis( i * 600));

            pausa.setOnFinished(e -> {
                repartirDesdeMazo( mazo, mesaPoker.getCartaCentro( indice), cartas.get( indice));
            }

            );

            pausa.play();
        }

        String mejorMano = Evaluador.obtenerMejorManoTexto( cartas);

        List<String> draws = Evaluador.obtenerDraws( cartas);

        PauseTransition fin = new PauseTransition( Duration.millis( 3600));

        fin.setOnFinished(e -> {
            mejorVisual.setText( "★ " + Evaluador.obtenerNombreMano( cartas)); mostrarResultadoVisual( mejorVisual); resultado.appendText( "- Best hand: " + mejorMano + System.lineSeparator()); for (String draw : draws) {
                resultado.appendText( "- " + draw + System.lineSeparator());
            }

            avanzarMano( lineas); boton.setDisable( false);
        }

        );

        fin.play();
    }

    // Reparte las cartas propias y comunitarias de Texas Hold'em.

    // Reparte las cartas propias y comunitarias de Texas Hold'em.
    private void repartirApartado2( TextArea entrada, TextArea resultado, Label contador, Button boton, CartaVista mazo, Label mejorVisual) {
        List<String> lineas = obtenerLineas( entrada);

        if (lineas.isEmpty()) {
            return;
        }

        if (indiceManoActual >= lineas.size()) {
            indiceManoActual = 0;
        }

        String linea = lineas.get( indiceManoActual);

        contador.setText( "Mano " + (indiceManoActual + 1) + " / " + lineas.size());

        String[] partes = linea.split(";");

        if (partes.length < 3) {
            resultado.setText( "Formato incorrecto.");

            return;
        }

        List<Carta> propias = Utils.parsearCartas( partes[0]);

        int numeroComunes = Integer.parseInt( partes[1]);

        List<Carta> comunes = Utils.parsearCartas( partes[2]);

        if (comunes.size() > numeroComunes) {
            comunes = new ArrayList<>( comunes.subList( 0, numeroComunes));
        }

        final List<Carta> comunesFinal = comunes;

        Jugador jugador = new Jugador( propias, comunesFinal);

        List<Carta> mejorMano = jugador.getMejorMano().getCartas();

        jugadorVistaApartado2.reiniciar();

        mesaPoker.reiniciarCartasCentro();

        resultado.clear();

        ocultarResultadoVisual(mejorVisual);

        boton.setDisable( true);

        for (int i = 0; i < propias.size(); i++) {
            final int indice = i;

            PauseTransition pausa = new PauseTransition( Duration.millis( i * 600));

            pausa.setOnFinished(e -> {
                repartirDesdeMazo( mazo, jugadorVistaApartado2.getCartaVista( indice), propias.get( indice));
            }

            );

            pausa.play();
        }

        for (int i = 0; i < comunesFinal.size(); i++) {
            final int indice = i;

            PauseTransition pausa = new PauseTransition( Duration.millis( 1200 + i * 600));

            pausa.setOnFinished(e -> {
                repartirDesdeMazo( mazo, mesaPoker.getCartaCentro( indice), comunesFinal.get( indice));
            }

            );

            pausa.play();
        }

        long tiempoFinal = 1200 + comunesFinal.size() * 600L + 700;

        PauseTransition fin = new PauseTransition( Duration.millis( tiempoFinal));

        fin.setOnFinished(e -> {
            mejorVisual.setText( "★ MEJOR MANO: " + jugador.getNombreMano()); mostrarResultadoVisual( mejorVisual); destacarMejorManoApartado2( mejorMano); resultado.appendText( "- Best hand: " + jugador.getMejorManoTexto() + System.lineSeparator()); for (String draw : jugador.getDraws()) {
                resultado.appendText( "- " + draw + System.lineSeparator());
            }

            avanzarMano( lineas); boton.setDisable( false);
        }

        );

        fin.play();
    }

    // Reparte dos cartas a cada jugador y muestra la clasificación final.

    // Reparte las cartas de todos los jugadores y calcula la clasificación final.
    private void repartirApartado3( TextArea entrada, TextArea resultado, Label contador, Button boton, CartaVista mazo) {
        List<String> lineas = obtenerLineas( entrada);

        if (lineas.isEmpty()) {
            return;
        }

        if (indiceManoActual >= lineas.size()) {
            indiceManoActual = 0;
        }

        String linea = lineas.get( indiceManoActual);

        contador.setText( "Mano " + (indiceManoActual + 1) + " / " + lineas.size());

        String[] partes = linea.split(";");

        int numeroJugadores = Integer.parseInt( partes[0]);

        List<Carta> comunes = Utils.parsearCartas( partes[ partes.length - 1]);

        List<String> ids = new ArrayList<>();

        List<List<Carta>> cartasJugadores = new ArrayList<>();

        List<Jugador> jugadores = new ArrayList<>();

        for (int i = 1; i <= numeroJugadores; i++) {
            String textoJugador = partes[i];

            String id = textoJugador.substring( 0, textoJugador.length() - 4);

            List<Carta> propias = Utils.parsearCartas( textoJugador.substring( textoJugador.length() - 4));

            ids.add( id);

            cartasJugadores.add( propias);

            jugadores.add( new Jugador( id, propias, comunes));
        }

        jugadoresVistaApartado3.clear();

        for (String id : ids) {
            jugadoresVistaApartado3.add( new JugadorVista( id));
        }

        mesaPoker.prepararApartado3( jugadoresVistaApartado3);

        resultado.clear();

        boton.setDisable( true);

        long retraso = 0;

        final long intervalo = 450;

        for (int numeroCarta = 0; numeroCarta < 2; numeroCarta++) {
            final int indiceCarta = numeroCarta;

            for (int j = 0; j < numeroJugadores; j++) {
                final int indiceJugador = j;

                PauseTransition pausa = new PauseTransition( Duration.millis( retraso));

                pausa.setOnFinished(e -> {
                    repartirDesdeMazo( mazo, jugadoresVistaApartado3.get( indiceJugador).getCartaVista( indiceCarta), cartasJugadores.get( indiceJugador).get( indiceCarta));
                }

                );

                pausa.play();

                retraso += intervalo;
            }
        }

        for (int i = 0; i < comunes.size() && i < 5; i++) {
            final int indice = i;

            PauseTransition pausa = new PauseTransition( Duration.millis( retraso));

            pausa.setOnFinished(e -> {
                repartirDesdeMazo( mazo, mesaPoker.getCartaCentro( indice), comunes.get( indice));
            }

            );

            pausa.play();

            retraso += intervalo;
        }

        PauseTransition fin = new PauseTransition( Duration.millis( retraso + 800));

        fin.setOnFinished(e -> {
            List<Jugador> clasificacion = new ArrayList<>( jugadores); clasificacion.sort( Collections.reverseOrder()); for (int i = 0; i < clasificacion.size(); i++) {
                Jugador jugador = clasificacion.get( i); JugadorVista vista = buscarVistaJugador( jugador.getId()); if (vista != null) {
                    vista.mostrarResultado( i + 1, jugador.getNombreMano(), i == 0);
                }
            }

            if (!clasificacion.isEmpty()) {
                destacarManoGanadora( clasificacion.get(0));
            }

            resultado.clear(); for (int i = 0; i < clasificacion.size(); i++) {
                Jugador jugador = clasificacion.get( i); resultado.appendText( (i + 1) + "º " + jugador.getId() + ": " + Utils.cartasAString( jugador.getMejorMano().getCartas()) + " (" + jugador.getNombreMano() + ")" + System.lineSeparator());
            }

            avanzarMano( lineas); boton.setDisable( false);
        }

        );

        fin.play();
    }

    // Omaha: cuatro cartas propias, usando exactamente dos junto a tres de la mesa.

    // Reparte Omaha y evalúa la mano usando exactamente 2 propias y 3 comunitarias.
    private void repartirApartado4(TextArea entrada, TextArea resultado, Label contador, Button boton, CartaVista mazo, Label mejorVisual) {
        List<String> lineas = obtenerLineas(entrada);

        if (lineas.isEmpty()) return;

        if (indiceManoActual >= lineas.size()) indiceManoActual = 0;

        String linea = lineas.get(indiceManoActual);

        contador.setText("Mano " + (indiceManoActual + 1) + " / " + lineas.size());

        String[] partes = linea.split(";");

        if (partes.length < 3) {
            resultado.setText("Formato incorrecto.");

            return;
        }

        List<Carta> propias = Utils.parsearCartas(partes[0]);

        int numeroComunes = Integer.parseInt(partes[1]);

        List<Carta> comunes = Utils.parsearCartas(partes[2]);

        if (propias.size() != 4) {
            resultado.setText("En Omaha el jugador debe tener 4 cartas propias.");

            return;
        }

        if (comunes.size() > numeroComunes) comunes = new ArrayList<>(comunes.subList(0, numeroComunes));

        final List<Carta> comunesFinal = comunes;

        JugadorOmaha jugador = new JugadorOmaha(propias, comunesFinal);

        List<Carta> mejorMano = jugador.getMejorCombinacion();

        jugadorVistaApartado4.reiniciar();

        mesaPoker.reiniciarCartasCentro();

        resultado.clear();

        ocultarResultadoVisual(mejorVisual);

        boton.setDisable(true);

        for (int i = 0; i < propias.size(); i++) {
            final int indice = i;

            PauseTransition pausa = new PauseTransition(Duration.millis(i * 600L));

            pausa.setOnFinished(e -> repartirDesdeMazo(mazo, jugadorVistaApartado4.getCartaVista(indice), propias.get(indice)));

            pausa.play();
        }

        long inicioComunes = propias.size() * 600L;

        for (int i = 0; i < comunesFinal.size() && i < 5; i++) {
            final int indice = i;

            PauseTransition pausa = new PauseTransition(Duration.millis(inicioComunes + i * 600L));

            pausa.setOnFinished(e -> repartirDesdeMazo(mazo, mesaPoker.getCartaCentro(indice), comunesFinal.get(indice)));

            pausa.play();
        }

        long tiempoFinal = inicioComunes + comunesFinal.size() * 600L + 700;

        PauseTransition fin = new PauseTransition(Duration.millis(tiempoFinal));

        fin.setOnFinished(e -> {
            mejorVisual.setText("★ MEJOR MANO: " + jugador.getNombreMano()); mostrarResultadoVisual(mejorVisual); destacarMejorManoApartado4(mejorMano); resultado.appendText("- Best hand: " + jugador.getMejorManoTexto() + System.lineSeparator()); for (String draw : jugador.getDraws()) resultado.appendText("- " + draw + System.lineSeparator()); avanzarMano(lineas); boton.setDisable(false);
        }

        );

        fin.play();
    }

    // Destaca las cinco cartas que forman la mejor combinación de Omaha.
    private void destacarMejorManoApartado4(List<Carta> mejorMano) {
        List<CartaVista> todas = new ArrayList<>();

        todas.addAll(jugadorVistaApartado4.getCartasVista());

        todas.addAll(mesaPoker.getCartasCentro());

        destacarCartas(mejorMano, todas);
    }

    // Destaca las cartas que forman la mejor mano del apartado 2.
    private void destacarMejorManoApartado2( List<Carta> mejorMano) {
        List<CartaVista> todas = new ArrayList<>();

        todas.addAll( jugadorVistaApartado2.getCartasVista());

        todas.addAll( mesaPoker.getCartasCentro());

        destacarCartas( mejorMano, todas);
    }

    // Localiza al ganador del apartado 3 y resalta las cartas de su mejor mano.
    private void destacarManoGanadora( Jugador ganador) {
        JugadorVista vistaGanador = buscarVistaJugador( ganador.getId());

        if (vistaGanador == null) {
            return;
        }

        List<CartaVista> posibles = new ArrayList<>();

        posibles.addAll( vistaGanador.getCartasVista());

        posibles.addAll( mesaPoker.getCartasCentro());

        destacarCartas( ganador.getMejorMano().getCartas(), posibles);
    }

    // Aplica el destacado visual únicamente a las cartas indicadas.
    private void destacarCartas( List<Carta> mejorMano, List<CartaVista> vistas) {
        for (CartaVista vista : vistas) {
            vista.quitarDestacado();
        }

        for (Carta cartaMejor : mejorMano) {
            for (CartaVista vista : vistas) {
                if (vista.representa( cartaMejor)) {
                    vista.destacar();

                    break;
                }
            }
        }
    }

    // Busca la vista asociada a un jugador mediante su identificador.
    private JugadorVista buscarVistaJugador( String id) {
        for (JugadorVista vista : jugadoresVistaApartado3) {
            if (vista.getIdJugador().equals(id)) {
                return vista;
            }
        }

        return null;
    }

    // Delega en el crupier la animación de reparto de una carta.
    private void repartirDesdeMazo( CartaVista mazo, CartaVista destino, Carta carta) {
        crupier.repartirCarta( destino, carta);
    }

    // Hace visible una etiqueta de resultado.
    private void mostrarResultadoVisual( Label label) {
        label.setVisible( true);

        label.setManaged( true);
    }

    // Oculta una etiqueta de resultado hasta que termine el reparto.
    private void ocultarResultadoVisual(Label label) {
        label.setVisible( false);

        label.setManaged( false);
    }

    // Obtiene únicamente las líneas no vacías del fichero mostrado.
    private List<String> obtenerLineas( TextArea entrada) {
        return entrada.getText().lines().filter( linea -> !linea.isBlank()).toList();
    }

    // Avanza a la siguiente mano y vuelve al principio al llegar al final.
    private void avanzarMano( List<String> lineas) {
        indiceManoActual++;

        if (indiceManoActual >= lineas.size()) {
            indiceManoActual = 0;
        }
    }

    // Carga uno de los ficheros de prueba en el área de entrada.
    private void cargarFichero( String nombre, TextArea entrada, TextArea resultado, Label contador) {
        try {
            List<String> lineas = Utils.leerFichero( nombre);

            entrada.clear();

            for (String linea : lineas) {
                entrada.appendText( linea + System.lineSeparator());
            }

            resultado.clear();

            indiceManoActual = 0;

            contador.setText( "Mano 1 / " + lineas.size());
        }
        catch (IOException e) {
            entrada.setText( "Error al cargar " + nombre + ": " + e.getMessage());
        }
    }

    // Punto de entrada de la interfaz JavaFX.
    public static void main( String[] args) {
        launch(args);
    }
}
