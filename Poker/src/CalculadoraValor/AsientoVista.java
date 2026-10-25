package CalculadoraValor;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Button;
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
//   - su propio RANGO: un cuadro de texto (AA,KK,AKs,77+...) o un cuadro de
//     porcentaje (top X % de manos). Son excluyentes: si se usa uno, el otro
//     queda deshabilitado. El fondo del cuadro en uso se pone verde o rojo
//     según la mano del jugador esté dentro o fuera del rango.
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

    private static final String ESTILO_CAMPO_RANGO = "-fx-font-size: 11px;";

    private final int indice;
    private final boolean esHero;

    private final Label nombreLabel;
    private final CheckBox activo;
    private final Button fold;
    private boolean retirado;
    private final Label manoLabel;
    private final TextField equity;
    private final TextField cartasManual;

    private final TextField rangoTexto;
    private final TextField porcentaje;
    private final Label infoRango;

    // La interfaz se entera de cualquier cambio en el rango o en el check "Jugando"
    private Runnable alCambiarRango = () -> { };

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
        fold = new Button("Fold");
        fold.setId("fold-" + indice);
        fold.setDisable(true);
        fold.setTooltip(new Tooltip("Retirarse de esta mano. Las cartas siguen fuera del mazo."));
        HBox participacion = new HBox(8, activo, fold);
        participacion.setAlignment(Pos.CENTER);
        getChildren().add(0, participacion);

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

        // Rango propio: texto o porcentaje (excluyentes)
        rangoTexto = new TextField();
        rangoTexto.setPromptText("Rango: AA,KK,AKs");
        rangoTexto.setPrefWidth(100);
        rangoTexto.setStyle(ESTILO_CAMPO_RANGO);

        porcentaje = new TextField();
        porcentaje.setPromptText("Top %");
        porcentaje.setPrefWidth(46);
        porcentaje.setStyle(ESTILO_CAMPO_RANGO);

        HBox filaRango = new HBox(4, rangoTexto, porcentaje);
        filaRango.setAlignment(Pos.CENTER);

        infoRango = new Label("");
        infoRango.setStyle("-fx-text-fill: #D9D9D9; -fx-font-size: 10px;");
        infoRango.setMinHeight(13);

        getChildren().addAll(manoLabel, filaEquity, cartasManual, filaRango, infoRango);

        // Exclusión mutua: si hay texto en uno, el otro queda deshabilitado
        rangoTexto.textProperty().addListener((obs, antes, ahora) -> {
            porcentaje.setDisable(!ahora.isBlank());
            alCambiarRango.run();
        });

        porcentaje.textProperty().addListener((obs, antes, ahora) -> {
            rangoTexto.setDisable(!ahora.isBlank());
            alCambiarRango.run();
        });

        // Al desactivar un asiento se atenúa y se limpia
        activo.selectedProperty().addListener((obs, antes, ahora) -> {
            setOpacity(ahora ? 1.0 : 0.45);
            if (!ahora) {
                reiniciar();
                limpiarResultado();
            }
            alCambiarRango.run();
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

    // En el recorrido obligatorio participan siempre los seis asientos.
    public void setParticipacionFija(boolean fija) {
        if (fija) activo.setSelected(true);
        activo.setDisable(esHero || fija);
        fold.setVisible(fija);
        fold.setManaged(fija);
    }

    public void setOnFold(Runnable accion) {
        fold.setOnAction(e -> accion.run());
    }

    public void setFoldDisponible(boolean disponible) {
        fold.setDisable(!disponible || retirado);
    }

    public boolean isRetirado() {
        return retirado;
    }

    public void setRetirado(boolean retirado) {
        this.retirado = retirado;
        setOpacity(retirado || !isActivo() ? 0.45 : 1.0);
        fold.setText(retirado ? "Retirado" : "Fold");
        if (retirado) {
            fold.setDisable(true);
            limpiarResultado();
            setManoTexto("FOLD");
            setEquity("0,00 %", "Retirado: no disputa el bote; sus cartas siguen fuera del mazo.");
            getCartasVista().forEach(CartaVista::quitarDestacado);
        }
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


    // =========================================================
    // RANGO DEL ASIENTO
    // =========================================================

    public void setOnRangoCambiado(Runnable accion) {
        this.alCambiarRango = accion == null ? () -> { } : accion;
    }

    public String getRangoTexto() {
        return rangoTexto.getText();
    }

    public String getPorcentajeTexto() {
        return porcentaje.getText();
    }

    // ¿Ha escrito algo el usuario en alguno de los dos cuadros de rango?
    public boolean tieneRango() {
        return !rangoTexto.getText().isBlank() || !porcentaje.getText().isBlank();
    }

    // Vacía los dos cuadros de rango (esto también los vuelve a habilitar)
    public void limpiarRango() {
        rangoTexto.clear();
        porcentaje.clear();
    }

    // Muestra el estado del rango:
    //   colorFondo: fondo del cuadro en uso (null = sin color)
    //   info:       texto corto bajo los cuadros
    //   detalle:    texto largo para el tooltip (null = sin tooltip)
    public void mostrarRango(String colorFondo, String info, String detalle) {
        rangoTexto.setStyle(ESTILO_CAMPO_RANGO);
        porcentaje.setStyle(ESTILO_CAMPO_RANGO);

        if (colorFondo != null) {
            TextField enUso = !rangoTexto.getText().isBlank() ? rangoTexto : porcentaje;
            enUso.setStyle(ESTILO_CAMPO_RANGO
                    + "-fx-control-inner-background: " + colorFondo + ";"
                    + "-fx-text-fill: #111111;");
        }

        infoRango.setText(info == null ? "" : info);

        Tooltip tip = detalle == null || detalle.isEmpty() ? null : new Tooltip(detalle);
        rangoTexto.setTooltip(tip);
        porcentaje.setTooltip(tip);
    }
}
