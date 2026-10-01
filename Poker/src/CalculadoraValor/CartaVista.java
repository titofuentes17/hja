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

    private static final double ANCHO = 66;
    private static final double ALTO = 88;


    // =========================================================
    // ELEMENTOS VISUALES
    // =========================================================

    /*
     * Rectángulo situado detrás de la imagen.
     *
     * Nos sirve principalmente para:
     * - poner un fondo blanco
     * - dibujar el borde normal
     * - dibujar el borde dorado cuando la carta forma
     *   parte de la mejor mano
     */
    private Rectangle fondo;


    /*
     * Aquí mostramos tanto:
     *
     * - el reverso rojo
     * - la cara de la carta
     */
    private ImageView imagenCarta;


    /*
     * Carta que se está mostrando.
     *
     * Si vale null significa que estamos viendo
     * el reverso.
     */
    private Carta cartaActual;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public CartaVista() {

        setAlignment(Pos.CENTER);


        // =====================================================
        // FONDO
        // =====================================================

        fondo = new Rectangle(ANCHO, ALTO);

        fondo.setArcWidth(8);
        fondo.setArcHeight(8);

        fondo.setFill(Color.WHITE);
        fondo.setStroke(Color.BLACK);
        fondo.setStrokeWidth(2);


        // =====================================================
        // IMAGEN
        // =====================================================

        imagenCarta = new ImageView();

        imagenCarta.setFitWidth(ANCHO);
        imagenCarta.setFitHeight(ALTO);

        /*
         * Muy importante:
         *
         * evita que las imágenes se deformen.
         */
        imagenCarta.setPreserveRatio(true);

        /*
         * Hace que JavaFX suavice la imagen
         * cuando cambia de tamaño.
         */
        imagenCarta.setSmooth(true);


        // =====================================================
        // AÑADIR ELEMENTOS
        // =====================================================

        getChildren().addAll(fondo, imagenCarta);


        /*
         * Todas las cartas empiezan boca abajo.
         */
        mostrarReverso();
    }


    // =========================================================
    // MOSTRAR REVERSO
    // =========================================================

    public void mostrarReverso() {

        /*
         * No hay ninguna carta visible por delante.
         */
        cartaActual = null;


        String ruta = "/CalculadoraValor/cartas/detras_carta.png";


        var recurso = getClass().getResource(ruta);


        // =====================================================
        // SI NO ENCONTRAMOS LA IMAGEN
        // =====================================================

        if (recurso == null) {

            System.err.println("No se encontró la imagen del reverso: " + ruta);


            imagenCarta.setImage(null);
            imagenCarta.setVisible(false);


            /*
             * Reverso azul de emergencia.
             *
             * Así la aplicación sigue funcionando
             * aunque falte el PNG.
             */
            fondo.setFill(Color.web("#163A70"));

            fondo.setStroke(Color.WHITE);

            fondo.setStrokeWidth(2);

            return;
        }


        // =====================================================
        // CARGAR REVERSO
        // =====================================================

        Image imagenReverso = new Image(recurso.toExternalForm());


        imagenCarta.setImage(imagenReverso);

        imagenCarta.setVisible(true);


        fondo.setFill(Color.WHITE);

        fondo.setStroke(Color.BLACK);

        fondo.setStrokeWidth(2);
    }


    // =========================================================
    // MOSTRAR CARA DE LA CARTA
    // =========================================================

    public void mostrarCarta(Carta carta) {

        cartaActual = carta;


        String ruta = obtenerRutaImagen(carta);


        var recurso = getClass().getResource(ruta);


        // =====================================================
        // COMPROBAR QUE EXISTE
        // =====================================================

        if (recurso == null) {

            System.err.println("No se encontró la imagen de la carta: " + ruta);


            /*
             * Si algo falla volvemos al reverso
             * para que no quede una carta vacía.
             */
            mostrarReverso();

            return;
        }


        // =====================================================
        // CARGAR IMAGEN
        // =====================================================

        Image imagen = new Image(recurso.toExternalForm());


        imagenCarta.setImage(imagen);

        imagenCarta.setVisible(true);


        fondo.setFill(Color.WHITE);

        fondo.setStroke(Color.BLACK);

        fondo.setStrokeWidth(2);
    }


    // =========================================================
    // OBTENER RUTA DEL PNG
    // =========================================================

    private String obtenerRutaImagen(Carta carta) {

        String valor = convertirValor(carta.getValor());


        String palo = convertirPalo(carta.getPalo());


        /*
         * Algunos ejemplos:
         *
         * Ah -> ace_of_hearts.png
         * Ad -> ace_of_diamonds.png
         * Ks -> king_of_spades.png
         * Qc -> queen_of_clubs.png
         * Jh -> jack_of_hearts.png
         * Td -> 10_of_diamonds.png
         * 9s -> 9_of_spades.png
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

    private String convertirValor(char valor) {

        return switch (valor) {

            case 'A' -> "ace";

            case 'K' -> "king";

            case 'Q' -> "queen";

            case 'J' -> "jack";

            case 'T' -> "10";

            default ->
                    String.valueOf(valor);
        };
    }


    // =========================================================
    // CONVERTIR PALO
    // =========================================================

    private String convertirPalo(char palo) {

        return switch (palo) {

            case 'h' -> "hearts";

            case 'd' -> "diamonds";

            case 'c' -> "clubs";

            case 's' -> "spades";

            default -> "";
        };
    }


    // =========================================================
    // VOLTEAR CARTA
    // =========================================================

    public void voltear(Carta carta) {

        /*
         * Queremos que la carta gire
         * alrededor del eje vertical.
         */
        setRotationAxis(Rotate.Y_AXIS);


        // =====================================================
        // PRIMERA MITAD
        // =====================================================

        RotateTransition primeraMitad = new RotateTransition(Duration.millis(200), this);


        /*
         * Reverso:
         *
         * 0º ------> 90º
         */
        primeraMitad.setFromAngle(0);

        primeraMitad.setToAngle(90);


        // =====================================================
        // CUANDO LA CARTA ESTÁ DE PERFIL
        // =====================================================

        primeraMitad.setOnFinished(evento -> {
                    /*
                     * En este instante cambiamos el
                     * reverso por la cara real.
                     */
                    mostrarCarta(carta);


                    /*
                     * Colocamos la cara a -90º.
                     */
                    setRotate(-90);


                    // =========================================
                    // SEGUNDA MITAD
                    // =========================================

                    RotateTransition segundaMitad = new RotateTransition(Duration.millis(200), this);


                    /*
                     * Cara:
                     *
                     * -90º ------> 0º
                     */
                    segundaMitad.setFromAngle(-90);

                    segundaMitad.setToAngle(0);


                    segundaMitad.play();
                }
        );


        primeraMitad.play();
    }


    // =========================================================
    // REPARTIR DESDE EL MAZO
    // =========================================================

    public void repartirDesde(Carta carta, double origenX, double origenY) {


        /*
         * Hacemos visible el hueco de la carta.
         */
        setVisible(true);


        /*
         * Siempre sale del crupier boca abajo.
         */
        mostrarReverso();


        /*
         * Situamos visualmente la carta
         * encima del mazo del crupier.
         */
        setTranslateX(origenX);

        setTranslateY(origenY);


        /*
         * Sale ligeramente más pequeña.
         */
        setScaleX(0.75);

        setScaleY(0.75);


        // =====================================================
        // ANIMACIÓN DEL REPARTO
        // =====================================================

        TranslateTransition movimiento = new TranslateTransition(Duration.millis(350), this);


        /*
         * La posición final natural de la
         * CartaVista es 0, 0.
         */
        movimiento.setToX(0);

        movimiento.setToY(0);


        // =====================================================
        // AL LLEGAR A SU SITIO
        // =====================================================

        movimiento.setOnFinished(evento -> {
                    /*
                     * Recuperamos tamaño normal.
                     */
                    setScaleX(1);

                    setScaleY(1);


                    /*
                     * Y volteamos la carta.
                     */
                    voltear(carta);
                }
        );


        movimiento.play();
    }


    // =========================================================
    // DESTACAR CARTA
    // =========================================================

    public void destacar() {

        /*
         * No hacemos nada si la carta
         * ni siquiera está visible.
         */
        if (!isVisible()) {
            return;
        }


        // =====================================================
        // PEQUEÑO TEMBLOR
        // =====================================================

        TranslateTransition temblor = new TranslateTransition(Duration.millis(65), this);


        temblor.setFromX(-3);

        temblor.setToX(3);


        temblor.setAutoReverse(true);

        temblor.setCycleCount(6);


        // =====================================================
        // BORDE DORADO
        // =====================================================

        temblor.setOnFinished(evento -> {
                    setTranslateX(0);


                    fondo.setStroke(Color.GOLD);


                    fondo.setStrokeWidth(4);
                }
        );


        temblor.play();
    }


    // =========================================================
    // QUITAR DESTACADO
    // =========================================================

    public void quitarDestacado() {

        setTranslateX(0);


        /*
         * Carta boca arriba.
         */
        if (cartaActual != null) {

            fondo.setStroke(Color.BLACK);

            fondo.setStrokeWidth(2);
        }


        /*
         * Carta boca abajo.
         */
        else {

            fondo.setStroke(Color.BLACK);

            fondo.setStrokeWidth(2);
        }
    }


    // =========================================================
    // OBTENER CARTA ACTUAL
    // =========================================================

    public Carta getCartaActual() {

        return cartaActual;
    }


    // =========================================================
    // COMPROBAR SI ESTA VISTA REPRESENTA UNA CARTA
    // =========================================================

    public boolean representa(Carta carta) {

        if (cartaActual == null || carta == null) {

            return false;
        }


        return cartaActual.getValor()
                == carta.getValor()

                &&

                cartaActual.getPalo()
                == carta.getPalo();
    }
}
