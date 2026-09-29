package CalculadoraValor;

import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;


public class CartaVista extends StackPane {

    // =========================================================
    // TAMAÑO DE LA CARTA
    // =========================================================

    private static final double ANCHO = 65;
    private static final double ALTO = 90;


    // =========================================================
    // ELEMENTOS VISUALES
    // =========================================================

    /*
     * Este rectángulo sirve para:
     *
     * - mostrar el reverso de la carta
     * - poner el borde negro
     * - poner el borde dorado al destacar
     */
    private Rectangle fondo;


    /*
     * Aquí se mostrará el PNG de la carta.
     */
    private ImageView imagenCarta;


    /*
     * Carta que se está mostrando actualmente.
     *
     * null = estamos mostrando el reverso.
     */
    private Carta cartaActual;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public CartaVista() {

        setAlignment(
                Pos.CENTER
        );


        // =====================================================
        // FONDO / BORDE
        // =====================================================

        fondo =
                new Rectangle(
                        ANCHO,
                        ALTO
                );


        fondo.setArcWidth(12);
        fondo.setArcHeight(12);


        // =====================================================
        // IMAGEN
        // =====================================================

        imagenCarta =
                new ImageView();


        imagenCarta.setFitWidth(
                ANCHO - 4
        );


        imagenCarta.setFitHeight(
                ALTO - 4
        );


        /*
         * false porque queremos que todas
         * tengan exactamente el mismo tamaño.
         */
        imagenCarta.setPreserveRatio(
                false
        );


        imagenCarta.setSmooth(
                true
        );


        // =====================================================
        // AÑADIR ELEMENTOS
        // =====================================================

        getChildren().addAll(
                fondo,
                imagenCarta
        );


        // Empezamos mostrando el reverso.
        mostrarReverso();
    }


    // =========================================================
    // MOSTRAR REVERSO
    // =========================================================

    public void mostrarReverso() {

        cartaActual = null;


        /*
         * Ocultamos el PNG.
         */
        imagenCarta.setImage(
                null
        );


        imagenCarta.setVisible(
                false
        );


        /*
         * Reverso azul.
         */
        fondo.setFill(
                Color.web("#163A70")
        );


        fondo.setStroke(
                Color.WHITE
        );


        fondo.setStrokeWidth(
                2
        );
    }


    // =========================================================
    // MOSTRAR CARTA
    // =========================================================

    public void mostrarCarta(
            Carta carta) {


        cartaActual =
                carta;


        String ruta =
                obtenerRutaImagen(
                        carta
                );


        /*
         * Buscamos la imagen dentro del proyecto.
         *
         * Ejemplo:
         *
         * /CalculadoraValor/cartas/ace_of_hearts.png
         */
        var recurso =
                getClass()
                        .getResource(
                                ruta
                        );


        if (recurso == null) {

            /*
             * Si esto ocurre significa que la
             * imagen no está donde esperamos.
             */
            System.err.println(
                    "No se encontró la imagen: "
                            + ruta
            );


            mostrarReverso();

            return;
        }


        Image imagen =
                new Image(
                        recurso.toExternalForm()
                );


        imagenCarta.setImage(
                imagen
        );


        imagenCarta.setVisible(
                true
        );


        /*
         * Fondo blanco por si el PNG
         * tiene alguna transparencia.
         */
        fondo.setFill(
                Color.WHITE
        );


        fondo.setStroke(
                Color.BLACK
        );


        fondo.setStrokeWidth(
                2
        );
    }


    // =========================================================
    // CONVERTIR CARTA -> RUTA PNG
    // =========================================================

    private String obtenerRutaImagen(
            Carta carta) {


        String valor =
                convertirValor(
                        carta.getValor()
                );


        String palo =
                convertirPalo(
                        carta.getPalo()
                );


        /*
         * Ejemplos:
         *
         * Ah -> ace_of_hearts.png
         * Kd -> king_of_diamonds.png
         * Qs -> queen_of_spades.png
         * Jc -> jack_of_clubs.png
         * Th -> 10_of_hearts.png
         * 8s -> 8_of_spades.png
         */

        return "/CalculadoraValor/cartas/"
                + valor
                + "_of_"
                + palo
                + ".png";
    }


    // =========================================================
    // CONVERTIR VALOR
    // =========================================================

    private String convertirValor(
            char valor) {


        return switch (valor) {

            case 'A' ->
                    "ace";

            case 'K' ->
                    "king";

            case 'Q' ->
                    "queen";

            case 'J' ->
                    "jack";

            case 'T' ->
                    "10";

            default ->
                    String.valueOf(
                            valor
                    );
        };
    }


