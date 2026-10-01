package CalculadoraValor;

import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class CrupierVista extends VBox {

    // =========================================================
    // ELEMENTOS DEL CRUPIER
    // =========================================================

    private ImageView manoIzquierda;
    private ImageView manoDerecha;

    private CartaVista mazo;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public CrupierVista() {

        setAlignment(Pos.CENTER);

        setSpacing(0);


        // =====================================================
        // CARGAR MANOS
        // =====================================================

        manoIzquierda = cargarImagen("/CalculadoraValor/imagenes/guante_izquierdo.png");

        manoDerecha = cargarImagen("/CalculadoraValor/imagenes/guante_derecho.png");


        // =====================================================
        // MAZO
        // =====================================================

        mazo = new CartaVista();

        mazo.setScaleX(0.70);

        mazo.setScaleY(0.70);


        // =====================================================
        // FILA DEL CRUPIER
        // =====================================================

        HBox manos = new HBox(5, manoIzquierda, mazo, manoDerecha);

        manos.setAlignment(Pos.CENTER);

        getChildren().add(manos);
    }


    // =========================================================
    // CARGAR IMAGEN
    // =========================================================

    private ImageView cargarImagen(String ruta) {

        var recurso = getClass().getResource(ruta);

        ImageView imagen = new ImageView();


        if (recurso == null) {

            System.err.println("No se encontró la imagen: " + ruta);

            return imagen;
        }


        Image archivo = new Image(recurso.toExternalForm());

        imagen.setImage(archivo);


        /*
         * Tamaño del guante.
         *
         * Luego podemos ajustarlo dependiendo
         * de cómo quede visualmente.
         */
        imagen.setFitWidth(105);

        imagen.setFitHeight(80);


        imagen.setPreserveRatio(true);


        imagen.setSmooth(true);


        return imagen;
    }


    // =========================================================
    // REPARTIR CARTA
    // =========================================================

    public void repartirCarta(CartaVista destino, Carta carta) {

        /*
         * IMPORTANTE:
         *
         * manoIzquierda es la mano que vemos
         * a la izquierda de la pantalla.
         *
         * Visualmente corresponde a la mano
         * derecha del crupier.
         */
        ImageView manoRepartidora = manoIzquierda;


        // =====================================================
        // POSICIÓN DEL MAZO
        // =====================================================

        Bounds posicionMazo = mazo.localToScene(mazo.getBoundsInLocal());


        // =====================================================
        // POSICIÓN DE LA MANO
        // =====================================================

        Bounds posicionMano = manoRepartidora.localToScene(manoRepartidora.getBoundsInLocal());


        // =====================================================
        // POSICIÓN DEL DESTINO
        // =====================================================

        Bounds posicionDestino = destino.localToScene(destino.getBoundsInLocal());


        /*
         * Primero calculamos cuánto tiene que moverse
         * la mano para llegar aproximadamente al mazo.
         */
        double haciaMazoX = posicionMazo.getCenterX() - posicionMano.getCenterX();

        double haciaMazoY = posicionMazo.getCenterY() - posicionMano.getCenterY();


        // =====================================================
        // 1. MANO HACIA EL MAZO
        // =====================================================

        TranslateTransition cogerCarta = new TranslateTransition(Duration.millis(180), manoRepartidora);

        cogerCarta.setToX(haciaMazoX);

        cogerCarta.setToY(haciaMazoY);


        // =====================================================
        // 2. MANO HACIA EL DESTINO
        // =====================================================

        /*
         * No queremos que la mano llegue completamente
         * hasta el jugador.
         *
         * Un crupier empuja/lanzaría la carta y después
         * retiraría la mano.
         *
         * Por eso recorremos aproximadamente un 40 %
         * de la distancia.
         */
        double porcentajeRecorrido = 0.40;

        double recorridoX = haciaMazoX + (posicionDestino.getCenterX() - posicionMazo.getCenterX()) * porcentajeRecorrido;

        double recorridoY = haciaMazoY + (posicionDestino.getCenterY() - posicionMazo.getCenterY()) * porcentajeRecorrido;


        TranslateTransition empujarCarta = new TranslateTransition(Duration.millis(230), manoRepartidora);

        empujarCarta.setToX(recorridoX);

        empujarCarta.setToY(recorridoY);


        // =====================================================
        // CARTA DESDE EL MAZO HASTA SU DESTINO
        // =====================================================

        /*
         * Calculamos desde dónde tiene que empezar
         * la animación de CartaVista.
         */
        double cartaX = posicionMazo.getCenterX() - posicionDestino.getCenterX();

        double cartaY = posicionMazo.getCenterY() - posicionDestino.getCenterY();


        /*
         * Cuando la mano ya está encima del mazo
         * empezamos a mover la carta.
         */
        cogerCarta.setOnFinished(evento -> {

            destino.repartirDesde(carta, cartaX, cartaY);
        });


        // =====================================================
        // 3. MANO VUELVE A SU POSICIÓN
        // =====================================================

        TranslateTransition volver = new TranslateTransition(Duration.millis(250), manoRepartidora);

        volver.setToX(0);

        volver.setToY(0);


        // =====================================================
        // SECUENCIA COMPLETA
        // =====================================================

        SequentialTransition movimiento = new SequentialTransition(cogerCarta, empujarCarta, volver);

        movimiento.play();
    }


    // =========================================================
    // GETTERS
    // =========================================================

    public CartaVista getMazo() {

        return mazo;
    }


    public ImageView getManoIzquierda() {

        return manoIzquierda;
    }


    public ImageView getManoDerecha() {

        return manoDerecha;
    }
}
