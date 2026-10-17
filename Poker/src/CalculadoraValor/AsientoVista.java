package CalculadoraValor;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

// Un asiento de la mesa de equity: es un JugadorVista (nombre + cartas) al que
// se le añaden:
//   - un check "Jugando" para activar/desactivar el asiento
//   - el nombre de su mejor mano
//   - un cuadro de texto de SOLO LECTURA con la equity
//   - un cuadro de texto para introducir sus cartas manualmente (p.ej. AhKd)
public class AsientoVista extends JugadorVista {

    private static final String ESTILO_NORMAL =
            "-fx-background-color: rgba(5,18,14,0.88);"
            + "-fx-border-color: #C9A227;"
            + "-fx-border-width: 1.5;"
            + "-fx-border-radius: 10;"
            + "-fx-background-radius: 10;";

    private static final String ESTILO_GANADOR =
            "-fx-background-color: rgba(40,32,5,0.92);"
            + "-fx-border-color: #FFD700;"
            + "-fx-border-width: 3;"
            + "-fx-border-radius: 10;"
            + "-fx-background-radius: 10;";

    private final int indice;
    private final boolean esHero;

    private final Label nombreLabel;
    private final CheckBox activo;
    private final Label manoLabel;
    private final TextField equity;
    private final TextField cartasManual;

    public AsientoVista(String id, int indice, boolean esHero) {
        super(id);

        this.indice = indice;
        this.esHero = esHero;

        // En JugadorVista los hijos son: posicion, nombre, filaCartas, nombreMano
        nombreLabel = (Label) getChildren().get(1);
        estiloNombre();

        setPrefWidth(165);
        setMinWidth(165);
        setMaxWidth(165);
        setPadding(new Insets(5));
        setSpacing(3);
        setStyle(ESTILO_NORMAL);

        // Check "Jugando" (el hero siempre juega)
        activo = new CheckBox(esHero ? "Hero" : "Jugando");
        activo.setSelected(true);
        activo.setDisable(esHero);
        activo.setStyle("-fx-text-fill: #DDDDDD; -fx-font-size: 11px;");
        getChildren().add(0, activo);

        // Nombre de la mejor mano
        manoLabel = new Label("");
        manoLabel.setStyle("-fx-text-fill: #D6B85A; -fx-font-size: 11px; -fx-font-weight: bold;");
        manoLabel.setMinHeight(15);
        manoLabel.setWrapText(false);

        // Equity: solo salida, no se puede editar
        Label etiquetaEquity = new Label("Equity");
        etiquetaEquity.setStyle("-fx-text-fill: #D6B85A; -fx-font-weight: bold; -fx-font-size: 11px;");

        equity = new TextField();
        equity.setEditable(false);
        equity.setFocusTraversable(false);
        equity.setPromptText("—");
        equity.setPrefWidth(100);
        equity.setAlignment(Pos.CENTER);
        estiloEquityNormal();

        HBox filaEquity = new HBox(5, etiquetaEquity, equity);
        filaEquity.setAlignment(Pos.CENTER);

        // Cartas manuales
        cartasManual = new TextField();
        cartasManual.setPromptText("Cartas: AhKd");
        cartasManual.setPrefWidth(140);
        cartasManual.setMaxWidth(140);
        cartasManual.setDisable(true);

        getChildren().addAll(manoLabel, filaEquity, cartasManual);

        // Al desactivar un asiento se atenúa y se limpia
        activo.selectedProperty().addListener((obs, antes, ahora) -> {
            setOpacity(ahora ? 1.0 : 0.45);
            if (!ahora) {
                reiniciar();
                limpiarResultado();
            }
        });
    }

    private void estiloNombre() {
        nombreLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: "
                + (esHero ? "#F2D16B" : "#FFFFFF") + ";");
    }

    private void estiloEquityNormal() {
        equity.setStyle("-fx-control-inner-background: #F4F4F4;"
                + "-fx-font-family: 'Consolas';"
                + "-fx-font-size: 12px;"
                + "-fx-font-weight: bold;");
    }

    // JugadorVista.reiniciar() deja el nombre sin color: lo restauramos
    @Override
    public void reiniciar() {
        super.reiniciar();
        estiloNombre();
    }


    // =========================================================
    // ACCESO DESDE LA INTERFAZ
    // =========================================================

    public int getIndice() {
        return indice;
    }

    public boolean isActivo() {
        return activo.isSelected();
    }

    public String getCartasTexto() {
        return cartasManual.getText();
    }

    // Habilita o no la escritura manual de cartas
    public void setModoManual(boolean manual) {
        cartasManual.setDisable(!manual);
    }

    public void setEquity(String texto, String detalle) {
        equity.setText(texto);
        equity.setTooltip(detalle == null || detalle.isEmpty() ? null : new Tooltip(detalle));
    }

    public void setManoTexto(String texto) {
        manoLabel.setText(texto);
    }

    public void marcarGanador(boolean ganador) {
        setStyle(ganador ? ESTILO_GANADOR : ESTILO_NORMAL);
    }

    public void limpiarResultado() {
        equity.clear();
        equity.setTooltip(null);
        manoLabel.setText("");
        marcarGanador(false);
    }
}
