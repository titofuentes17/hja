package CalculadoraValor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    // ELEMENTOS DEL APARTADO 3
    // =========================================================

    private List<JugadorVista> jugadoresVistaApartado3 =
            new ArrayList<>();


    private List<CartaVista> comunitariasApartado3 =
            new ArrayList<>();


    private VBox zonaJugadoresApartado3;

    private HBox filaComunitariasApartado3;

    private Label tituloComunitariasApartado3;


    // =========================================================
    // START
    // =========================================================

    @Override
    public void start(Stage escenario) {


        // =====================================================
        // PANEL PRINCIPAL
        // =====================================================

        BorderPane raiz =
                new BorderPane();


        raiz.setPadding(
                new Insets(10)
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
                "-fx-font-weight: bold;"
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
                        7,
                        titulo,
                        opciones
                );


        parteSuperior.setAlignment(
                Pos.CENTER
        );


        parteSuperior.setPadding(
                new Insets(
                        0,
                        0,
                        5,
                        0
                )
        );


        raiz.setTop(
                parteSuperior
        );


        // =====================================================
        // CRUPIER / MAZO
        // =====================================================

        Label tituloMazo =
                new Label(
                        "CRUPIER"
                );


        tituloMazo.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;"
        );


        CartaVista mazo =
                new CartaVista();


        mazo.setScaleX(0.75);
        mazo.setScaleY(0.75);


        // =====================================================
        // APARTADO 1
        // =====================================================

        CartaVista carta1 =
                new CartaVista();

        CartaVista carta2 =
                new CartaVista();

        CartaVista carta3 =
                new CartaVista();

        CartaVista carta4 =
                new CartaVista();

        CartaVista carta5 =
                new CartaVista();


        List<CartaVista> cartasApartado1 =
                List.of(
                        carta1,
                        carta2,
                        carta3,
                        carta4,
                        carta5
                );


        HBox filaApartado1 =
                new HBox(
                        7,
                        carta1,
                        carta2,
                        carta3,
                        carta4,
                        carta5
                );


        filaApartado1.setAlignment(
                Pos.CENTER
        );


        // =====================================================
        // APARTADO 2 - CARTAS DEL JUGADOR
        // =====================================================

        CartaVista jugador1 =
                new CartaVista();

        CartaVista jugador2 =
                new CartaVista();


        List<CartaVista> cartasJugador =
                List.of(
                        jugador1,
                        jugador2
                );


        HBox filaJugador =
                new HBox(
                        7,
                        jugador1,
                        jugador2
                );


        filaJugador.setAlignment(
                Pos.CENTER
        );


        Label tituloJugador =
                new Label(
                        "TU MANO"
                );


        tituloJugador.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;"
        );


        // =====================================================
        // APARTADO 2 - COMUNITARIAS
        // =====================================================

        CartaVista comun1 =
                new CartaVista();

        CartaVista comun2 =
                new CartaVista();

        CartaVista comun3 =
                new CartaVista();

        CartaVista comun4 =
                new CartaVista();

        CartaVista comun5 =
                new CartaVista();


        List<CartaVista> cartasComunitarias =
                List.of(
                        comun1,
                        comun2,
                        comun3,
                        comun4,
                        comun5
                );


        HBox filaComunitarias =
                new HBox(
                        7,
                        comun1,
                        comun2,
                        comun3,
                        comun4,
                        comun5
                );


        filaComunitarias.setAlignment(
                Pos.CENTER
        );


        Label tituloComunitarias =
                new Label(
                        "CARTAS COMUNITARIAS"
                );


        tituloComunitarias.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;"
        );


        // =====================================================
        // MEJOR MANO APARTADO 2
        // =====================================================

        Label mejorManoVisual =
                new Label();


        mejorManoVisual.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;"
        );


        mejorManoVisual.setVisible(
                false
        );


        // =====================================================
        // CONTADOR
        // =====================================================

        Label contadorManos =
                new Label(
                        "Mano 1"
                );


        contadorManos.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;"
        );


        // =====================================================
        // REPARTIR
        // =====================================================

        Button botonRepartir =
                new Button(
                        "▶ REPARTIR"
                );


        // =====================================================
        // ZONA JUEGO
        // =====================================================

        VBox zonaJuego =
                new VBox(4);


        zonaJuego.setAlignment(
                Pos.CENTER
        );


        zonaJuego.setPadding(
                new Insets(3)
        );


        raiz.setCenter(
                zonaJuego
        );


        // =====================================================
        // ENTRADA
        // =====================================================

        Label tituloEntrada =
                new Label(
                        "Fichero de entrada"
                );


        tituloEntrada.setStyle(
                "-fx-font-size: 11px;"
        );


        TextArea entrada =
                new TextArea();


        entrada.setPromptText(
                "Aquí aparecerá el contenido del fichero..."
        );


        entrada.setEditable(false);

        entrada.setPrefRowCount(4);
        entrada.setPrefHeight(90);
        entrada.setPrefWidth(400);


        VBox zonaEntrada =
                new VBox(
                        4,
                        tituloEntrada,
                        entrada
                );


        // =====================================================
        // RESULTADO
        // =====================================================

        Label tituloResultado =
                new Label(
                        "Resultado"
                );


        tituloResultado.setStyle(
                "-fx-font-size: 11px;"
        );


        TextArea resultado =
                new TextArea();


        resultado.setPromptText(
                "Aquí aparecerá el resultado..."
        );


        resultado.setEditable(false);

        resultado.setPrefRowCount(4);
        resultado.setPrefHeight(90);
        resultado.setPrefWidth(400);


        VBox zonaResultado =
                new VBox(
                        4,
                        tituloResultado,
                        resultado
                );


        HBox parteInferior =
                new HBox(
                        10,
                        zonaEntrada,
                        zonaResultado
                );


        parteInferior.setAlignment(
                Pos.CENTER
        );


        parteInferior.setPadding(
                new Insets(
                        5,
                        0,
                        0,
                        0
                )
        );


        raiz.setBottom(
                parteInferior
        );


        // =====================================================
        // OCULTAR CARTAS
        // =====================================================

        ocultarCartas(
                cartasApartado1
        );


        ocultarCartas(
                cartasJugador
        );


        ocultarCartas(
                cartasComunitarias
        );


        // =====================================================
        // APARTADO 1 INICIAL
        // =====================================================

        mostrarInterfazApartado1(
                zonaJuego,
                tituloMazo,
                mazo,
                filaApartado1,
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

            mejorManoVisual.setVisible(false);


            ocultarCartas(
                    cartasApartado1
            );


            ocultarCartas(
                    cartasJugador
            );


            ocultarCartas(
                    cartasComunitarias
            );


            String apartado =
                    selectorApartado
                            .getValue();


            // ================================================
            // APARTADO 1
            // ================================================

            if (apartado
                    .startsWith(
                            "Apartado 1"
                    )) {


                mostrarInterfazApartado1(
                        zonaJuego,
                        tituloMazo,
                        mazo,
                        filaApartado1,
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


            // ================================================
            // APARTADO 2
            // ================================================

            else if (apartado
                    .startsWith(
                            "Apartado 2"
                    )) {


                mostrarInterfazApartado2(
                        zonaJuego,
                        tituloMazo,
                        mazo,
                        tituloComunitarias,
                        filaComunitarias,
                        tituloJugador,
                        filaJugador,
                        mejorManoVisual,
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


            // ================================================
            // APARTADO 3
            // ================================================

            else {


                mostrarInterfazApartado3(
                        zonaJuego,
                        tituloMazo,
                        mazo,
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

            FileChooser selectorFichero =
                    new FileChooser();


            selectorFichero.setTitle(
                    "Seleccionar fichero de entrada"
            );


            selectorFichero
                    .getExtensionFilters()
                    .add(

                            new FileChooser.ExtensionFilter(

                                    "Ficheros de texto (*.txt)",

                                    "*.txt"
                            )
                    );


            File fichero =
                    selectorFichero
                            .showOpenDialog(
                                    escenario
                            );


            if (fichero != null) {

                try {

                    List<String> lineas =
                            Utils.leerFichero(
                                    fichero
                                            .getAbsolutePath()
                            );


                    entrada.clear();


                    for (String linea
                            : lineas) {

                        entrada.appendText(
                                linea
                                        + System.lineSeparator()
                        );
                    }


                    indiceManoActual = 0;

                    resultado.clear();

                    mejorManoVisual.setVisible(false);


                    contadorManos.setText(
                            "Mano 1 / "
                                    + lineas.size()
                    );


                    ocultarCartas(
                            cartasApartado1
                    );


                    ocultarCartas(
                            cartasJugador
                    );


                    ocultarCartas(
                            cartasComunitarias
                    );


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
                    selectorApartado
                            .getValue();


            // ================================================
            // APARTADO 1
            // ================================================

            if (apartado
                    .startsWith(
                            "Apartado 1"
                    )) {


                repartirApartado1(
                        entrada,
                        resultado,
                        contadorManos,
                        botonRepartir,
                        mazo,
                        cartasApartado1
                );
            }


            // ================================================
            // APARTADO 2
            // ================================================

            else if (apartado
                    .startsWith(
                            "Apartado 2"
                    )) {


                repartirApartado2(
                        entrada,
                        resultado,
                        contadorManos,
                        botonRepartir,
                        mazo,
                        cartasJugador,
                        cartasComunitarias,
                        mejorManoVisual
                );
            }


            // ================================================
            // APARTADO 3
            // ================================================

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
        // VENTANA
        // =====================================================

        Scene escena =
                new Scene(
                        raiz,
                        900,
                        650
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
    // INTERFAZ APARTADO 1
    // =========================================================

    private void mostrarInterfazApartado1(
            VBox zonaJuego,
            Label tituloMazo,
            CartaVista mazo,
            HBox filaCartas,
            Label contador,
            Button botonRepartir) {


        zonaJuego
                .getChildren()
                .clear();


        zonaJuego.setSpacing(5);


        Label tituloMesa =
                new Label(
                        "MESA"
                );


        tituloMesa.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;"
        );


        zonaJuego
                .getChildren()
                .addAll(
                        tituloMazo,
                        mazo,
                        tituloMesa,
                        filaCartas,
                        contador,
                        botonRepartir
                );
    }


    // =========================================================
    // INTERFAZ APARTADO 2
    // =========================================================

    private void mostrarInterfazApartado2(
            VBox zonaJuego,
            Label tituloMazo,
            CartaVista mazo,
            Label tituloComunitarias,
            HBox filaComunitarias,
            Label tituloJugador,
            HBox filaJugador,
            Label mejorMano,
            Label contador,
            Button botonRepartir) {


        zonaJuego
                .getChildren()
                .clear();


        zonaJuego.setSpacing(3);


        zonaJuego
                .getChildren()
                .addAll(
                        tituloMazo,
                        mazo,
                        tituloComunitarias,
                        filaComunitarias,
                        tituloJugador,
                        filaJugador,
                        mejorMano,
                        contador,
                        botonRepartir
                );
    }


    // =========================================================
    // INTERFAZ APARTADO 3
    // =========================================================

    private void mostrarInterfazApartado3(
            VBox zonaJuego,
            Label tituloMazo,
            CartaVista mazo,
            Label contador,
            Button botonRepartir) {


        zonaJuego
                .getChildren()
                .clear();


        zonaJuego.setSpacing(3);


        // -----------------------------------------
        // ZONA JUGADORES
        // -----------------------------------------

        zonaJugadoresApartado3 =
                new VBox(4);


        zonaJugadoresApartado3.setAlignment(
                Pos.CENTER
        );


        Label mensaje =
                new Label(
                        "Pulsa REPARTIR para sentar a los jugadores"
                );


        mensaje.setStyle(
                "-fx-font-size: 11px;"
        );


        zonaJugadoresApartado3
                .getChildren()
                .add(
                        mensaje
                );


        // -----------------------------------------
        // COMUNITARIAS
        // -----------------------------------------

        tituloComunitariasApartado3 =
                new Label(
                        "CARTAS COMUNITARIAS"
                );


        tituloComunitariasApartado3.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;"
        );


        comunitariasApartado3 =
                new ArrayList<>();


        for (int i = 0; i < 5; i++) {

            CartaVista carta =
                    new CartaVista();


            carta.setVisible(false);


            comunitariasApartado3.add(
                    carta
            );
        }


        filaComunitariasApartado3 =
                new HBox(7);


        filaComunitariasApartado3.setAlignment(
                Pos.CENTER
        );


        filaComunitariasApartado3
                .getChildren()
                .addAll(
                        comunitariasApartado3
                );


        // -----------------------------------------
        // TODO
        // -----------------------------------------

        zonaJuego
                .getChildren()
                .addAll(
                        tituloMazo,
                        mazo,
                        zonaJugadoresApartado3,
                        tituloComunitariasApartado3,
                        filaComunitariasApartado3,
                        contador,
                        botonRepartir
                );
    }


    // =========================================================
    // CREAR JUGADORES VISUALES APARTADO 3
    // =========================================================

    private void crearJugadoresVisuales(
            List<String> ids) {


        jugadoresVistaApartado3.clear();


        zonaJugadoresApartado3
                .getChildren()
                .clear();


        // Creamos todos los jugadores.
        for (String id : ids) {

            JugadorVista vista =
                    new JugadorVista(id);


            jugadoresVistaApartado3.add(
                    vista
            );
        }


        // -----------------------------------------
        // DOS FILAS
        // -----------------------------------------

        HBox filaSuperior =
                new HBox(20);


        HBox filaInferior =
                new HBox(20);


        filaSuperior.setAlignment(
                Pos.CENTER
        );


        filaInferior.setAlignment(
                Pos.CENTER
        );


        int mitad =
                (jugadoresVistaApartado3.size() + 1)
                        / 2;


        for (int i = 0;
             i < jugadoresVistaApartado3.size();
             i++) {


            if (i < mitad) {

                filaSuperior
                        .getChildren()
                        .add(
                                jugadoresVistaApartado3
                                        .get(i)
                        );

            } else {

                filaInferior
                        .getChildren()
                        .add(
                                jugadoresVistaApartado3
                                        .get(i)
                        );
            }
        }


        zonaJugadoresApartado3
                .getChildren()
                .add(
                        filaSuperior
                );


        if (!filaInferior
                .getChildren()
                .isEmpty()) {


            zonaJugadoresApartado3
                    .getChildren()
                    .add(
                            filaInferior
                    );
        }
    }


    // =========================================================
    // REPARTIR APARTADO 1
    // =========================================================

    private void repartirApartado1(
            TextArea entrada,
            TextArea resultado,
            Label contador,
            Button boton,
            CartaVista mazo,
            List<CartaVista> cartasVisuales) {


        List<String> lineas =
                obtenerLineas(
                        entrada
                );


        if (lineas.isEmpty()) {
            return;
        }


        if (indiceManoActual
                >= lineas.size()) {

            indiceManoActual = 0;
        }


        String lineaActual =
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
                        lineaActual
                );


        resultado.clear();

        boton.setDisable(true);


        ocultarCartas(
                cartasVisuales
        );


        // -----------------------------------------
        // REPARTIR
        // -----------------------------------------

        for (int i = 0;
             i < cartas.size()
                     && i < cartasVisuales.size();
             i++) {


            final int indice = i;


            PauseTransition pausa =
                    new PauseTransition(

                            Duration.millis(
                                    indice * 700
                            )
                    );


            pausa.setOnFinished(e -> {

                repartirDesdeMazo(
                        mazo,
                        cartasVisuales
                                .get(indice),
                        cartas
                                .get(indice)
                );
            });


            pausa.play();
        }


        String mejorMano =
                Evaluador
                        .obtenerMejorManoTexto(
                                cartas
                        );


        List<String> draws =
                Evaluador
                        .obtenerDraws(
                                cartas
                        );


        PauseTransition finalReparto =
                new PauseTransition(
                        Duration.millis(4000)
                );


        finalReparto.setOnFinished(e -> {

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


            boton.setDisable(false);
        });


        finalReparto.play();
    }


    // =========================================================
    // REPARTIR APARTADO 2
    // =========================================================

    private void repartirApartado2(
            TextArea entrada,
            TextArea resultado,
            Label contador,
            Button boton,
            CartaVista mazo,
            List<CartaVista> cartasJugador,
            List<CartaVista> cartasComunitarias,
            Label mejorManoVisual) {


        List<String> lineas =
                obtenerLineas(
                        entrada
                );


        if (lineas.isEmpty()) {
            return;
        }


        if (indiceManoActual
                >= lineas.size()) {

            indiceManoActual = 0;
        }


        String lineaActual =
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
                lineaActual.split(";");


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


        int numComunes =
                Integer.parseInt(
                        partes[1]
                );


        List<Carta> comunes =
                Utils.parsearCartas(
                        partes[2]
                );


        if (comunes.size()
                > numComunes) {


            comunes =
                    new ArrayList<>(

                            comunes.subList(
                                    0,
                                    numComunes
                            )
                    );
        }


        final List<Carta> comunesFinal =
                comunes;


        // -----------------------------------------
        // CALCULAR MANO
        // -----------------------------------------

        Jugador jugador =
                new Jugador(
                        propias,
                        comunesFinal
                );


        List<Carta> cartasMejorMano =
                jugador
                        .getMejorMano()
                        .getCartas();


        String nombreMejorMano =
                jugador.getNombreMano();


        String mejorManoTexto =
                jugador.getMejorManoTexto();


        List<String> draws =
                jugador.getDraws();


        // -----------------------------------------
        // REINICIAR
        // -----------------------------------------

        resultado.clear();

        mejorManoVisual.setVisible(false);

        boton.setDisable(true);


        ocultarCartas(
                cartasJugador
        );


        ocultarCartas(
                cartasComunitarias
        );


        // -----------------------------------------
        // CARTAS PROPIAS
        // -----------------------------------------

        for (int i = 0;
             i < propias.size()
                     && i < cartasJugador.size();
             i++) {


            final int indice = i;


            PauseTransition pausa =
                    new PauseTransition(

                            Duration.millis(
                                    indice * 700
                            )
                    );


            pausa.setOnFinished(e -> {

                repartirDesdeMazo(
                        mazo,
                        cartasJugador
                                .get(indice),
                        propias
                                .get(indice)
                );
            });


            pausa.play();
        }


        // -----------------------------------------
        // COMUNITARIAS
        // -----------------------------------------

        for (int i = 0;
             i < comunesFinal.size()
                     && i < cartasComunitarias.size();
             i++) {


            final int indice = i;


            PauseTransition pausa =
                    new PauseTransition(

                            Duration.millis(
                                    1400
                                            + indice * 700
                            )
                    );


            pausa.setOnFinished(e -> {

                repartirDesdeMazo(
                        mazo,
                        cartasComunitarias
                                .get(indice),
                        comunesFinal
                                .get(indice)
                );
            });


            pausa.play();
        }


        long tiempoFinal =
                1400
                        + comunesFinal.size() * 700L
                        + 700;


        PauseTransition finalReparto =
                new PauseTransition(

                        Duration.millis(
                                tiempoFinal
                        )
                );


        finalReparto.setOnFinished(e -> {

            mejorManoVisual.setText(
                    "★ MEJOR MANO: "
                            + nombreMejorMano
            );


            mejorManoVisual.setVisible(true);


            destacarMejorMano(
                    cartasMejorMano,
                    cartasJugador,
                    cartasComunitarias
            );


            resultado.appendText(
                    "- Best hand: "
                            + mejorManoTexto
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


            boton.setDisable(false);
        });


        finalReparto.play();
    }


    // =========================================================
    // REPARTIR APARTADO 3
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


        if (indiceManoActual
                >= lineas.size()) {

            indiceManoActual = 0;
        }


        String lineaActual =
                lineas.get(
                        indiceManoActual
                );


        contador.setText(
                "Mano "
                        + (indiceManoActual + 1)
                        + " / "
                        + lineas.size()
        );


        // =========================================
        // LEER LÍNEA
        // =========================================

        String[] partes =
                lineaActual.split(";");


        int numeroJugadores =
                Integer.parseInt(
                        partes[0]
                );


        // =========================================
        // JUGADORES
        // =========================================

        List<String> ids =
                new ArrayList<>();


        List<List<Carta>> cartasJugadores =
                new ArrayList<>();


        for (int i = 1;
             i <= numeroJugadores;
             i++) {


            String parteJugador =
                    partes[i];


            // Las últimas cuatro posiciones
            // corresponden a las dos cartas.
            String id =
                    parteJugador.substring(
                            0,
                            parteJugador.length() - 4
                    );


            String textoCartas =
                    parteJugador.substring(
                            parteJugador.length() - 4
                    );


            ids.add(id);


            cartasJugadores.add(

                    Utils.parsearCartas(
                            textoCartas
                    )
            );
        }


        // =========================================
        // COMUNITARIAS
        // =========================================

        List<Carta> comunes =
                Utils.parsearCartas(

                        partes[
                                partes.length - 1
                        ]
                );


        // =========================================
        // CREAR JUGADORES VISUALES
        // =========================================

        crearJugadoresVisuales(
                ids
        );


        // =========================================
        // REINICIAR COMUNITARIAS
        // =========================================

        for (CartaVista carta
                : comunitariasApartado3) {


            carta.quitarDestacado();

            carta.setVisible(false);

            carta.setTranslateX(0);
            carta.setTranslateY(0);

            carta.setScaleX(1);
            carta.setScaleY(1);

            carta.setRotate(0);
        }


        resultado.clear();

        boton.setDisable(true);


        // =========================================
        // REPARTO REAL
        //
        // 1ª vuelta:
        // J1 J2 J3 J4...
        //
        // 2ª vuelta:
        // J1 J2 J3 J4...
        // =========================================

        long retraso = 0;

        final long intervalo = 450;


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

                    JugadorVista jugadorVista =
                            jugadoresVistaApartado3
                                    .get(
                                            indiceJugador
                                    );


                    CartaVista destino =
                            jugadorVista
                                    .getCartaVista(
                                            indiceCarta
                                    );


                    Carta carta =
                            cartasJugadores
                                    .get(
                                            indiceJugador
                                    )
                                    .get(
                                            indiceCarta
                                    );


                    repartirDesdeMazo(
                            mazo,
                            destino,
                            carta
                    );
                });


                pausa.play();


                retraso += intervalo;
            }
        }


        // =========================================
        // CINCO COMUNITARIAS
        // =========================================

        for (int i = 0;
             i < comunes.size()
                     && i < comunitariasApartado3.size();
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
                        comunitariasApartado3
                                .get(indice),
                        comunes
                                .get(indice)
                );
            });


            pausa.play();


            retraso += intervalo;
        }


        // =========================================
        // FINAL
        // =========================================

        PauseTransition finalReparto =
                new PauseTransition(

                        Duration.millis(
                                retraso + 700
                        )
                );


        finalReparto.setOnFinished(e -> {

            resultado.appendText(
                    "Reparto completado."
                            + System.lineSeparator()
            );


            resultado.appendText(
                    numeroJugadores
                            + " jugadores."
                            + System.lineSeparator()
            );


            avanzarMano(
                    lineas
            );


            boton.setDisable(false);
        });


        finalReparto.play();
    }


    // =========================================================
    // DESTACAR MEJOR MANO APARTADO 2
    // =========================================================

    private void destacarMejorMano(
            List<Carta> mejorMano,
            List<CartaVista> cartasJugador,
            List<CartaVista> cartasComunitarias) {


        for (CartaVista vista
                : cartasJugador) {

            vista.quitarDestacado();
        }


        for (CartaVista vista
                : cartasComunitarias) {

            vista.quitarDestacado();
        }


        List<CartaVista> todas =
                new ArrayList<>();


        todas.addAll(
                cartasJugador
        );


        todas.addAll(
                cartasComunitarias
        );


        for (Carta cartaMejor
                : mejorMano) {


            for (CartaVista vista
                    : todas) {


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
    // REPARTIR DESDE EL MAZO
    // =========================================================

    private void repartirDesdeMazo(
            CartaVista mazo,
            CartaVista destino,
            Carta carta) {


        Bounds posicionMazo =
                mazo.localToScene(
                        mazo.getBoundsInLocal()
                );


        Bounds posicionDestino =
                destino.localToScene(
                        destino.getBoundsInLocal()
                );


        double origenX =
                posicionMazo.getCenterX()
                        -
                        posicionDestino.getCenterX();


        double origenY =
                posicionMazo.getCenterY()
                        -
                        posicionDestino.getCenterY();


        destino.repartirDesde(
                carta,
                origenX,
                origenY
        );
    }


    // =========================================================
    // OCULTAR CARTAS
    // =========================================================

    private void ocultarCartas(
            List<CartaVista> cartas) {


        for (CartaVista carta
                : cartas) {


            carta.quitarDestacado();

            carta.setVisible(false);

            carta.setTranslateX(0);
            carta.setTranslateY(0);

            carta.setScaleX(1);
            carta.setScaleY(1);

            carta.setRotate(0);
        }
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
            String nombreFichero,
            TextArea entrada,
            TextArea resultado,
            Label contador) {


        try {


            List<String> lineas =
                    Utils.leerFichero(
                            nombreFichero
                    );


            entrada.clear();


            for (String linea
                    : lineas) {


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
                            + nombreFichero
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