package CalculadoraValor;

import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


public class JugadorVista extends VBox {

    private String id;

    private Label nombre;

    private CartaVista carta1;
    private CartaVista carta2;

    private List<CartaVista> cartas;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public JugadorVista(String id) {

        this.id = id;


        // ==========================================
        // NOMBRE
        // ==========================================

        nombre =
                new Label(id);


        nombre.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;"
        );


        // ==========================================
        // CARTAS
        // ==========================================

        carta1 =
                new CartaVista();

        carta2 =
                new CartaVista();


        cartas =
                List.of(
                        carta1,
                        carta2
                );


        carta1.setVisible(false);
        carta2.setVisible(false);


        HBox filaCartas =
                new HBox(
                        4,
                        carta1,
                        carta2
                );


        filaCartas.setAlignment(
                Pos.CENTER
        );


        // ==========================================
        // JUGADOR
        // ==========================================

        setSpacing(2);

        setAlignment(
                Pos.CENTER
        );


        getChildren().addAll(
                nombre,
                filaCartas
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


    public CartaVista getCartaVista(
            int indice) {

        return cartas.get(indice);
    }


    // =========================================================
    // REINICIAR
    // =========================================================

    public void reiniciar() {

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
