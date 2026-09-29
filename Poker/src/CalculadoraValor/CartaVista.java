package CalculadoraValor;

import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;


public class CartaVista extends StackPane {

    private Rectangle fondo;
    private Label contenido;

    // Carta que se está mostrando actualmente.
    private Carta cartaActual;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public CartaVista() {

        fondo = new Rectangle(65, 90);

        fondo.setArcWidth(12);
        fondo.setArcHeight(12);


        contenido = new Label();

        contenido.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        20
                )
        );


        setAlignment(Pos.CENTER);

        getChildren().addAll(
                fondo,
                contenido
        );


        mostrarReverso();
    }


    // =========================================================
    // MOSTRAR REVERSO
    // =========================================================

    public void mostrarReverso() {

        cartaActual = null;

        fondo.setFill(Color.DARKBLUE);
        fondo.setStroke(Color.WHITE);
        fondo.setStrokeWidth(2);

        contenido.setText("♠");
        contenido.setTextFill(Color.WHITE);
    }


    // =========================================================
    // MOSTRAR CARTA
    // =========================================================

    public void mostrarCarta(Carta carta) {

        cartaActual = carta;

        fondo.setFill(Color.WHITE);
        fondo.setStroke(Color.BLACK);
        fondo.setStrokeWidth(2);


        String simboloPalo =
                obtenerSimboloPalo(
                        carta.getPalo()
                );


        String valorVisual;


        if (carta.getValor() == 'T') {

            valorVisual = "10";

        } else {

            valorVisual =
                    String.valueOf(
                            carta.getValor()
                    );
        }


        contenido.setText(
                valorVisual
                        + "\n"
                        + simboloPalo
        );


        if (carta.getPalo() == 'h'
                || carta.getPalo() == 'd') {

            contenido.setTextFill(
                    Color.RED
            );

        } else {

            contenido.setTextFill(
                    Color.BLACK
            );
        }
    }


    // =========================================================
    // VOLTEAR
    // =========================================================

    public void voltear(Carta carta) {

        setRotationAxis(
                Rotate.Y_AXIS
        );


        RotateTransition primeraMitad =
                new RotateTransition(
                        Duration.millis(200),
                        this
                );


        primeraMitad.setFromAngle(0);
        primeraMitad.setToAngle(90);


        primeraMitad.setOnFinished(evento -> {

            mostrarCarta(carta);

            setRotate(-90);


            RotateTransition segundaMitad =
                    new RotateTransition(
                            Duration.millis(200),
                            this
                    );


            segundaMitad.setFromAngle(-90);
            segundaMitad.setToAngle(0);

            segundaMitad.play();
        });


        primeraMitad.play();
    }


    // =========================================================
    // REPARTIR DESDE UNA POSICIÓN
    // =========================================================

    public void repartirDesde(
            Carta carta,
            double origenX,
            double origenY) {

        setVisible(true);

        mostrarReverso();

        setTranslateX(origenX);
        setTranslateY(origenY);

        setScaleX(0.75);
        setScaleY(0.75);


        TranslateTransition movimiento =
                new TranslateTransition(
                        Duration.millis(350),
                        this
                );


        movimiento.setFromX(origenX);
        movimiento.setFromY(origenY);

        movimiento.setToX(0);
        movimiento.setToY(0);


        movimiento.setOnFinished(evento -> {

            setScaleX(1);
            setScaleY(1);

            voltear(carta);
        });


        movimiento.play();
    }


    // =========================================================
    // DESTACAR
    // =========================================================

    public void destacar() {

        if (!isVisible()) {
            return;
        }


        TranslateTransition temblor =
                new TranslateTransition(
                        Duration.millis(65),
                        this
                );


        temblor.setFromX(-3);
        temblor.setToX(3);

        temblor.setAutoReverse(true);
        temblor.setCycleCount(6);


        temblor.setOnFinished(evento -> {

            setTranslateX(0);

            fondo.setStroke(
                    Color.GOLD
            );

            fondo.setStrokeWidth(4);
        });


        temblor.play();
    }


    // =========================================================
    // QUITAR DESTACADO
    // =========================================================

    public void quitarDestacado() {

        setTranslateX(0);


        if (cartaActual != null) {

            fondo.setStroke(
                    Color.BLACK
            );

            fondo.setStrokeWidth(2);

        } else {

            fondo.setStroke(
                    Color.WHITE
            );

            fondo.setStrokeWidth(2);
        }
    }


    // =========================================================
    // GET CARTA ACTUAL
    // =========================================================

    public Carta getCartaActual() {

        return cartaActual;
    }


    // =========================================================
    // COMPROBAR CARTA
    // =========================================================

    public boolean representa(Carta carta) {

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


    // =========================================================
    // PALO
    // =========================================================

    private String obtenerSimboloPalo(
            char palo) {

        switch (palo) {

            case 'h':
                return "♥";

            case 'd':
                return "♦";

            case 'c':
                return "♣";

            case 's':
                return "♠";

            default:
                return "";
        }
    }
}