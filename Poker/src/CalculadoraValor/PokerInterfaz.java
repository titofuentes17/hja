package CalculadoraValor;

import java.io.File;

import java.io.IOException;

import java.util.ArrayList;

import java.util.Collections;

import java.util.List;

import javafx.animation.PauseTransition;

import javafx.application.Application;

import javafx.geometry.Bounds;

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

    // =========================================================

    // MANO ACTUAL

    // =========================================================

    private int indiceManoActual = 0;

    // =========================================================

    // MESA

    // =========================================================

    private MesaPokerVista mesaPoker;

    // Crupier visual: guantes + mazo
    private CrupierVista crupier;

    // =========================================================

    // APARTADO 2

    // =========================================================

    private JugadorVista jugadorVistaApartado2;

    // =========================================================

    // APARTADO 3

    // =========================================================

    private List<JugadorVista> jugadoresVistaApartado3 =

            new ArrayList<>();

    // =========================================================

    // START

    // =========================================================

    @Override

    public void start(Stage escenario) {

        // =====================================================

        // FONDO GENERAL

        // =====================================================

        StackPane fondoGeneral =

                new StackPane();

        fondoGeneral.setStyle(

                "-fx-background-color: " +

                "linear-gradient(to bottom, #071A14, #0B2B20, #07110E);"

        );

        // =====================================================

        // SÍMBOLOS DECORATIVOS

        // =====================================================

        Label simboloPicas =

                crearSimboloFondo("♠");

        Label simboloCorazones =

                crearSimboloFondo("♥");

        Label simboloDiamantes =

                crearSimboloFondo("♦");

        Label simboloTreboles =

                crearSimboloFondo("♣");

        // Picas - arriba izquierda

        StackPane.setAlignment(

                simboloPicas,

                Pos.TOP_LEFT

        );

        StackPane.setMargin(

                simboloPicas,

                new Insets(

                        60,

                        0,

                        0,

                        45

                )

        );

        // Corazones - arriba derecha

        StackPane.setAlignment(

                simboloCorazones,

                Pos.TOP_RIGHT

        );

        StackPane.setMargin(

                simboloCorazones,

                new Insets(

                        80,

                        55,

                        0,

                        0

                )

        );

        // Tréboles - abajo izquierda

        StackPane.setAlignment(

                simboloTreboles,

                Pos.BOTTOM_LEFT

        );

        StackPane.setMargin(

                simboloTreboles,

                new Insets(

                        0,

                        0,

                        70,

                        55

                )

        );

        // Diamantes - abajo derecha

        StackPane.setAlignment(

                simboloDiamantes,

                Pos.BOTTOM_RIGHT

        );

        StackPane.setMargin(

                simboloDiamantes,

                new Insets(

                        0,

                        45,

                        60,

                        0

                )

        );

        // =====================================================

        // CONTENIDO PRINCIPAL

        // =====================================================

        BorderPane raiz =

                new BorderPane();

        raiz.setPadding(

                new Insets(8)

        );

        /*

         * Transparente para poder ver el fondo

         * y los símbolos decorativos.

         */

        raiz.setStyle(

                "-fx-background-color: transparent;"

        );

        // =====================================================

        // TÍTULO

        // =====================================================

        Label titulo =

                new Label(

                        "♠  CALCULADORA DE PÓKER  ♥"

                );

        titulo.setStyle(

                "-fx-font-size: 21px;" +

                "-fx-font-weight: bold;" +

                "-fx-text-fill: #F5F5F5;"

        );

        // =====================================================

        // SELECTOR

        // =====================================================

        ComboBox<String> selectorApartado =

                new ComboBox<>();

        selectorApartado

                .getItems()

                .addAll(

                        "Apartado 1 - Mano de 5 cartas",

                        "Apartado 2 - Texas Hold'em",

                        "Apartado 3 - Varios jugadores"

                );

        selectorApartado.setValue(

                "Apartado 1 - Mano de 5 cartas"

        );

        // =====================================================

        // BOTÓN FICHERO

        // =====================================================

        Button botonFichero =

                new Button(

                        "Seleccionar fichero"

                );

        botonFichero.setStyle(

                "-fx-background-color: #153C2D;" +

                "-fx-text-fill: white;" +

                "-fx-font-weight: bold;" +

                "-fx-border-color: #C9A227;" +

                "-fx-border-width: 1;" +

                "-fx-border-radius: 6;" +

                "-fx-background-radius: 6;" +

                "-fx-padding: 5 12 5 12;" +

                "-fx-cursor: hand;"

        );

        // =====================================================

        // OPCIONES SUPERIORES

        // =====================================================

        HBox opciones =

                new HBox(

                        10,

                        selectorApartado,

                        botonFichero

                );

        opciones.setAlignment(

                Pos.CENTER

        );

        VBox parteSuperior =

                new VBox(

                        6,

                        titulo,

                        opciones

                );

        parteSuperior.setAlignment(

                Pos.CENTER

        );

        raiz.setTop(

                parteSuperior

        );

        // =====================================================

        // CRUPIER

        // =====================================================

        // Crupier completo: guante izquierdo + mazo + guante derecho
        crupier = new CrupierVista();

        // Conservamos esta referencia porque la lógica de reparto ya trabaja con el mazo
        CartaVista mazo = crupier.getMazo();

        // =====================================================

        // MESA

        // =====================================================

        mesaPoker =

                new MesaPokerVista();

        // =====================================================

        // JUGADOR APARTADO 2

        // =====================================================

        jugadorVistaApartado2 =

                new JugadorVista(

                        "TÚ"

                );

        // =====================================================

        // RESULTADO VISUAL APARTADO 1

        // =====================================================

        Label mejorManoApartado1 =

                new Label();

        mejorManoApartado1.setStyle(

                "-fx-font-size: 14px;" +

                "-fx-font-weight: bold;" +

                "-fx-text-fill: #D6B85A;"

        );

        mejorManoApartado1.setVisible(false);

        mejorManoApartado1.setManaged(false);

        // =====================================================

        // RESULTADO VISUAL APARTADO 2

        // =====================================================

        Label mejorManoApartado2 =

                new Label();

        mejorManoApartado2.setStyle(

                "-fx-font-size: 14px;" +

                "-fx-font-weight: bold;" +

                "-fx-text-fill: #D6B85A;"

        );

        mejorManoApartado2.setVisible(false);

        mejorManoApartado2.setManaged(false);

        // =====================================================

        // CONTADOR

        // =====================================================

        Label contadorManos =

                new Label(

                        "Mano 1"

                );

        contadorManos.setStyle(

                "-fx-font-size: 12px;" +

                "-fx-font-weight: bold;" +

                "-fx-text-fill: #E2E2E2;"

        );

        // =====================================================

        // BOTÓN REPARTIR

        // =====================================================

        Button botonRepartir =

                new Button(

                        "▶  REPARTIR"

                );

        botonRepartir.setStyle(

                "-fx-background-color: #C9A227;" +

                "-fx-text-fill: #111111;" +

                "-fx-font-weight: bold;" +

                "-fx-font-size: 13px;" +

                "-fx-background-radius: 8;" +

                "-fx-padding: 7 22 7 22;" +

                "-fx-cursor: hand;"

        );

        // =====================================================

        // ZONA DE JUEGO

        // =====================================================

        VBox zonaJuego =

                new VBox(2);

        zonaJuego.setAlignment(

                Pos.CENTER

        );

        raiz.setCenter(

                zonaJuego

        );

        // =====================================================

        // FICHERO DE ENTRADA

        // =====================================================

        Label tituloEntrada =

                new Label(

                        "FICHERO DE ENTRADA"

                );

        tituloEntrada.setStyle(

                "-fx-font-size: 12px;" +

                "-fx-font-weight: bold;" +

                "-fx-text-fill: #D6B85A;"

        );

        TextArea entrada =

                new TextArea();

        entrada.setEditable(false);

        entrada.setPrefHeight(80);

        entrada.setPrefWidth(410);

        entrada.setStyle(

                "-fx-control-inner-background: #F4F4F4;" +

                "-fx-font-family: 'Consolas';" +

                "-fx-font-size: 12px;" +

                "-fx-background-radius: 6;"

        );

        VBox zonaEntrada =

                new VBox(

                        5,

                        tituloEntrada,

                        entrada

                );

        zonaEntrada.setPadding(

                new Insets(8)

        );

        zonaEntrada.setStyle(

                "-fx-background-color: rgba(5,18,14,0.92);" +

                "-fx-border-color: #C9A227;" +

                "-fx-border-width: 2;" +

                "-fx-border-radius: 10;" +

                "-fx-background-radius: 10;"

        );

        // =====================================================

        // RESULTADO

        // =====================================================

        Label tituloResultado =

                new Label(

                        "RESULTADO"

                );

        tituloResultado.setStyle(

                "-fx-font-size: 12px;" +

                "-fx-font-weight: bold;" +

                "-fx-text-fill: #D6B85A;"

        );

        TextArea resultado =

                new TextArea();

        resultado.setEditable(false);

        resultado.setPrefHeight(80);

        resultado.setPrefWidth(410);

        resultado.setStyle(

                "-fx-control-inner-background: #F4F4F4;" +

                "-fx-font-family: 'Consolas';" +

                "-fx-font-size: 12px;" +

                "-fx-background-radius: 6;"

        );

        VBox zonaResultado =

                new VBox(

                        5,

                        tituloResultado,

                        resultado

                );

        zonaResultado.setPadding(

                new Insets(8)

        );

        zonaResultado.setStyle(

                "-fx-background-color: rgba(5,18,14,0.92);" +

                "-fx-border-color: #C9A227;" +

                "-fx-border-width: 2;" +

                "-fx-border-radius: 10;" +

                "-fx-background-radius: 10;"

        );

        // =====================================================

        // PARTE INFERIOR

        // =====================================================

        HBox parteInferior =

                new HBox(

                        12,

                        zonaEntrada,

                        zonaResultado

                );

        parteInferior.setAlignment(

                Pos.CENTER

        );

        parteInferior.setPadding(

                new Insets(

                        6,

                        0,

                        0,

                        0

                )

        );

        raiz.setBottom(

                parteInferior

        );

        // =====================================================

        // AÑADIR FONDO + CONTENIDO

        // =====================================================

        /*

         * El orden es importante.

         *

         * Los símbolos se añaden primero para que

         * queden detrás de la interfaz.

         */

        fondoGeneral

                .getChildren()

                .addAll(

                        simboloPicas,

                        simboloCorazones,

                        simboloTreboles,

                        simboloDiamantes,

                        raiz

                );

        // =====================================================

        // APARTADO 1 INICIAL

        // =====================================================

        mostrarApartado1(
                zonaJuego,
                mejorManoApartado1,
                contadorManos,
                botonRepartir
        );

        cargarFichero(

                "test1.txt",

                entrada,

                resultado,

                contadorManos

        );

        // =====================================================

        // CAMBIO DE APARTADO

        // =====================================================

        selectorApartado.setOnAction(evento -> {

            indiceManoActual = 0;

            resultado.clear();

            ocultarResultadoVisual(

                    mejorManoApartado1

            );

            ocultarResultadoVisual(

                    mejorManoApartado2

            );

            String apartado =

                    selectorApartado.getValue();

            if (apartado.startsWith(

                    "Apartado 1")) {

                mostrarApartado1(
                zonaJuego,
                mejorManoApartado1,
                contadorManos,
                botonRepartir
        );

                cargarFichero(

                        "test1.txt",

                        entrada,

                        resultado,

                        contadorManos

                );

            }

            else if (apartado.startsWith(

                    "Apartado 2")) {

                mostrarApartado2(
                        zonaJuego,
                        mejorManoApartado2,
                        contadorManos,
                        botonRepartir
                );

                cargarFichero(

                        "test2.txt",

                        entrada,

                        resultado,

                        contadorManos

                );

            }

            else {

                mostrarApartado3(
                        zonaJuego,
                        contadorManos,
                        botonRepartir
                );

                cargarFichero(

                        "test3.txt",

                        entrada,

                        resultado,

                        contadorManos

                );

            }

        });

        // =====================================================

        // SELECCIONAR FICHERO

        // =====================================================

        botonFichero.setOnAction(evento -> {

            FileChooser selector =

                    new FileChooser();

            selector.setTitle(

                    "Seleccionar fichero de entrada"

            );

            selector

                    .getExtensionFilters()

                    .add(

                            new FileChooser.ExtensionFilter(

                                    "Ficheros de texto (*.txt)",

                                    "*.txt"

                            )

                    );

            File fichero =

                    selector.showOpenDialog(

                            escenario

                    );

            if (fichero != null) {

                try {

                    List<String> lineas =

                            Utils.leerFichero(

                                    fichero.getAbsolutePath()

                            );

                    entrada.clear();

                    for (String linea : lineas) {

                        entrada.appendText(

                                linea

                                        + System.lineSeparator()

                        );

                    }

                    indiceManoActual = 0;

                    resultado.clear();

                    contadorManos.setText(

                            "Mano 1 / "

                                    + lineas.size()

                    );

                    mesaPoker

                            .reiniciarCartasCentro();

                    jugadorVistaApartado2

                            .reiniciar();

                } catch (IOException e) {

                    entrada.setText(

                            "Error al leer el fichero: "

                                    + e.getMessage()

                    );

                }

            }

        });

        // =====================================================

        // BOTÓN REPARTIR

        // =====================================================

        botonRepartir.setOnAction(evento -> {

            if (entrada

                    .getText()

                    .isBlank()) {

                resultado.setText(

                        "No hay ningún fichero cargado."

                );

                return;

            }

            String apartado =

                    selectorApartado.getValue();

            if (apartado.startsWith(

                    "Apartado 1")) {

                repartirApartado1(

                        entrada,

                        resultado,

                        contadorManos,

                        botonRepartir,

                        mazo,

                        mejorManoApartado1

                );

            }

            else if (apartado.startsWith(

                    "Apartado 2")) {

                repartirApartado2(

                        entrada,

                        resultado,

                        contadorManos,

                        botonRepartir,

                        mazo,

                        mejorManoApartado2

                );

            }

            else {

                repartirApartado3(

                        entrada,

                        resultado,

                        contadorManos,

                        botonRepartir,

                        mazo

                );

            }

        });

        // =====================================================

        // ESCENA

        // =====================================================

        Scene escena =

                new Scene(

                        fondoGeneral,

                        950,

                        720

                );

        escenario.setTitle(

                "Calculadora de Poker"

        );

        escenario.setScene(

                escena

        );

        escenario.show();

    }

    // =========================================================

    // MOSTRAR APARTADO 1

    // =========================================================

    private void mostrarApartado1(
            VBox zona,
            Label mejor,
            Label contador,
            Button boton) {

        zona.getChildren().clear();
        mesaPoker.prepararApartado1();

        zona.getChildren().addAll(
                crupier,
                mesaPoker,
                mejor,
                contador,
                boton
        );
    }

    // =========================================================

    // MOSTRAR APARTADO 2

    // =========================================================

    private void mostrarApartado2(
            VBox zona,
            Label mejor,
            Label contador,
            Button boton) {

        zona.getChildren().clear();
        jugadorVistaApartado2 = new JugadorVista("TÚ");
        mesaPoker.prepararApartado2(jugadorVistaApartado2);

        zona.getChildren().addAll(
                crupier,
                mesaPoker,
                mejor,
                contador,
                boton
        );
    }

    // =========================================================

    // MOSTRAR APARTADO 3

    // =========================================================

    private void mostrarApartado3(
            VBox zona,
            Label contador,
            Button boton) {

        zona.getChildren().clear();
        jugadoresVistaApartado3.clear();
        mesaPoker.prepararApartado3(jugadoresVistaApartado3);

        zona.getChildren().addAll(
                crupier,
                mesaPoker,
                contador,
                boton
        );
    }

    // =========================================================

    // APARTADO 1

    // =========================================================

    private void repartirApartado1(

            TextArea entrada,

            TextArea resultado,

            Label contador,

            Button boton,

            CartaVista mazo,

            Label mejorVisual) {

        List<String> lineas =

                obtenerLineas(

                        entrada

                );

        if (lineas.isEmpty()) {

            return;

        }

        if (indiceManoActual >= lineas.size()) {

            indiceManoActual = 0;

        }

        String linea =

                lineas.get(

                        indiceManoActual

                );

        contador.setText(

                "Mano "

                        + (indiceManoActual + 1)

                        + " / "

                        + lineas.size()

        );

        List<Carta> cartas =

                Utils.parsearCartas(

                        linea

                );

        mesaPoker.prepararApartado1();

        resultado.clear();

        ocultarResultadoVisual(

                mejorVisual

        );

        boton.setDisable(

                true

        );

        // =====================================================

        // REPARTIR 5 CARTAS

        // =====================================================

        for (int i = 0;

             i < cartas.size()

                     && i < 5;

             i++) {

            final int indice =

                    i;

            PauseTransition pausa =

                    new PauseTransition(

                            Duration.millis(

                                    i * 600

                            )

                    );

            pausa.setOnFinished(e -> {

                repartirDesdeMazo(

                        mazo,

                        mesaPoker.getCartaCentro(

                                indice

                        ),

                        cartas.get(

                                indice

                        )

                );

            });

            pausa.play();

        }

        String mejorMano =

                Evaluador.obtenerMejorManoTexto(

                        cartas

                );

        List<String> draws =

                Evaluador.obtenerDraws(

                        cartas

                );

        PauseTransition fin =

                new PauseTransition(

                        Duration.millis(

                                3600

                        )

                );

        fin.setOnFinished(e -> {

            mejorVisual.setText(

                    "★ "

                            + Evaluador.obtenerNombreMano(

                                    cartas

                            )

            );

            mostrarResultadoVisual(

                    mejorVisual

            );

            resultado.appendText(

                    "- Best hand: "

                            + mejorMano

                            + System.lineSeparator()

            );

            for (String draw : draws) {

                resultado.appendText(

                        "- "

                                + draw

                                + System.lineSeparator()

                );

            }

            avanzarMano(

                    lineas

            );

            boton.setDisable(

                    false

            );

        });

        fin.play();

    }

    // =========================================================

    // APARTADO 2

    // =========================================================

    private void repartirApartado2(

            TextArea entrada,

            TextArea resultado,

            Label contador,

            Button boton,

            CartaVista mazo,

            Label mejorVisual) {

        List<String> lineas =

                obtenerLineas(

                        entrada

                );

        if (lineas.isEmpty()) {

            return;

        }

        if (indiceManoActual >= lineas.size()) {

            indiceManoActual = 0;

        }

        String linea =

                lineas.get(

                        indiceManoActual

                );

        contador.setText(

                "Mano "

                        + (indiceManoActual + 1)

                        + " / "

                        + lineas.size()

        );

        String[] partes =

                linea.split(";");

        if (partes.length < 3) {

            resultado.setText(

                    "Formato incorrecto."

            );

            return;

        }

        List<Carta> propias =

                Utils.parsearCartas(

                        partes[0]

                );

        int numeroComunes =

                Integer.parseInt(

                        partes[1]

                );

        List<Carta> comunes =

                Utils.parsearCartas(

                        partes[2]

                );

        if (comunes.size()

                > numeroComunes) {

            comunes =

                    new ArrayList<>(

                            comunes.subList(

                                    0,

                                    numeroComunes

                            )

                    );

        }

        final List<Carta> comunesFinal =

                comunes;

        Jugador jugador =

                new Jugador(

                        propias,

                        comunesFinal

                );

        List<Carta> mejorMano =

                jugador

                        .getMejorMano()

                        .getCartas();

        jugadorVistaApartado2

                .reiniciar();

        mesaPoker

                .reiniciarCartasCentro();

        resultado.clear();

        ocultarResultadoVisual(

                mejorVisual

        );

        boton.setDisable(

                true

        );

        // =====================================================

        // CARTAS DEL JUGADOR

        // =====================================================

        for (int i = 0;

             i < propias.size();

             i++) {

            final int indice =

                    i;

            PauseTransition pausa =

                    new PauseTransition(

                            Duration.millis(

                                    i * 600

                            )

                    );

            pausa.setOnFinished(e -> {

                repartirDesdeMazo(

                        mazo,

                        jugadorVistaApartado2

                                .getCartaVista(

                                        indice

                                ),

                        propias.get(

                                indice

                        )

                );

            });

            pausa.play();

        }

        // =====================================================

        // CARTAS COMUNITARIAS

        // =====================================================

        for (int i = 0;

             i < comunesFinal.size();

             i++) {

            final int indice =

                    i;

            PauseTransition pausa =

                    new PauseTransition(

                            Duration.millis(

                                    1200

                                            + i * 600

                            )

                    );

            pausa.setOnFinished(e -> {

                repartirDesdeMazo(

                        mazo,

                        mesaPoker.getCartaCentro(

                                indice

                        ),

                        comunesFinal.get(

                                indice

                        )

                );

            });

            pausa.play();

        }

        long tiempoFinal =

                1200

                        + comunesFinal.size()

                        * 600L

                        + 700;

        PauseTransition fin =

                new PauseTransition(

                        Duration.millis(

                                tiempoFinal

                        )

                );

        fin.setOnFinished(e -> {

            mejorVisual.setText(

                    "★ MEJOR MANO: "

                            + jugador.getNombreMano()

            );

            mostrarResultadoVisual(

                    mejorVisual

            );

            destacarMejorManoApartado2(

                    mejorMano

            );

            resultado.appendText(

                    "- Best hand: "

                            + jugador.getMejorManoTexto()

                            + System.lineSeparator()

            );

            for (String draw :

                    jugador.getDraws()) {

                resultado.appendText(

                        "- "

                                + draw

                                + System.lineSeparator()

                );

            }

            avanzarMano(

                    lineas

            );

            boton.setDisable(

                    false

            );

        });

        fin.play();

    }

    // =========================================================

    // APARTADO 3

    // =========================================================

    private void repartirApartado3(

            TextArea entrada,

            TextArea resultado,

            Label contador,

            Button boton,

            CartaVista mazo) {

        List<String> lineas =

                obtenerLineas(

                        entrada

                );

        if (lineas.isEmpty()) {

            return;

        }

        if (indiceManoActual >= lineas.size()) {

            indiceManoActual = 0;

        }

        String linea =

                lineas.get(

                        indiceManoActual

                );

        contador.setText(

                "Mano "

                        + (indiceManoActual + 1)

                        + " / "

                        + lineas.size()

        );

        String[] partes =

                linea.split(";");

        int numeroJugadores =

                Integer.parseInt(

                        partes[0]

                );

        List<Carta> comunes =

                Utils.parsearCartas(

                        partes[

                                partes.length - 1

                        ]

                );

        List<String> ids =

                new ArrayList<>();

        List<List<Carta>> cartasJugadores =

                new ArrayList<>();

        List<Jugador> jugadores =

                new ArrayList<>();

        // =====================================================

        // LEER JUGADORES

        // =====================================================

        for (int i = 1;

             i <= numeroJugadores;

             i++) {

            String textoJugador =

                    partes[i];

            String id =

                    textoJugador.substring(

                            0,

                            textoJugador.length() - 4

                    );

            List<Carta> propias =

                    Utils.parsearCartas(

                            textoJugador.substring(

                                    textoJugador.length() - 4

                            )

                    );

            ids.add(

                    id

            );

            cartasJugadores.add(

                    propias

            );

            jugadores.add(

                    new Jugador(

                            id,

                            propias,

                            comunes

                    )

            );

        }

        // =====================================================

        // CREAR JUGADORES VISUALES

        // =====================================================

        jugadoresVistaApartado3.clear();

        for (String id : ids) {

            jugadoresVistaApartado3.add(

                    new JugadorVista(

                            id

                    )

            );

        }

        mesaPoker.prepararApartado3(

                jugadoresVistaApartado3

        );

        resultado.clear();

        boton.setDisable(

                true

        );

        // =====================================================

        // REPARTO

        // =====================================================

        long retraso = 0;

        final long intervalo = 450;

        /*

         * Primera ronda:

         *

         * J1 J2 J3...

         *

         * Segunda ronda:

         *

         * J1 J2 J3...

         */

        for (int numeroCarta = 0;

             numeroCarta < 2;

             numeroCarta++) {

            final int indiceCarta =

                    numeroCarta;

            for (int j = 0;

                 j < numeroJugadores;

                 j++) {

                final int indiceJugador =

                        j;

                PauseTransition pausa =

                        new PauseTransition(

                                Duration.millis(

                                        retraso

                                )

                        );

                pausa.setOnFinished(e -> {

                    repartirDesdeMazo(

                            mazo,

                            jugadoresVistaApartado3

                                    .get(

                                            indiceJugador

                                    )

                                    .getCartaVista(

                                            indiceCarta

                                    ),

                            cartasJugadores

                                    .get(

                                            indiceJugador

                                    )

                                    .get(

                                            indiceCarta

                                    )

                    );

                });

                pausa.play();

                retraso += intervalo;

            }

        }

        // =====================================================

        // COMUNITARIAS

        // =====================================================

        for (int i = 0;

             i < comunes.size()

                     && i < 5;

             i++) {

            final int indice =

                    i;

            PauseTransition pausa =

                    new PauseTransition(

                            Duration.millis(

                                    retraso

                            )

                    );

            pausa.setOnFinished(e -> {

                repartirDesdeMazo(

                        mazo,

                        mesaPoker.getCartaCentro(

                                indice

                        ),

                        comunes.get(

                                indice

                        )

                );

            });

            pausa.play();

            retraso += intervalo;

        }

        // =====================================================

        // RESULTADO FINAL

        // =====================================================

        PauseTransition fin =

                new PauseTransition(

                        Duration.millis(

                                retraso + 800

                        )

                );

        fin.setOnFinished(e -> {

            List<Jugador> clasificacion =

                    new ArrayList<>(

                            jugadores

                    );

            clasificacion.sort(

                    Collections.reverseOrder()

            );

            // =================================================

            // MOSTRAR PUESTOS

            // =================================================

            for (int i = 0;

                 i < clasificacion.size();

                 i++) {

                Jugador jugador =

                        clasificacion.get(

                                i

                        );

                JugadorVista vista =

                        buscarVistaJugador(

                                jugador.getId()

                        );

                if (vista != null) {

                    vista.mostrarResultado(

                            i + 1,

                            jugador.getNombreMano(),

                            i == 0

                    );

                }

            }

            // =================================================

            // DESTACAR GANADOR

            // =================================================

            if (!clasificacion.isEmpty()) {

                destacarManoGanadora(

                        clasificacion.get(0)

                );

            }

            // =================================================

            // RESULTADO DE TEXTO

            // =================================================

            resultado.clear();

            for (int i = 0;

                 i < clasificacion.size();

                 i++) {

                Jugador jugador =

                        clasificacion.get(

                                i

                        );

                resultado.appendText(

                        (i + 1)

                                + "º "

                                + jugador.getId()

                                + ": "

                                + Utils.cartasAString(

                                        jugador

                                                .getMejorMano()

                                                .getCartas()

                                )

                                + " ("

                                + jugador.getNombreMano()

                                + ")"

                                + System.lineSeparator()

                );

            }

            avanzarMano(

                    lineas

            );

            boton.setDisable(

                    false

            );

        });

        fin.play();

    }

    // =========================================================

    // DESTACAR MEJOR MANO APARTADO 2

    // =========================================================

    private void destacarMejorManoApartado2(

            List<Carta> mejorMano) {

        List<CartaVista> todas =

                new ArrayList<>();

        todas.addAll(

                jugadorVistaApartado2

                        .getCartasVista()

        );

        todas.addAll(

                mesaPoker

                        .getCartasCentro()

        );

        destacarCartas(

                mejorMano,

                todas

        );

    }

    // =========================================================

    // DESTACAR GANADOR APARTADO 3

    // =========================================================

    private void destacarManoGanadora(

            Jugador ganador) {

        JugadorVista vistaGanador =

                buscarVistaJugador(

                        ganador.getId()

                );

        if (vistaGanador == null) {

            return;

        }

        List<CartaVista> posibles =

                new ArrayList<>();

        posibles.addAll(

                vistaGanador

                        .getCartasVista()

        );

        posibles.addAll(

                mesaPoker

                        .getCartasCentro()

        );

        destacarCartas(

                ganador

                        .getMejorMano()

                        .getCartas(),

                posibles

        );

    }

    // =========================================================

    // DESTACAR CARTAS

    // =========================================================

    private void destacarCartas(

            List<Carta> mejorMano,

            List<CartaVista> vistas) {

        for (CartaVista vista : vistas) {

            vista.quitarDestacado();

        }

        for (Carta cartaMejor :

                mejorMano) {

            for (CartaVista vista :

                    vistas) {

                if (vista.representa(

                        cartaMejor

                )) {

                    vista.destacar();

                    break;

                }

            }

        }

    }

    // =========================================================

    // BUSCAR VISTA DE JUGADOR

    // =========================================================

    private JugadorVista buscarVistaJugador(

            String id) {

        for (JugadorVista vista :

                jugadoresVistaApartado3) {

            if (vista

                    .getIdJugador()

                    .equals(id)) {

                return vista;

            }

        }

        return null;

    }

    // =========================================================

    // REPARTIR DESDE EL MAZO

    // =========================================================

    private void repartirDesdeMazo(
            CartaVista mazo,
            CartaVista destino,
            Carta carta) {

        /*
         * Ahora el reparto lo controla CrupierVista.
         */
        crupier.repartirCarta(
                destino,
                carta
        );
    }

    // =========================================================

    // CREAR SÍMBOLO DECORATIVO

    // =========================================================

    private Label crearSimboloFondo(

            String simbolo) {

        Label label =

                new Label(

                        simbolo

                );

        String color;

        /*

         * Corazones y diamantes:

         * rojo vino.

         */

        if (simbolo.equals("♥")

                || simbolo.equals("♦")) {

            color = "#B83232";

        }

        /*

         * Picas y tréboles:

         * dorado.

         */

        else {

            color = "#D6B85A";

        }

        label.setStyle(

                "-fx-font-size: 150px;" +

                "-fx-font-family: 'Georgia';" +

                "-fx-text-fill: "

                        + color

                        + ";"

        );

        /*

         * Marca de agua.

         *

         * Si quieres que se vean más:

         * 0.09 -> 0.15

         *

         * Si quieres que se vean menos:

         * 0.09 -> 0.05

         */

        label.setOpacity(

                0.09

        );

        // =====================================================

        // ROTACIONES

        // =====================================================

        switch (simbolo) {

            case "♠":

                label.setRotate(

                        -15

                );

                break;

            case "♥":

                label.setRotate(

                        12

                );

                break;

            case "♦":

                label.setRotate(

                        -12

                );

                break;

            case "♣":

                label.setRotate(

                        15

                );

                break;

        }

        /*

         * Son decoración.

         *

         * No deben impedir pulsar botones

         * o interactuar con otros elementos.

         */

        label.setMouseTransparent(

                true

        );

        return label;

    }

    // =========================================================

    // MOSTRAR RESULTADO VISUAL

    // =========================================================

    private void mostrarResultadoVisual(

            Label label) {

        label.setVisible(

                true

        );

        label.setManaged(

                true

        );

    }

    // =========================================================

    // OCULTAR RESULTADO VISUAL

    // =========================================================

    private void ocultarResultadoVisual(

            Label label) {

        label.setVisible(

                false

        );

        label.setManaged(

                false

        );

    }

    // =========================================================

    // OBTENER LÍNEAS

    // =========================================================

    private List<String> obtenerLineas(

            TextArea entrada) {

        return entrada

                .getText()

                .lines()

                .filter(

                        linea ->

                                !linea.isBlank()

                )

                .toList();

    }

    // =========================================================

    // AVANZAR MANO

    // =========================================================

    private void avanzarMano(

            List<String> lineas) {

        indiceManoActual++;

        if (indiceManoActual

                >= lineas.size()) {

            indiceManoActual = 0;

        }

    }

    // =========================================================

    // CARGAR FICHERO

    // =========================================================

    private void cargarFichero(

            String nombre,

            TextArea entrada,

            TextArea resultado,

            Label contador) {

        try {

            List<String> lineas =

                    Utils.leerFichero(

                            nombre

                    );

            entrada.clear();

            for (String linea :

                    lineas) {

                entrada.appendText(

                        linea

                                + System.lineSeparator()

                );

            }

            resultado.clear();

            indiceManoActual = 0;

            contador.setText(

                    "Mano 1 / "

                            + lineas.size()

            );

        } catch (IOException e) {

            entrada.setText(

                    "Error al cargar "

                            + nombre

                            + ": "

                            + e.getMessage()

            );

        }

    }

    // =========================================================

    // MAIN

    // =========================================================

    public static void main(

            String[] args) {

        launch(args);

    }

}