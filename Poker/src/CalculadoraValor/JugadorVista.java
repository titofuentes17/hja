package CalculadoraValor;

import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


public class JugadorVista extends VBox {

    private String id;

    private Label nombre;
    private Label posicion;
    private Label nombreMano;

    private CartaVista carta1;
    private CartaVista carta2;

    private List<CartaVista> cartas;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public JugadorVista(String id) {

        this.id = id;


        // =====================================================
        // NOMBRE DEL JUGADOR
        // =====================================================

        nombre = new Label(id);

        nombre.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;"
        );


        // =====================================================
        // POSICIÓN
        // =====================================================

        posicion = new Label("");

        posicion.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;"
        );

        posicion.setVisible(false);
        posicion.setManaged(false);


        // =====================================================
        // CARTAS
        // =====================================================

        carta1 = new CartaVista();
        carta2 = new CartaVista();


        cartas = List.of(
                carta1,
                carta2
        );


        carta1.setVisible(false);
        carta2.setVisible(false);


        HBox filaCartas = new HBox(
                4,
                carta1,
                carta2
        );


        filaCartas.setAlignment(
                Pos.CENTER
        );


        // =====================================================
        // NOMBRE DE LA MANO
        // =====================================================

        nombreMano = new Label("");

        nombreMano.setStyle(
                "-fx-font-size: 11px;"
        );

        nombreMano.setVisible(false);
        nombreMano.setManaged(false);


        // =====================================================
        // JUGADOR COMPLETO
        // =====================================================

        setSpacing(2);

        setAlignment(
                Pos.CENTER
        );


        getChildren().addAll(
                posicion,
                nombre,
                filaCartas,
                nombreMano
        );
    }


    // =========================================================
    // ID
    // =========================================================

    public String getIdJugador() {

        return id;
    }


    // =========================================================
    // CARTAS
    // =========================================================

    public List<CartaVista> getCartasVista() {

        return cartas;
    }


    public CartaVista getCartaVista(int indice) {

        return cartas.get(indice);
    }


    // =========================================================
    // MOSTRAR RESULTADO
    // =========================================================

    public void mostrarResultado(
            int puesto,
            String mano,
            boolean ganador) {


        // =====================================================
        // POSICIÓN
        // =====================================================

        if (ganador) {

            posicion.setText(
                    "🏆 1º"
            );

        } else {

            posicion.setText(
                    puesto + "º"
            );
        }


        posicion.setVisible(true);
        posicion.setManaged(true);


        // =====================================================
        // MEJOR MANO
        // =====================================================

        nombreMano.setText(
                mano
        );


        nombreMano.setVisible(true);
        nombreMano.setManaged(true);


        // =====================================================
        // ESTILO
        // =====================================================

        if (ganador) {

            posicion.setStyle(
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #C99700;"
            );


            nombre.setStyle(
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #C99700;"
            );


            nombreMano.setStyle(
                    "-fx-font-size: 11px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #C99700;"
            );

        } else {

            posicion.setStyle(
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: bold;"
            );


            nombre.setStyle(
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: bold;"
            );


            nombreMano.setStyle(
                    "-fx-font-size: 11px;"
            );
        }
    }


    // =========================================================
    // REINICIAR
    // =========================================================

    public void reiniciar() {

        posicion.setText("");
        posicion.setVisible(false);
        posicion.setManaged(false);


        nombreMano.setText("");
        nombreMano.setVisible(false);
        nombreMano.setManaged(false);


        nombre.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;"
        );


        for (CartaVista carta : cartas) {

            carta.quitarDestacado();

            carta.setVisible(false);

            carta.setTranslateX(0);
            carta.setTranslateY(0);

            carta.setScaleX(1);
            carta.setScaleY(1);

            carta.setRotate(0);
        }
    }
}