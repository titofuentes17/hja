package CalculadoraValor;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;

// Mesa con 6 asientos y el board en el centro.
//
//          [J3]
//   [J2]            [J4]
//             board
//   [J1]            [J5]
//          [HERO]
//
// El asiento 0 es el hero (la posición del usuario).
public class MesaEquityVista extends Pane {

    public static final int NUM_ASIENTOS = 6;

    private static final double ANCHO = 900;
    private static final double ALTO = 670;

    private static final double CENTRO_X = ANCHO / 2;
    private static final double CENTRO_Y = 330;

    private static final double ANCHO_ASIENTO = 165;
    private static final double ALTO_ASIENTO = 265;

    // Centro de cada asiento: hero, J1..J5 en sentido horario
    private static final double[][] POSICIONES = {
        {450, 520},   // hero (abajo)
        {125, 470},   // J1 (abajo izquierda)
        {125, 190},   // J2 (arriba izquierda)
        {450, 140},   // J3 (arriba)
        {775, 190},   // J4 (arriba derecha)
        {775, 470}    // J5 (abajo derecha)
    };

    private final List<AsientoVista> asientos = new ArrayList<>();
    private final List<CartaVista> board = new ArrayList<>();

    public MesaEquityVista() {
        setPrefSize(ANCHO, ALTO);
        setMinSize(ANCHO, ALTO);
        setMaxSize(ANCHO, ALTO);

        // Tapete
        Ellipse tapete = new Ellipse(CENTRO_X, CENTRO_Y, 345, 160);
        tapete.setFill(Color.web("#176B3A"));
        tapete.setStroke(Color.web("#4A2C17"));
        tapete.setStrokeWidth(8);
        getChildren().add(tapete);

        // Cartas del board
        HBox zonaBoard = new HBox(7);
        zonaBoard.setAlignment(Pos.CENTER);
        for (int i = 0; i < 5; i++) {
            CartaVista carta = new CartaVista();
            carta.setVisible(false);
            board.add(carta);
            zonaBoard.getChildren().add(carta);
        }
        double anchoBoard = 5 * 66 + 4 * 7;
        zonaBoard.setLayoutX(CENTRO_X - anchoBoard / 2);
        zonaBoard.setLayoutY(CENTRO_Y - 44);
        getChildren().add(zonaBoard);

        // Asientos
        for (int i = 0; i < NUM_ASIENTOS; i++) {
            boolean hero = i == 0;
            AsientoVista asiento = new AsientoVista(hero ? "HERO" : "J" + i, i, hero);
            asiento.setLayoutX(POSICIONES[i][0] - ANCHO_ASIENTO / 2);
            asiento.setLayoutY(POSICIONES[i][1] - ALTO_ASIENTO / 2);
            asientos.add(asiento);
            getChildren().add(asiento);
        }

        zonaBoard.toFront();
    }

    public List<AsientoVista> getAsientos() {
        return asientos;
    }

    public AsientoVista getAsiento(int indice) {
        return asientos.get(indice);
    }

    public List<CartaVista> getBoard() {
        return board;
    }

    public CartaVista getCartaBoard(int indice) {
        return board.get(indice);
    }

    public void reiniciarBoard() {
        for (CartaVista carta : board) {
            carta.quitarDestacado();
            carta.setVisible(false);
            carta.setTranslateX(0);
            carta.setTranslateY(0);
            carta.setScaleX(1);
            carta.setScaleY(1);
            carta.setRotate(0);
        }
    }

    // Deja la mesa limpia: sin cartas, sin equity y sin ganador marcado
    public void reiniciarTodo() {
        reiniciarBoard();
        for (AsientoVista asiento : asientos) {
            asiento.setRetirado(false);
            asiento.reiniciar();
            asiento.limpiarResultado();
        }
    }
}
