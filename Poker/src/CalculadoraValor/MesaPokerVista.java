package CalculadoraValor;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;


public class MesaPokerVista extends Pane {

    // =========================================================
    // TAMAÑO GENERAL
    // =========================================================

    private static final double ANCHO = 850;
    private static final double ALTO = 350;

    private static final double CENTRO_X = ANCHO / 2;
    private static final double CENTRO_Y = ALTO / 2;


    // =========================================================
    // TAMAÑO DEL TAPETE
    // =========================================================

    private static final double RADIO_MESA_X = 310;
    private static final double RADIO_MESA_Y = 110;


    // =========================================================
    // POSICIÓN DE LOS JUGADORES DEL APARTADO 3
    // =========================================================

    private static final double RADIO_JUGADORES_X = 350;
    private static final double RADIO_JUGADORES_Y = 135;


    // =========================================================
    // ELEMENTOS
    // =========================================================

    private Ellipse tapete;

    private Label tituloCentro;

    private HBox zonaCartasCentro;

    private List<CartaVista> cartasCentro;

    private JugadorVista jugadorPrincipal;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public MesaPokerVista() {

        setPrefSize(ANCHO, ALTO);

        setMinSize(ANCHO, ALTO);

        setMaxSize(ANCHO, ALTO);


        // =====================================================
        // TAPETE
        // =====================================================

        tapete = new Ellipse(CENTRO_X, CENTRO_Y, RADIO_MESA_X, RADIO_MESA_Y);


        tapete.setFill(Color.web("#176B3A"));


        tapete.setStroke(Color.web("#4A2C17"));


        tapete.setStrokeWidth(8);


        getChildren().add(tapete);


        // =====================================================
        // TÍTULO CENTRAL
        // =====================================================

        tituloCentro = new Label("CARTAS");


        tituloCentro.setStyle("-fx-font-size: 12px;" + "-fx-font-weight: bold;" + "-fx-text-fill: white;");


        tituloCentro.setPrefWidth(300);


        tituloCentro.setAlignment(Pos.CENTER);


        tituloCentro.setLayoutX(CENTRO_X - 150);


        tituloCentro.setLayoutY(CENTRO_Y - 75);


        getChildren().add(tituloCentro);


        // =====================================================
        // ZONA CENTRAL DE CARTAS
        // =====================================================

        cartasCentro = new ArrayList<>();


        zonaCartasCentro = new HBox(7);


        zonaCartasCentro.setAlignment(Pos.CENTER);


        for (int i = 0; i < 5; i++) {

            CartaVista carta = new CartaVista();


            carta.setVisible(false);


            cartasCentro.add(carta);


            zonaCartasCentro
                    .getChildren()
                    .add(carta);
        }


        double anchoCartas = 5 * 65 + 4 * 7;


        zonaCartasCentro.setPrefWidth(anchoCartas);


        zonaCartasCentro.setLayoutX(CENTRO_X - anchoCartas / 2);


        zonaCartasCentro.setLayoutY(CENTRO_Y - 45);


        getChildren().add(zonaCartasCentro);
    }


    // =========================================================
    // MODO APARTADO 1
    // =========================================================

    public void prepararApartado1() {

        limpiarJugadores();


        jugadorPrincipal = null;


        tituloCentro.setText("MANO");


        tituloCentro.setVisible(true);


        reiniciarCartasCentro();
    }


    // =========================================================
    // MODO APARTADO 2
    // =========================================================

    public void prepararApartado2(JugadorVista jugador) {


        limpiarJugadores();


        jugadorPrincipal = jugador;


        tituloCentro.setText("CARTAS COMUNITARIAS");


        tituloCentro.setVisible(true);


        reiniciarCartasCentro();


        /*
         * Colocamos al jugador en la parte
         * inferior de la mesa.
         */

        jugador.setLayoutX(CENTRO_X - 70);


        jugador.setLayoutY(ALTO - 110);


        getChildren().add(jugador);


        /*
         * Las cartas comunitarias deben
         * quedar por delante del tapete.
         */

        zonaCartasCentro.toFront();

        tituloCentro.toFront();

        jugador.toFront();
    }


    // =========================================================
    // MODO APARTADO 3
    // =========================================================

    public void prepararApartado3(List<JugadorVista> jugadores) {


        limpiarJugadores();


        jugadorPrincipal = null;


        tituloCentro.setText("CARTAS COMUNITARIAS");


        tituloCentro.setVisible(true);


        reiniciarCartasCentro();


        colocarJugadores(jugadores);
    }


    // =========================================================
    // COLOCAR JUGADORES
    // =========================================================

    public void colocarJugadores(List<JugadorVista> jugadores) {


        int numeroJugadores = jugadores.size();


        if (numeroJugadores == 0) {

            return;
        }


        /*
         * J1 empieza abajo.
         *
         * Los siguientes se distribuyen
         * alrededor de la mesa.
         */

        double anguloInicial = Math.PI / 2;


        double paso = (2 * Math.PI)
                        / numeroJugadores;


        for (int i = 0;
             i < numeroJugadores;
             i++) {


            JugadorVista jugador = jugadores.get(i);


            double angulo = anguloInicial + i * paso;


            double x = CENTRO_X + RADIO_JUGADORES_X * Math.cos(angulo);


            double y = CENTRO_Y + RADIO_JUGADORES_Y * Math.sin(angulo);


            jugador.setLayoutX(x - 70);


            jugador.setLayoutY(y - 55);


            getChildren().add(jugador);
        }


        zonaCartasCentro.toFront();

        tituloCentro.toFront();
    }


    // =========================================================
    // LIMPIAR JUGADORES
    // =========================================================

    private void limpiarJugadores() {

        getChildren()
                .removeIf(nodo -> nodo instanceof JugadorVista);
    }


    // =========================================================
    // CARTAS CENTRALES
    // =========================================================

    public List<CartaVista> getCartasCentro() {

        return cartasCentro;
    }


    public CartaVista getCartaCentro(int indice) {

        return cartasCentro.get(indice);
    }


    // =========================================================
    // REINICIAR CARTAS CENTRALES
    // =========================================================

    public void reiniciarCartasCentro() {

        for (CartaVista carta : cartasCentro) {


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
    // COMPATIBILIDAD
    // =========================================================

    public List<CartaVista> getCartasComunitarias() {

        return cartasCentro;
    }


    public CartaVista getCartaComunitaria(int indice) {

        return cartasCentro.get(indice);
    }


    public void reiniciarComunitarias() {

        reiniciarCartasCentro();
    }
}
