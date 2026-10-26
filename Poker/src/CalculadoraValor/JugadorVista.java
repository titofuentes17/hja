package CalculadoraValor;

import java.util.ArrayList;
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
    private List<CartaVista> cartas;
    private HBox filaCartas;

    // Constructor normal: Texas Hold'em y apartado 3 usan 2 cartas
    public JugadorVista(String id) {
        this(id, 2);
    }

    // Permite crear jugadores con distinto número de cartas, por ejemplo 4 en Omaha
    public JugadorVista(String id, int numeroCartas) {
        this.id = id;
        nombre = new Label(id);
        nombre.setStyle("-fx-font-size: 12px;-fx-font-weight: bold;");

        posicion = new Label("");
        posicion.setStyle("-fx-font-size: 12px;-fx-font-weight: bold;");
        posicion.setVisible(false);
        posicion.setManaged(false);

        cartas = new ArrayList<>();
        filaCartas = new HBox(4);
        filaCartas.setAlignment(Pos.CENTER);

        for (int i = 0; i < numeroCartas; i++) {
            CartaVista carta = new CartaVista();
            carta.setVisible(false);
            cartas.add(carta);
            filaCartas.getChildren().add(carta);
        }

        nombreMano = new Label("");
        nombreMano.setStyle("-fx-font-size: 11px;");
        nombreMano.setVisible(false);
        nombreMano.setManaged(false);

        setSpacing(2);
        setAlignment(Pos.CENTER);
        getChildren().addAll(posicion, nombre, filaCartas, nombreMano);
    }

    public String getIdJugador() {
        return id;
    }

    public List<CartaVista> getCartasVista() {
        return cartas;
    }

    public CartaVista getCartaVista(int indice) {
        return cartas.get(indice);
    }

    // Cambia las cartas del jugador (p.ej. de 2 a 4 al pasar a Omaha).
    // Con separación negativa las cartas se solapan, como en la mano.
    public void setNumeroCartas(int numeroCartas, double ancho, double alto, double separacion) {
        cartas.clear();
        filaCartas.getChildren().clear();
        filaCartas.setSpacing(separacion);

        for (int i = 0; i < numeroCartas; i++) {
            CartaVista carta = new CartaVista(ancho, alto);
            carta.setVisible(false);
            cartas.add(carta);
            filaCartas.getChildren().add(carta);
        }
    }

    public void mostrarResultado(int puesto, String mano, boolean ganador) {
        if (ganador) posicion.setText("🏆 1º");
        else posicion.setText(puesto + "º");

        posicion.setVisible(true);
        posicion.setManaged(true);
        nombreMano.setText(mano);
        nombreMano.setVisible(true);
        nombreMano.setManaged(true);

        if (ganador) {
            posicion.setStyle("-fx-font-size: 13px;-fx-font-weight: bold;-fx-text-fill: #C99700;");
            nombre.setStyle("-fx-font-size: 13px;-fx-font-weight: bold;-fx-text-fill: #C99700;");
            nombreMano.setStyle("-fx-font-size: 11px;-fx-font-weight: bold;-fx-text-fill: #C99700;");
        } else {
            posicion.setStyle("-fx-font-size: 12px;-fx-font-weight: bold;");
            nombre.setStyle("-fx-font-size: 12px;-fx-font-weight: bold;");
            nombreMano.setStyle("-fx-font-size: 11px;");
        }
    }

    public void reiniciar() {
        posicion.setText("");
        posicion.setVisible(false);
        posicion.setManaged(false);
        nombreMano.setText("");
        nombreMano.setVisible(false);
        nombreMano.setManaged(false);
        nombre.setStyle("-fx-font-size: 12px;-fx-font-weight: bold;");

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