    // =========================================================
    // CONVERTIR PALO
    // =========================================================

    private String convertirPalo(
            char palo) {


        return switch (palo) {

            case 'h' ->
                    "hearts";

            case 'd' ->
                    "diamonds";

            case 'c' ->
                    "clubs";

            case 's' ->
                    "spades";

            default ->
                    "";
        };
    }


    // =========================================================
    // VOLTEAR CARTA
    // =========================================================

    public void voltear(
            Carta carta) {


        /*
         * Primera mitad:
         *
         * la carta gira de 0º a 90º.
         */
        RotateTransition primeraMitad =
                new RotateTransition(
                        Duration.millis(200),
                        this
                );


        primeraMitad.setAxis(
                Rotate.Y_AXIS
        );


        primeraMitad.setFromAngle(
                0
        );


        primeraMitad.setToAngle(
                90
        );


        // =====================================================
        // AL LLEGAR A 90º CAMBIAMOS LA IMAGEN
        // =====================================================

        primeraMitad.setOnFinished(
                evento -> {


                    mostrarCarta(
                            carta
                    );


                    /*
                     * Empezamos la segunda mitad
                     * desde -90º.
                     */
                    setRotate(
                            -90
                    );


                    RotateTransition segundaMitad =
                            new RotateTransition(
                                    Duration.millis(200),
                                    this
                            );


                    segundaMitad.setAxis(
                            Rotate.Y_AXIS
                    );


                    segundaMitad.setFromAngle(
                            -90
                    );


                    segundaMitad.setToAngle(
                            0
                    );


                    segundaMitad.play();
                }
        );


        primeraMitad.play();
    }


    // =========================================================
    // REPARTIR DESDE EL MAZO
    // =========================================================

    public void repartirDesde(
            Carta carta,
            double origenX,
            double origenY) {


        /*
         * Hacemos visible la carta.
         */
        setVisible(
                true
        );


        /*
         * Primero mostramos el reverso.
         */
        mostrarReverso();


        /*
         * Colocamos visualmente la carta
         * sobre el mazo.
         */
        setTranslateX(
                origenX
        );


        setTranslateY(
                origenY
        );


        setScaleX(
                0.75
        );


        setScaleY(
                0.75
        );


        // =====================================================
        // MOVIMIENTO DESDE EL CRUPIER
        // =====================================================

        TranslateTransition movimiento =
                new TranslateTransition(
                        Duration.millis(350),
                        this
                );


        movimiento.setToX(
                0
        );


        movimiento.setToY(
                0
        );


        // =====================================================
        // CUANDO LLEGA...
        // =====================================================

        movimiento.setOnFinished(
                evento -> {


                    setScaleX(
                            1
                    );


                    setScaleY(
                            1
                    );


                    /*
                     * Giramos la carta y
                     * mostramos el PNG.
                     */
                    voltear(
                            carta
                    );
                }
        );


        movimiento.play();
    }


    // =========================================================
    // DESTACAR CARTA
    // =========================================================

    public void destacar() {


        /*
         * Pequeño temblor horizontal.
         */
        TranslateTransition temblor =
                new TranslateTransition(
                        Duration.millis(65),
                        this
                );


        temblor.setFromX(
                -3
        );


        temblor.setToX(
                3
        );


        temblor.setAutoReverse(
                true
        );


        temblor.setCycleCount(
                6
        );


        temblor.setOnFinished(
                evento -> {


                    setTranslateX(
                            0
                    );


                    /*
                     * Borde dorado.
                     */
                    fondo.setStroke(
                            Color.GOLD
                    );


                    fondo.setStrokeWidth(
                            4
                    );
                }
        );


        temblor.play();
    }


    // =========================================================
    // QUITAR DESTACADO
    // =========================================================

    public void quitarDestacado() {


        setTranslateX(
                0
        );


        if (cartaActual != null) {

            fondo.setStroke(
                    Color.BLACK
            );


            fondo.setStrokeWidth(
                    2
            );

        } else {

            fondo.setStroke(
                    Color.WHITE
            );


            fondo.setStrokeWidth(
                    2
            );
        }
    }


    // =========================================================
    // CARTA ACTUAL
    // =========================================================

    public Carta getCartaActual() {

        return cartaActual;
    }


    // =========================================================
    // COMPROBAR SI REPRESENTA UNA CARTA
    // =========================================================

    public boolean representa(
            Carta carta) {


        if (cartaActual == null
                || carta == null) {


            return false;
        }


        return cartaActual.getValor()
                == carta.getValor()

                &&

                cartaActual.getPalo()
                == carta.getPalo();
    }
}