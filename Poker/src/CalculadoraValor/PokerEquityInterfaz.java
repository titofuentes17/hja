	package CalculadoraValor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Label;
import javafx.scene.Group;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

// Calculadora de equity para 6 jugadores.
//
//  - CADA jugador puede tener un RANGO, definido de dos formas excluyentes:
//      * en texto (AA,KK,AKs,77+,A5s-A2s...)
//      * por porcentaje de mejores manos según un ranking
//    El fondo del cuadro de rango se pone verde si la mano repartida a ese
//    jugador está dentro de su rango, y rojo si está fuera.
//  - Las cartas de los jugadores y el board pueden ser aleatorios o manuales.
//  - Todos los jugadores muestran su equity en un cuadro de solo lectura.
//  - Modalidad Texas Hold'em (2 cartas) u Omaha (4 cartas: la mano se forma
//    con exactamente 2 cartas propias y exactamente 3 comunes).
//  - Modo "Apuestas NLHE" (práctica 3, apartado 2.1): cada jugador tiene además
//    un equity mínimo (EM). En cada turno el jugador puede hacer Bet manual o
//    dejar que la aplicación decida (Bet/Call si su mano está en el rango y su
//    equity llega al EM; si no, Fold).
//  - Modo "Decisión en el Turn" (práctica 3, apartado 2.2): hero contra un villano
//    al que se le da un rango. Se calculan las outs de hero contra cada mano del
//    rango y se compara su media con el EM de hero (Call / Fold).


public class PokerEquityInterfaz extends Application {

    private static final int ITERACIONES = 100_000;
    private static final String COLOR_OK = "#7FD99A";
    private static final String COLOR_ERROR = "#FF7B7B";
    private static final String COLOR_NEUTRO = "#BBBBBB";

    // Fondos del cuadro de rango
    private static final String FONDO_DENTRO = "#9BE3AE";    // verde
    private static final String FONDO_FUERA = "#F2A1A1";     // rojo
    private static final String FONDO_INVALIDO = "#FFE0A3";  // naranja claro

    private final Random rnd = new Random();

    private MesaEquityVista mesa;
    private CrupierVista crupier;

    // Controles de rango (generales, cada jugador tiene los suyos en su asiento)
    private ComboBox<String> rankingCombo;
    private CheckBox repartirEnRango;
    private CheckBox equityRango;

    // Controles de cartas
    private ComboBox<String> modoCartas;
    private ComboBox<String> modoBoard;
    private ComboBox<String> calleBoard;
    private TextField boardManual;

    private Button botonRepartir;
    private Button botonSiguiente;
    private ComboBox<String> modoAnalisis;
    private ComboBox<String> modalidad;
    private Label subtitulo;
    private Label calleActual;
    private VBox configuracion;
    private Label estado;
    private ManoEquity manoActual;
    private boolean ocupado;
    private long versionOperacion;
    private Task<?> tareaActual;
    private final List<PauseTransition> pausas = new ArrayList<>();

    // Manos repartidas en el último reparto, por asiento (null si aún no hay reparto
    // o si el asiento no juega). Sirve para comprobar si cada mano está en su rango.
    private List<List<Carta>> manosRepartidas;

    // ---------- Modo de apuestas (práctica 3) ----------
    private RondaApuestas ronda;
    // Equity (0..100) de cada asiento en la calle actual. Se calcula una vez al
    // empezar la calle, con los jugadores que siguen en la mano.
    private final double[] equityCalle = new double[MesaEquityVista.NUM_ASIENTOS];
    private boolean equityCalleLista;
    // Colores de rango y EM de cada asiento: se pintan cuando el jugador decide
    // y se borran al empezar cada calle (null = sin color)
    private final String[] colorRangoDecision = new String[MesaEquityVista.NUM_ASIENTOS];
    private final String[] colorEMDecision = new String[MesaEquityVista.NUM_ASIENTOS];
    private HBox barraApuestas;
    private Label turnoLabel;
    private Label boteLabel;
    private String ganadoresTexto = "";   // "Gana Highjack", "Empate: Button, UTG"
    private Button botonBetManual;
    private Button botonAutomatico;
    private Button botonAutoCalle;


    // =========================================================
    // VENTANA
    // =========================================================

    @Override
    public void start(Stage escenario) {
        escenario.initStyle(StageStyle.DECORATED);
        escenario.setMaximized(false);
        StackPane fondoGeneral = new StackPane();
        fondoGeneral.setStyle("-fx-background-color: #071711;");

        BorderPane raiz = new BorderPane();
        raiz.setPadding(new Insets(12, 16, 12, 16));
        raiz.setStyle("-fx-background-color: transparent;");

        Label titulo = new Label("♠  POKER EQUITY STUDIO");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: #F2F5EF;");
        subtitulo = new Label(textoSubtitulo());
        subtitulo.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #A6B5A9;");
        VBox cabecera = new VBox(3, titulo, subtitulo);
        Label marca = new Label("●  EQUITY LAB");
        marca.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #E0BE68;");
        Region separador = new Region();
        HBox.setHgrow(separador, Priority.ALWAYS);
        HBox parteSuperior = new HBox(12, cabecera, separador, marca);
        parteSuperior.setAlignment(Pos.CENTER_LEFT);
        parteSuperior.setPadding(new Insets(4, 6, 12, 6));
        raiz.setTop(parteSuperior);

        crupier = new CrupierVista();
        mesa = new MesaEquityVista();

        // La mesa mantiene sus coordenadas internas; escalamos todo el conjunto
        // (crupier + mesa) para que los seis asientos sean visibles sin scroll.
        // Botones de las apuestas, debajo de la mesa (solo en el modo de apuestas)
        construirBarraApuestas();

        VBox centro = new VBox(4, crupier, mesa, barraApuestas);
        centro.setAlignment(Pos.TOP_CENTER);
        centro.setPadding(new Insets(4, 0, 4, 0));
        centro.setMinWidth(900);
        centro.setPrefWidth(900);
        centro.setStyle("-fx-background-color: #091F18; -fx-background-radius: 16;");

        Group mesaEscalable = new Group(centro);
        StackPane contenedorMesa = new StackPane(mesaEscalable);
        contenedorMesa.setAlignment(Pos.CENTER);
        contenedorMesa.setMinSize(0, 0);
        contenedorMesa.setStyle("-fx-background-color: #091F18;"
                + "-fx-border-color: #25493B; -fx-border-radius: 16;");
        raiz.setCenter(contenedorMesa);

        // El Group tiene en cuenta la escala al calcular su tamaño. Escalar
        // el Group en sí evita afectar las coordenadas de las animaciones.
        Runnable ajustarMesa = () -> {
            double anchoNatural = centro.getLayoutBounds().getWidth();
            double altoNatural = centro.getLayoutBounds().getHeight();
            double anchoDisponible = contenedorMesa.getWidth() - 12;
            double altoDisponible = contenedorMesa.getHeight() - 12;
            if (anchoNatural <= 0 || altoNatural <= 0
                    || anchoDisponible <= 0 || altoDisponible <= 0) return;
            double escala = Math.min(1.0, Math.min(
                    anchoDisponible / anchoNatural, altoDisponible / altoNatural));
            centro.setScaleX(escala);
            centro.setScaleY(escala);
        };
        contenedorMesa.widthProperty().addListener((obs, anterior, nuevo) -> ajustarMesa.run());
        contenedorMesa.heightProperty().addListener((obs, anterior, nuevo) -> ajustarMesa.run());
        centro.layoutBoundsProperty().addListener((obs, anterior, nuevo) -> ajustarMesa.run());

        raiz.setRight(construirPanelControl());
        BorderPane.setMargin(raiz.getRight(), new Insets(0, 0, 0, 14));

        // Selector de modalidad en la cabecera (el panel lateral va justo de altura)
        HBox filaModalidad = new HBox(8, etiqueta("Modalidad:"), modalidad);
        filaModalidad.setAlignment(Pos.CENTER_RIGHT);
        parteSuperior.getChildren().add(2, filaModalidad);

        // Cada asiento avisa a la interfaz cuando cambia su rango
        for (AsientoVista asiento : mesa.getAsientos()) {
            asiento.setOnRangoCambiado(this::actualizarRangos);
            asiento.setOnFold(() -> retirarJugador(asiento.getIndice()));
        }

        fondoGeneral.getChildren().add(raiz);
        actualizarRangos();
        // Respetar el área visible de Windows (incluye la barra de tareas).
        // Una ventana mayor que la pantalla oculta la barra de título y la X.
        Rectangle2D area = Screen.getPrimary().getVisualBounds();
        double anchoVentana = Math.min(1280, Math.max(640, area.getWidth() - 60));
        double altoVentana = Math.min(760, Math.max(480, area.getHeight() - 65));
        Scene escena = new Scene(fondoGeneral, anchoVentana, altoVentana);
        escenario.setTitle("Poker Equity Studio");
        escenario.setMinWidth(Math.min(950, anchoVentana));
        escenario.setMinHeight(Math.min(600, altoVentana));
        escenario.setScene(escena);
        escenario.centerOnScreen();
        escenario.show();
        actualizarControles();
    }


    // =========================================================
    // PANEL DE CONTROL (RANGOS + CARTAS)
    // =========================================================

    private VBox construirPanelControl() {
        // ---------- Rangos ----------
        Label tituloRango = tituloSeccion("RANGOS");

       
        Label etiquetaRanking = etiqueta("Ranking para los porcentajes:");

        rankingCombo = new ComboBox<>();
        rankingCombo.getItems().addAll(RankingManos.NOMBRES);
        rankingCombo.setValue(RankingManos.NOMBRES.get(0));
        rankingCombo.setMaxWidth(Double.MAX_VALUE);
        rankingCombo.setOnAction(e -> actualizarRangos());

        // Se conservan los controles internos para mantener la compatibilidad
        // con el reparto y el calculo existentes, pero no se muestran.
        repartirEnRango = new CheckBox();
        repartirEnRango.setSelected(false);
        equityRango = new CheckBox();
        equityRango.setSelected(false);

        Button limpiarRangos = new Button("Limpiar todos los rangos");
        limpiarRangos.setMaxWidth(Double.MAX_VALUE);
        limpiarRangos.setOnAction(e -> {
            for (AsientoVista asiento : mesa.getAsientos()) {
                asiento.limpiarRango();
            }
        });

        // ---------- Cartas ----------
        Label tituloCartas = tituloSeccion("CARTAS");

        modoCartas = new ComboBox<>();
        modoCartas.getItems().addAll("Cartas aleatorias", "Cartas manuales");
        modoCartas.setValue("Cartas aleatorias");
        modoCartas.setMaxWidth(Double.MAX_VALUE);

        modoCartas.setOnAction(e -> {
            boolean manual = modoCartas.getValue().contains("manual");
            for (AsientoVista asiento : mesa.getAsientos()) {
                asiento.setModoManual(manual);
            }
        });

        modoBoard = new ComboBox<>();
        modoBoard.getItems().addAll("Board aleatorio", "Board manual");
        modoBoard.setValue("Board aleatorio");
        modoBoard.setMaxWidth(Double.MAX_VALUE);

        calleBoard = new ComboBox<>();
        calleBoard.getItems().addAll("Preflop (sin board)", "Flop (3 cartas)", "Turn (4 cartas)", "River (5 cartas)");
        calleBoard.getSelectionModel().select(1);
        calleBoard.setMaxWidth(Double.MAX_VALUE);

        boardManual = new TextField();
        boardManual.setPromptText("Board: Ts9h2c (0, 3, 4 o 5 cartas)");
        boardManual.setDisable(true);

        modoBoard.setOnAction(e -> {
            boolean manual = modoBoard.getValue().contains("manual");
            calleBoard.setDisable(manual || esManoPorCalles() || esTurn());
            boardManual.setDisable(!manual);
        });

        // ---------- Botón y estado ----------
        botonRepartir = new Button("▶  REPARTIR Y CALCULAR");
        botonRepartir.setMaxWidth(Double.MAX_VALUE);
        botonRepartir.setStyle("-fx-background-color: #C9A227;" + "-fx-text-fill: #111111;" + "-fx-font-weight: bold;"
                + "-fx-font-size: 13px;" + "-fx-background-radius: 8;" + "-fx-padding: 8 22 8 22;" + "-fx-cursor: hand;");
        botonRepartir.setOnAction(e -> {
            if (esTurn()) decidirTurn();
            else if (esManoPorCalles()) nuevaMano();
            else repartir();
        });

        modalidad = new ComboBox<>();
        modalidad.getItems().addAll("Texas Hold'em", "Omaha");
        modalidad.setValue("Texas Hold'em");
        modalidad.setPrefWidth(160);
        modalidad.setOnAction(e -> cambiarModalidad());

        modoAnalisis = new ComboBox<>();
        modoAnalisis.getItems().addAll("Mano por calles (6 jugadores)", "Escenario aislado",
                "Apuestas NLHE (6 jugadores)", "Decisión en el Turn (hero vs villano)");
        modoAnalisis.getSelectionModel().selectFirst();
        modoAnalisis.setMaxWidth(Double.MAX_VALUE);
        modoAnalisis.setOnAction(e -> {
            invalidarOperacion();
            subtitulo.setText(textoSubtitulo());
            manoActual = null;
            manosRepartidas = null;
            ronda = null;
            limpiarDecisiones();
            for (AsientoVista asiento : mesa.getAsientos()) {
                // En el Turn solo juegan hero (asiento 0) y el villano: el resto se oculta
                boolean visible = !esTurn() || asiento.getIndice() == 0 || asiento.getIndice() == VILLANO;
                asiento.setVisible(visible);
                asiento.setModoApuestas(esApuestas() || esTurn(), nombreAsientoDecision(asiento.getIndice()));
            }
            calleBoard.setDisable(esManoPorCalles() || esTurn() || modoBoard.getValue().contains("manual"));
            mesa.reiniciarTodo();
            actualizarRangos();
            calleActual.setText("Sin mano iniciada");
            mostrarEstado("Configura las cartas y pulsa el botón de inicio.", false);
            actualizarControles();
        });
        botonSiguiente = new Button("Mostrar flop");
        botonSiguiente.setMaxWidth(Double.MAX_VALUE);
        botonSiguiente.setOnAction(e -> siguienteCalle());
        calleActual = etiqueta("Sin mano iniciada");
        estado = new Label("Pulsa Nueva mano para empezar en preflop.");
        estado.setWrapText(true);
        estado.setMaxWidth(290);
        estado.setStyle("-fx-text-fill: " + COLOR_NEUTRO + "; -fx-font-size: 12px;");

        configuracion = new VBox(12,
                tituloRango, etiquetaRanking, rankingCombo, limpiarRangos,
                new Separator(),
                tituloCartas, modoCartas, modoBoard, calleBoard, boardManual);
        Label tituloModo = tituloSeccion("MODO DE ANÁLISIS");
        VBox panel = new VBox(14, tituloModo, modoAnalisis, configuracion,
                new Separator(), calleActual, botonRepartir, botonSiguiente, estado);

        panel.setPadding(new Insets(20));
        panel.setPrefWidth(316);
        panel.setMinWidth(316);
        panel.setMaxWidth(316);
        panel.setAlignment(Pos.TOP_LEFT);
        panel.setStyle("-fx-background-color: #10271F; -fx-border-color: #355445;"
                + "-fx-border-width: 1; -fx-border-radius: 16; -fx-background-radius: 16;");
        String estiloCombo = "-fx-background-color: #213B31; -fx-text-fill: #F0F4EE;"
                + "-fx-background-radius: 7; -fx-border-color: #456254;"
                + "-fx-border-radius: 7; -fx-pref-height: 34; -fx-font-size: 12px;";
        for (ComboBox<String> combo : java.util.Arrays.asList(
                modalidad, modoAnalisis, rankingCombo, modoCartas, modoBoard, calleBoard)) {
            combo.setStyle(estiloCombo);
            // ComboBox utiliza celdas internas: -fx-text-fill en el ComboBox
            // no cambia necesariamente el texto de la celda seleccionada.
            combo.setButtonCell(new ListCell<String>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                    setStyle("-fx-text-fill: #F4F6F0; -fx-background-color: transparent;"
                            + "-fx-font-size: 12px; -fx-opacity: 1;");
                }
            });
            combo.setCellFactory(lista -> new ListCell<String>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                    setStyle("-fx-text-fill: #16251E; -fx-background-color: #F2F5F0;"
                            + "-fx-font-size: 12px;");
                }
            });
            // El estado deshabilitado se muestra en gris, sin perder legibilidad.
            combo.disabledProperty().addListener((obs, anterior, deshabilitado) -> {
                combo.setOpacity(1.0);
                combo.setStyle(deshabilitado
                        ? "-fx-background-color: #35403C; -fx-text-fill: #A9B3AE;"
                          + "-fx-background-radius: 7; -fx-border-color: #53615A;"
                          + "-fx-border-radius: 7; -fx-pref-height: 34; -fx-font-size: 12px;"
                        : estiloCombo);
                combo.getButtonCell().setStyle("-fx-text-fill: "
                        + (deshabilitado ? "#A9B3AE" : "#F4F6F0")
                        + "; -fx-background-color: transparent; -fx-font-size: 12px;");
            });
            // Sincronizar también el estado inicial.
            if (combo.isDisabled()) {
                combo.setStyle("-fx-background-color: #35403C; -fx-border-color: #53615A;"
                        + "-fx-background-radius: 7; -fx-border-radius: 7; -fx-pref-height: 34;");
                combo.getButtonCell().setStyle("-fx-text-fill: #A9B3AE; -fx-background-color: transparent;");
            }
        }
        boardManual.setStyle("-fx-background-color: #213B31; -fx-text-fill: #F0F4EE;"
                + "-fx-prompt-text-fill: #A4B6A9; -fx-background-radius: 7;"
                + "-fx-border-color: #456254; -fx-border-radius: 7; -fx-pref-height: 34;");
        boardManual.disabledProperty().addListener((obs, antes, deshabilitado) -> {
            boardManual.setOpacity(1.0);
            boardManual.setStyle(deshabilitado
                    ? "-fx-background-color: #35403C; -fx-text-fill: #A9B3AE;"
                      + "-fx-background-radius: 7; -fx-border-color: #53615A;"
                      + "-fx-border-radius: 7; -fx-pref-height: 34;"
                    : "-fx-background-color: #213B31; -fx-text-fill: #F0F4EE;"
                      + "-fx-background-radius: 7; -fx-border-color: #456254;"
                      + "-fx-border-radius: 7; -fx-pref-height: 34;");
        });
        limpiarRangos.setStyle("-fx-background-color: #274237; -fx-text-fill: #E7EEE7;"
                + "-fx-background-radius: 7; -fx-pref-height: 34; -fx-cursor: hand;");
        botonSiguiente.setStyle("-fx-background-color: #2D493D; -fx-text-fill: #F0F4EE;"
                + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-pref-height: 38; -fx-cursor: hand;");
        botonRepartir.setStyle("-fx-background-color: #D4AB4D; -fx-text-fill: #102118;"
                + "-fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 8;"
                + "-fx-pref-height: 43; -fx-cursor: hand;");

        
        // Los botones no disponibles se distinguen de los activos.
        botonSiguiente.disabledProperty().addListener((obs, antes, deshabilitado) -> {
            botonSiguiente.setOpacity(1.0);
            botonSiguiente.setStyle(deshabilitado
                    ? "-fx-background-color: #35403C; -fx-text-fill: #A9B3AE;"
                      + "-fx-background-radius: 8; -fx-pref-height: 38;"
                    : "-fx-background-color: #2D493D; -fx-text-fill: #F0F4EE;"
                      + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-pref-height: 38;");
        });
        botonRepartir.disabledProperty().addListener((obs, antes, deshabilitado) -> {
            botonRepartir.setOpacity(1.0);
            botonRepartir.setStyle(deshabilitado
                    ? "-fx-background-color: #35403C; -fx-text-fill: #A9B3AE;"
                      + "-fx-background-radius: 8; -fx-pref-height: 43;"
                    : "-fx-background-color: #D4AB4D; -fx-text-fill: #102118;"
                      + "-fx-font-weight: bold; -fx-font-size: 13px;"
                      + "-fx-background-radius: 8; -fx-pref-height: 43;");
        });
        return panel;
    }

    private Label tituloSeccion(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #E0BE68;");
        return l;
    }

    private Label etiqueta(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #B9CCC0;");
        return l;
    }

    private void mostrarEstado(String mensaje, boolean error) {
        estado.setText(mensaje);
        estado.setStyle("-fx-text-fill: " + (error ? COLOR_ERROR : COLOR_NEUTRO) + "; -fx-font-size: 12px;");
    }

    // El modo de apuestas también va por calles (preflop, flop, turn y river)
    private boolean esManoPorCalles() {
        return modoAnalisis != null && (modoAnalisis.getSelectionModel().getSelectedIndex() == 0 || esApuestas());
    }

    private boolean esApuestas() {
        return modoAnalisis != null && modoAnalisis.getSelectionModel().getSelectedIndex() == 2;
    }

    private boolean esTurn() {
        return modoAnalisis != null && modoAnalisis.getSelectionModel().getSelectedIndex() == 3;
    }

    private boolean esOmaha() {
        return modalidad != null && "Omaha".equals(modalidad.getValue());
    }

    private String textoSubtitulo() {
        if (esApuestas()) return "NLHE  /  APUESTAS Y TOMA DE DECISIONES";
        if (esTurn()) return "NLHE  /  DECISIÓN EN EL TURN: HERO VS RANGO";
        return (esOmaha() ? "OMAHA" : "TEXAS HOLD'EM") + "  /  ANALISIS DE PROBABILIDADES";
    }

    // Omaha se calcula igual que la mano por calles (con Fold): se fuerza ese modo.
    // Cada asiento pasa a tener 4 cartas y se vacía la mesa.
    private void cambiarModalidad() {
        invalidarOperacion();
        boolean omaha = esOmaha();
        subtitulo.setText(textoSubtitulo());
        if (omaha) {
            modoAnalisis.getSelectionModel().selectFirst();
        }
        manoActual = null;
        manosRepartidas = null;
        for (AsientoVista asiento : mesa.getAsientos()) {
            asiento.setModalidadOmaha(omaha);
        }
        mesa.reiniciarTodo();
        actualizarRangos();
        calleActual.setText("Sin mano iniciada");
        mostrarEstado(omaha
                ? "Omaha: 4 cartas por jugador; la mano usa exactamente 2 propias y 3 comunes."
                : "Configura las cartas y pulsa el botón de inicio.", false);
        actualizarControles();
    }

    private void actualizarControles() {
        boolean porCalles = esManoPorCalles();
        configuracion.setDisable(ocupado);
        mesa.setDisable(ocupado);
        modoAnalisis.setDisable(ocupado || esOmaha());
        modalidad.setDisable(ocupado || esApuestas() || esTurn());
        botonRepartir.setDisable(ocupado);
        botonRepartir.setText(esTurn() ? "Repartir y decidir"
                : porCalles ? "Nueva mano" : "Repartir escenario y calcular");
        equityRango.setDisable(porCalles);
        calleBoard.setDisable(porCalles || esTurn() || modoBoard.getValue().contains("manual"));
        botonSiguiente.setVisible(porCalles);
        botonSiguiente.setManaged(porCalles);
        // En el modo de apuestas no se pasa de calle hasta que han hablado todos
        boolean apuestasPendientes = esApuestas() && (ronda == null || !ronda.calleTerminada());
        botonSiguiente.setDisable(ocupado || manoActual == null || !manoActual.puedeAvanzar()
                || apuestasPendientes);
        if (manoActual != null) {
            botonSiguiente.setText(manoActual.puedeAvanzar()
                    ? "Mostrar " + manoActual.getCalle().siguiente().name().toLowerCase(java.util.Locale.ROOT)
                    : "Mano finalizada");
        }
        for (AsientoVista asiento : mesa.getAsientos()) {
            asiento.setParticipacionFija(porCalles);
            asiento.setFoldDisponible(porCalles && !ocupado && manoActual != null
                    && manoActual.puedeAvanzar() && !manoActual.estaRetirado(asiento.getIndice()));
        }
        actualizarControlesApuestas();
    }

    // Todos los callbacks comprueban esta versión, incluso si la cancelación llega tarde.
    private void invalidarOperacion() {
        versionOperacion++;
        if (tareaActual != null) tareaActual.cancel(true);
        tareaActual = null;
        for (PauseTransition pausa : pausas) pausa.stop();
        pausas.clear();
    }

    private void empezarOperacion() {
        invalidarOperacion();
        ocupado = true;
        actualizarControles();
    }

    private void terminarOperacion() {
        tareaActual = null;
        ocupado = false;
        actualizarControles();
    }

    @Override
    public void stop() {
        invalidarOperacion();
    }

    private void nuevaMano() {
        if (ocupado) return;
        final List<List<Carta>> manuales = new ArrayList<>();
        final List<Rango> rangos = new ArrayList<>();
        final List<Carta> reservado;
        try {
            reservado = modoBoard.getValue().contains("manual")
                    ? Cartas.parsear(boardManual.getText()) : List.of();
            for (AsientoVista asiento : mesa.getAsientos()) {
                String texto = asiento.getCartasTexto();
                manuales.add(modoCartas.getValue().contains("manual") && texto != null && !texto.isBlank()
                        ? Cartas.parsear(texto) : null);
                rangos.add(obtenerRango(asiento));
            }
        } catch (IllegalArgumentException e) {
            mostrarEstado(e.getMessage(), true);
            return;
        }
        boolean restringir = repartirEnRango.isSelected();
        final int cartasPorJugador = esOmaha() ? 4 : 2;
        empezarOperacion();
        ronda = null;
        limpiarDecisiones();
        mostrarEstado("Preparando la nueva mano...", false);
        final long version = versionOperacion;
        Task<ManoEquity> preparacion = new Task<>() {
            @Override
            protected ManoEquity call() {
                return RepartidorEquity.nuevaMano(manuales, rangos, reservado, restringir,
                        cartasPorJugador, new Random());
            }
        };
        tareaActual = preparacion;
        preparacion.setOnSucceeded(e -> {
            if (version != versionOperacion) return;
            manoActual = preparacion.getValue();
            // Apuestas: se ponen las ciegas y empieza a hablar UTG
            if (esApuestas()) ronda = new RondaApuestas(manoActual);
            calleActual.setText("Calle actual: PREFLOP");
            animarReparto(repartoActual(), true, 0);
        });
        preparacion.setOnFailed(e -> {
            if (version != versionOperacion) return;
            mostrarEstado("No se pudo repartir: " + preparacion.getException().getMessage(), true);
            terminarOperacion();
        });
        iniciarHilo(preparacion);
    }

    private Reparto repartoActual() {
        Reparto reparto = new Reparto();
        for (int indice : manoActual.getJugadoresEnMano()) reparto.activos.add(mesa.getAsiento(indice));
        reparto.manos.addAll(manoActual.getManos());
        reparto.board = manoActual.getBoardVisible();
        reparto.cartasRetiradas = manoActual.getCartasRetiradas();
        reparto.omaha = manoActual.isOmaha();
        return reparto;
    }

    private void retirarJugador(int indice) {
        if (ocupado || !esManoPorCalles() || manoActual == null || !manoActual.puedeAvanzar()
                || manoActual.estaRetirado(indice)) return;
        manoActual.retirar(indice);
        mesa.getAsiento(indice).setRetirado(true);
        empezarOperacion();
        // Recalcular también la calle actual, sin revelar ni volver a repartir cartas.
        calcularEquity(repartoActual());
    }

    private void siguienteCalle() {
        if (ocupado || !esManoPorCalles() || manoActual == null || !manoActual.puedeAvanzar()) return;
        int anteriores = manoActual.getBoardVisible().size();
        empezarOperacion();
        manoActual.avanzar();
        // Apuestas: nueva ronda en la calle; los colores de rango y EM se vuelven a pintar al decidir
        if (ronda != null) {
            ronda.empezarCalle();
            limpiarDecisiones();
        }
        calleActual.setText("Calle actual: " + manoActual.getCalle());
        // No se reinicia la mesa ni se vuelven a repartir las cartas propias.
        animarReparto(repartoActual(), false, anteriores);
    }

    private void iniciarHilo(Task<?> tarea) {
        Thread hilo = new Thread(tarea, "poker-equity");
        hilo.setDaemon(true);
        hilo.start();
    }


    // =========================================================
    // RANGOS DE LOS JUGADORES
    // =========================================================

    // Devuelve el rango escrito en un asiento, o null si no tiene ninguno.
    // Lanza IllegalArgumentException si lo escrito no es válido.
    private Rango obtenerRango(AsientoVista asiento) {
        String texto = asiento.getRangoTexto();
        if (texto != null && !texto.isBlank()) {
            return Rango.desdeTexto(texto);
        }

        String pct = asiento.getPorcentajeTexto();
        if (pct != null && !pct.isBlank()) {
            double valor;
            try {
                valor = Double.parseDouble(pct.trim().replace("%", "").replace(',', '.').trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Porcentaje no válido: \"" + pct.trim() + "\"");
            }
            return Rango.desdePorcentaje(valor, RankingManos.obtener(rankingCombo.getValue()));
        }

        return null;
    }

    // Recalcula el estado de los rangos de TODOS los asientos:
    //   verde     -> la mano del jugador está dentro de su rango
    //   rojo      -> la mano del jugador está fuera de su rango
    //   naranja   -> el rango escrito no es válido
    //   sin color -> todavía no hay mano repartida
    private void actualizarRangos() {
        if (mesa == null) {
            return;
        }

        for (AsientoVista asiento : mesa.getAsientos()) {
            asiento.mostrarEM(esApuestas() || esTurn() ? colorEMDecision[asiento.getIndice()] : null);
            if (!asiento.tieneRango()) {
                asiento.mostrarRango(esApuestas() ? colorRangoDecision[asiento.getIndice()] : null, "", null);
                continue;
            }

            Rango rango;
            try {
                rango = obtenerRango(asiento);
            } catch (IllegalArgumentException e) {
                asiento.mostrarRango(FONDO_INVALIDO, "Rango no válido", e.getMessage());
                continue;
            }

            String resumen = String.format("%.1f %%", rango.porcentaje());
            String detalle = rango.size() + " combinaciones (" + resumen + " de las manos)";

            // Apuestas: el color se pone cuando el jugador toma su decisión
            if (esApuestas()) {
                asiento.mostrarRango(colorRangoDecision[asiento.getIndice()], resumen, detalle);
                continue;
            }

            List<Carta> mano = null;
            if (manosRepartidas != null && asiento.isActivo()) {
                mano = manosRepartidas.get(asiento.getIndice());
            }

            if (mano == null) {
                asiento.mostrarRango(null, detalle, detalle);
                continue;
            }

            boolean dentro = rango.contiene(mano.get(0), mano.get(1));
            String clase = Rango.clase(mano.get(0), mano.get(1));

            asiento.mostrarRango(
                    dentro ? FONDO_DENTRO : FONDO_FUERA,
                    resumen + " · " + clase + ": " + (dentro ? "dentro" : "fuera"),
                    detalle + "\nMano: " + Utils.cartasAString(mano) + " (" + clase + ") → "
                            + (dentro ? "DENTRO del rango" : "FUERA del rango"));
        }
    }


    // =========================================================
    // PREPARACIÓN DEL REPARTO (validación y elección de cartas)
    // =========================================================

    private static final class Reparto {
        final List<AsientoVista> activos = new ArrayList<>();
        final List<List<Carta>> manos = new ArrayList<>();   // una entrada por asiento (null si no juega)
        List<Carta> board = new ArrayList<>();
        List<Carta> cartasRetiradas = List.of();
        boolean omaha;   // 4 cartas por jugador, 2 propias + 3 comunes
        // Rango con el que se calcula la equity de cada asiento (null = su mano concreta)
        final Rango[] rangosEquity = new Rango[MesaEquityVista.NUM_ASIENTOS];
    }

    private Reparto prepararReparto() {
        Reparto reparto = new Reparto();
        for (int i = 0; i < MesaEquityVista.NUM_ASIENTOS; i++) {
            reparto.manos.add(null);
        }

        for (AsientoVista asiento : mesa.getAsientos()) {
            if (asiento.isActivo()) {
                reparto.activos.add(asiento);
            }
        }
        if (reparto.activos.size() < 2) {
            throw new IllegalArgumentException("Activa al menos dos jugadores.");
        }

        Set<Integer> usadas = new HashSet<>();

        // ---------- Board ----------
        int boardAleatorio = 0;
        if (modoBoard.getValue().contains("manual")) {
            List<Carta> board;
            try {
                board = Cartas.parsear(boardManual.getText());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Board: " + e.getMessage());
            }
            int n = board.size();
            if (n != 0 && n != 3 && n != 4 && n != 5) {
                throw new IllegalArgumentException("El board debe tener 0, 3, 4 o 5 cartas (has escrito " + n + ").");
            }
            for (Carta c : board) {
                usadas.add(Cartas.id(c));
            }
            reparto.board = board;
        } else {
            int[] cartasPorCalle = {0, 3, 4, 5};
            boardAleatorio = cartasPorCalle[calleBoard.getSelectionModel().getSelectedIndex()];
        }

        // ---------- Cartas manuales ----------
        boolean manual = modoCartas.getValue().contains("manual");
        if (manual) {
            for (AsientoVista asiento : reparto.activos) {
                String texto = asiento.getCartasTexto();
                if (texto == null || texto.isBlank()) {
                    continue;
                }

                List<Carta> mano;
                try {
                    mano = Cartas.parsear(texto);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(asiento.getIdJugador() + ": " + e.getMessage());
                }
                if (mano.size() != 2) {
                    throw new IllegalArgumentException(asiento.getIdJugador()
                            + ": escribe exactamente 2 cartas (por ejemplo AhKd).");
                }
                for (Carta c : mano) {
                    if (!usadas.add(Cartas.id(c))) {
                        throw new IllegalArgumentException("La carta " + c + " está repetida (" + asiento.getIdJugador()
                                + " u otro jugador o el board).");
                    }
                }
                reparto.manos.set(asiento.getIndice(), mano);
            }
        }

        // ---------- Rangos de cada jugador ----------
        Rango[] rangos = new Rango[MesaEquityVista.NUM_ASIENTOS];
        for (AsientoVista asiento : reparto.activos) {
            try {
                rangos[asiento.getIndice()] = obtenerRango(asiento);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(asiento.getIdJugador() + ": rango no válido. " + e.getMessage());
            }

            if (equityRango.isSelected()) {
                reparto.rangosEquity[asiento.getIndice()] = rangos[asiento.getIndice()];
            }
        }

        // ---------- Jugadores con rango: mano aleatoria dentro de su rango ----------
        // (primero ellos, que son los más restringidos)
        if (repartirEnRango.isSelected()) {
            for (AsientoVista asiento : reparto.activos) {
                Rango rango = rangos[asiento.getIndice()];
                if (rango == null || reparto.manos.get(asiento.getIndice()) != null) {
                    continue;
                }

                List<int[]> libres = new ArrayList<>();
                for (int[] combo : rango.getCombos()) {
                    if (!usadas.contains(combo[0]) && !usadas.contains(combo[1])) {
                        libres.add(combo);
                    }
                }
                if (libres.isEmpty()) {
                    throw new IllegalArgumentException(asiento.getIdJugador()
                            + ": su rango no es compatible con las cartas ya elegidas (board u otros jugadores).");
                }

                int[] elegido = libres.get(rnd.nextInt(libres.size()));
                usadas.add(elegido[0]);
                usadas.add(elegido[1]);

                List<Carta> mano = new ArrayList<>();
                mano.add(Cartas.carta(elegido[0]));
                mano.add(Cartas.carta(elegido[1]));
                reparto.manos.set(asiento.getIndice(), mano);
            }
        }

        // ---------- Resto de jugadores: cartas aleatorias ----------
        for (AsientoVista asiento : reparto.activos) {
            if (reparto.manos.get(asiento.getIndice()) == null) {
                List<Carta> mano = new ArrayList<>();
                mano.add(robarCarta(usadas));
                mano.add(robarCarta(usadas));
                reparto.manos.set(asiento.getIndice(), mano);
            }
        }

        // ---------- Board aleatorio ----------
        for (int i = 0; i < boardAleatorio; i++) {
            reparto.board.add(robarCarta(usadas));
        }

        return reparto;
    }

    // Saca una carta que no esté en uso
    private Carta robarCarta(Set<Integer> usadas) {
        int id;
        do {
            id = rnd.nextInt(52);
        } while (usadas.contains(id));
        usadas.add(id);
        return Cartas.carta(id);
    }


    // =========================================================
    // REPARTO ANIMADO + CÁLCULO
    // =========================================================

    private void repartir() {
        if (ocupado) return;
        final Reparto reparto;
        try {
            reparto = prepararReparto();
        } catch (IllegalArgumentException e) {
            mostrarEstado(e.getMessage(), true);
            return;
        }

        empezarOperacion();
        manoActual = null;
        calleActual.setText("Escenario: " + switch (reparto.board.size()) {
            case 0 -> "PREFLOP";
            case 3 -> "FLOP";
            case 4 -> "TURN";
            default -> "RIVER";
        });
        animarReparto(reparto, true, 0);
    }

    private void animarReparto(Reparto reparto, boolean nuevo, int primerasVisibles) {
        mostrarEstado("Repartiendo...", false);
        if (nuevo) mesa.reiniciarTodo();
        else for (AsientoVista asiento : reparto.activos) asiento.limpiarResultado();

        // Hasta que termine el reparto no se indica si cada mano está dentro de su rango
        if (nuevo) manosRepartidas = null;
        actualizarRangos();

        long retraso = 0;
        final long intervalo = 750;

        // Una vuelta por carta: 2 en Hold'em, 4 en Omaha. En Omaha se reparte el
        // doble de cartas, así que van al doble de velocidad (tarda lo mismo)
        int cartasPorJugador = reparto.omaha ? 4 : 2;
        final double factor = reparto.omaha ? 0.5 : 1.0;
        final long intervaloJugadores = Math.round(intervalo * factor);
        for (int numeroCarta = 0; nuevo && numeroCarta < cartasPorJugador; numeroCarta++) {
            final int indiceCarta = numeroCarta;
            for (AsientoVista asiento : reparto.activos) {
                final AsientoVista destino = asiento;
                final Carta carta = reparto.manos.get(asiento.getIndice()).get(indiceCarta);
                programar(retraso, () -> crupier.repartirCarta(destino.getCartaVista(indiceCarta), carta, factor));
                retraso += intervaloJugadores;
            }
        }

        // Board
        for (int i = primerasVisibles; i < reparto.board.size(); i++) {
            final int indice = i;
            final Carta carta = reparto.board.get(i);
            programar(retraso, () -> crupier.repartirCarta(mesa.getCartaBoard(indice), carta));
            retraso += intervalo;
        }

        programar(retraso + 900, () -> calcularEquity(reparto));
    }

    private void programar(long milisegundos, Runnable accion) {
        final long version = versionOperacion;
        PauseTransition pausa = new PauseTransition(Duration.millis(milisegundos));
        pausas.add(pausa);
        pausa.setOnFinished(e -> {
            pausas.remove(pausa);
            if (version == versionOperacion) accion.run();
        });
        pausa.play();
    }

    private void calcularEquity(Reparto reparto) {
        final long version = versionOperacion;
        mostrarEstado("Calculando equity...", false);
        mesa.getBoard().forEach(CartaVista::quitarDestacado);
        for (AsientoVista asiento : reparto.activos) {
            asiento.limpiarResultado();
            asiento.getCartasVista().forEach(CartaVista::quitarDestacado);
        }

        // Ya están todas las cartas repartidas: comprobamos quién está dentro de su rango
        manosRepartidas = new ArrayList<>();
        for (int i = 0; i < MesaEquityVista.NUM_ASIENTOS; i++) {
            List<Carta> mano = reparto.manos.get(i);
            manosRepartidas.add(mano == null ? null : new ArrayList<>(mano));
        }
        actualizarRangos();

        final List<Rango> rangos = new ArrayList<>();
        final List<List<Carta>> manosActivas = new ArrayList<>();
        for (AsientoVista asiento : reparto.activos) {
            asiento.setEquity("...", null);
            manosActivas.add(reparto.manos.get(asiento.getIndice()));
            if (reparto.omaha) {
                continue;
            }

            Rango rangoEquity = reparto.rangosEquity[asiento.getIndice()];
            if (rangoEquity != null) {
                rangos.add(rangoEquity);
            } else {
                List<Carta> mano = reparto.manos.get(asiento.getIndice());
                rangos.add(Rango.deCartas(mano.get(0), mano.get(1)));
            }
        }

        final List<Carta> board = new ArrayList<>(reparto.board);

        // La simulación se hace en un hilo aparte para no congelar la ventana
        Task<CalculadoraEquity.Resultado> tarea = new Task<>() {
            @Override
            protected CalculadoraEquity.Resultado call() {
                if (reparto.omaha) {
                    return CalculadoraEquity.calcularOmaha(manosActivas, board, reparto.cartasRetiradas);
                }
                return CalculadoraEquity.calcular(rangos, board, reparto.cartasRetiradas,
                        ITERACIONES, new Random());
            }
        };

        tarea.setOnSucceeded(e -> {
            if (version != versionOperacion) return;
            mostrarResultado(reparto, tarea.getValue());
            terminarOperacion();
        });

        tarea.setOnFailed(e -> {
            if (version != versionOperacion) return;
            Throwable causa = tarea.getException();
            for (AsientoVista asiento : reparto.activos) {
                asiento.setEquity("", null);
            }
            mostrarEstado("No se pudo calcular: " + (causa == null ? "error desconocido" : causa.getMessage()), true);
            terminarOperacion();
        });
        tareaActual = tarea;
        iniciarHilo(tarea);
    }

    private void mostrarResultado(Reparto reparto, CalculadoraEquity.Resultado resultado) {
        for (int i = 0; i < reparto.activos.size(); i++) {
            AsientoVista asiento = reparto.activos.get(i);

            String texto = String.format("%.2f %%", resultado.equity[i] * 100);
            String detalle = String.format("Gana: %.2f %%   Empata: %.2f %%",
                    resultado.victorias[i] * 100, resultado.empates[i] * 100);

            // Apuestas: se guarda para las decisiones de la calle. Se muestra con
            // 2 decimales sin redondear, como en el enunciado (97,22 %, 2,77 %)
            if (esApuestas()) {
                equityCalle[asiento.getIndice()] = resultado.equity[i] * 100;
                texto = formatoEquity(equityCalle[asiento.getIndice()]);
            }

            Rango rangoEquity = reparto.rangosEquity[asiento.getIndice()];
            if (rangoEquity != null) {
                detalle = "Equity del rango completo (" + rangoEquity.size() + " combos).  " + detalle;
            }
            asiento.setEquity(texto, detalle);
        }

        if (reparto.activos.size() == 1) reparto.activos.get(0).marcarGanador(true);

        // Nombre de la mejor mano de cada jugador (con las clases ya existentes:
        // Jugador en Hold'em y JugadorOmaha en Omaha)
        if (reparto.board.size() >= 3) {
            List<ValorMano> mejores = new ArrayList<>();
            for (AsientoVista asiento : reparto.activos) {
                List<Carta> propias = new ArrayList<>(reparto.manos.get(asiento.getIndice()));
                List<Carta> comunes = new ArrayList<>(reparto.board);
                if (reparto.omaha) {
                    JugadorOmaha jugador = new JugadorOmaha(asiento.getIdJugador(), propias, comunes);
                    mejores.add(jugador.getMejorMano());
                    asiento.setManoTexto(jugador.getNombreMano());
                } else {
                    Jugador jugador = new Jugador(asiento.getIdJugador(), propias, comunes);
                    mejores.add(jugador.getMejorMano());
                    asiento.setManoTexto(jugador.getNombreMano());
                }
            }

            // Con el board completo hay showdown: resaltamos a los ganadores.
            // En el modo de apuestas el showdown llega al terminar las apuestas del river.
            if (reparto.board.size() == 5 && !esApuestas()) {
                ValorMano mejor = Collections.max(mejores, ValorMano::compararCon);
                for (int i = 0; i < mejores.size(); i++) {
                    if (mejores.get(i).compararCon(mejor) == 0) {
                        AsientoVista asiento = reparto.activos.get(i);
                        asiento.marcarGanador(true);
                        destacarMejorMano(asiento, mejores.get(i).getCartas());
                    }
                }
            }
        }

        if (esApuestas()) {
            equityCalleLista = true;
            mostrarEstado("Equity calculada (" + resultado.iteraciones + " boards). " + textoTurno(), false);
            return;
        }

        mostrarEstado((reparto.activos.size() == 1 ? "Ganador por Fold. " : "")
                + "Equity calculada: " + resultado.iteraciones + " repartos evaluados."
                + (manoActual != null && !manoActual.puedeAvanzar() ? " Mano finalizada." : ""), false);
    }

    // =========================================================
    // DECISIÓN EN EL TURN (práctica 3, apartado 2.2)
    // =========================================================

    // Asiento del villano (el de arriba); hero es el asiento 0
    private static final int VILLANO = 3;

    private String nombreAsientoDecision(int indice) {
        if (esTurn()) return indice == 0 ? "Hero" : indice == VILLANO ? "Villano" : "";
        return RondaApuestas.POSICIONES[indice];
    }

    // Reparte hero y el board del Turn (aleatorios o manuales), deja las cartas del
    // villano boca abajo y decide Call o Fold con la media de outs contra su rango.
    private void decidirTurn() {
        if (ocupado) return;
        AsientoVista hero = mesa.getAsiento(0);
        AsientoVista villano = mesa.getAsiento(VILLANO);

        final List<Carta> cartasHero;
        final List<Carta> board;
        final Rango rango;
        final double em;
        final DecisionTurn.Resultado resultado;
        try {
            Set<Integer> usadas = new HashSet<>();

            // Board del Turn: 4 cartas
            if (modoBoard.getValue().contains("manual")) {
                board = Cartas.parsear(boardManual.getText());
                if (board.size() != 4) {
                    throw new IllegalArgumentException("El board del Turn debe tener 4 cartas (has escrito "
                            + board.size() + ").");
                }
                for (Carta c : board) usadas.add(Cartas.id(c));
            } else {
                board = new ArrayList<>();
            }

            // Cartas de hero
            String texto = hero.getCartasTexto();
            if (modoCartas.getValue().contains("manual") && texto != null && !texto.isBlank()) {
                cartasHero = Cartas.parsear(texto);
                if (cartasHero.size() != 2) {
                    throw new IllegalArgumentException("Hero: escribe exactamente 2 cartas (por ejemplo AhKd).");
                }
                for (Carta c : cartasHero) {
                    if (!usadas.add(Cartas.id(c))) {
                        throw new IllegalArgumentException("La carta " + c + " está repetida (hero y board).");
                    }
                }
            } else {
                cartasHero = new ArrayList<>();
                cartasHero.add(robarCarta(usadas));
                cartasHero.add(robarCarta(usadas));
            }
            while (board.size() < 4) board.add(robarCarta(usadas));

            // Rango del villano y EM de hero
            try {
                rango = obtenerRango(villano);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Villano: rango no válido. " + e.getMessage());
            }
            if (rango == null) throw new IllegalArgumentException("Escribe el rango del villano (texto o Top %).");
            try {
                em = obtenerEM(hero);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Hero: " + e.getMessage());
            }
            resultado = DecisionTurn.evaluar(cartasHero, board, rango, em);
        } catch (IllegalArgumentException e) {
            mostrarEstado(e.getMessage(), true);
            return;
        }

        empezarOperacion();
        manoActual = null;
        manosRepartidas = null;
        Arrays.fill(colorEMDecision, null);
        Arrays.fill(colorRangoDecision, null);
        mesa.reiniciarTodo();
        actualizarRangos();
        calleActual.setText("Escenario: TURN");
        mostrarEstado("Repartiendo...", false);

        long retraso = 0;
        final long intervalo = 750;
        for (int i = 0; i < 2; i++) {
            final int indice = i;
            programar(retraso, () -> crupier.repartirCarta(hero.getCartaVista(indice), cartasHero.get(indice), 1.0));
            retraso += intervalo;
        }
        for (int i = 0; i < 4; i++) {
            final int indice = i;
            programar(retraso, () -> crupier.repartirCarta(mesa.getCartaBoard(indice), board.get(indice)));
            retraso += intervalo;
        }

        // La mano del villano no se muestra: sus dos cartas quedan boca abajo
        programar(retraso, () -> {
            for (int i = 0; i < 2; i++) {
                CartaVista oculta = villano.getCartaVista(i);
                oculta.mostrarReverso();
                oculta.setVisible(true);
            }
        });

        programar(retraso + 400, () -> {
            String enRango = String.format("%.2f", resultado.mediaOuts).replace('.', ',');
            hero.setEquity(enRango + " outs", DecisionTurn.resumen(resultado));
            hero.setManoTexto(new Jugador("Hero", new ArrayList<>(cartasHero), new ArrayList<>(board)).getNombreMano());
            hero.setAccion(resultado.call ? "Call" : "Fold");

            // EM: verde si la media de outs lo alcanza, rojo si no
            colorEMDecision[0] = resultado.call ? FONDO_DENTRO : FONDO_FUERA;
            actualizarRangos();

            mostrarEstado("Hero " + (resultado.call ? "hace CALL" : "hace FOLD") + ": media de " + enRango
                    + " outs " + (resultado.call ? ">= " : "< ") + "EM " + String.format("%.2f", em).replace('.', ',')
                    + ".\n" + DecisionTurn.resumen(resultado), false);
            terminarOperacion();
        });
    }


    // =========================================================
    // APUESTAS (práctica 3, apartado 2.1)
    // =========================================================

    // Barra debajo de la mesa: bote, de quién es el turno y los botones de decisión
    private void construirBarraApuestas() {
        boteLabel = new Label("");
        boteLabel.setStyle("-fx-text-fill: #F2D16B; -fx-font-size: 14px; -fx-font-weight: bold;");
        boteLabel.setMinWidth(110);

        turnoLabel = new Label("");
        turnoLabel.setStyle("-fx-text-fill: #4FC3F7; -fx-font-size: 13px; -fx-font-weight: bold;");
        turnoLabel.setMinWidth(230);

        botonBetManual = new Button("Bet manual (3 BB)");
        botonBetManual.setOnAction(e -> betManual());
        botonAutomatico = new Button("Decisión automática");
        botonAutomatico.setOnAction(e -> decisionAutomatica());
        botonAutoCalle = new Button("Automático hasta fin de calle");
        botonAutoCalle.setOnAction(e -> decidirRestoCalle());

        String activo = "-fx-background-color: #2D493D; -fx-text-fill: #F0F4EE;"
                + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-pref-height: 36; -fx-cursor: hand;";
        String inactivo = "-fx-background-color: #35403C; -fx-text-fill: #A9B3AE;"
                + "-fx-background-radius: 8; -fx-pref-height: 36;";
        for (Button boton : List.of(botonBetManual, botonAutomatico, botonAutoCalle)) {
            boton.setStyle(inactivo);
            boton.setDisable(true);
            boton.disabledProperty().addListener((obs, antes, deshabilitado) -> {
                boton.setOpacity(1.0);
                boton.setStyle(deshabilitado ? inactivo : activo);
            });
        }

        barraApuestas = new HBox(10, boteLabel, turnoLabel, botonBetManual, botonAutomatico, botonAutoCalle);
        barraApuestas.setAlignment(Pos.CENTER);
        // Margen superior: el asiento de abajo sobresale un poco de la mesa
        barraApuestas.setPadding(new Insets(24, 0, 8, 0));
        barraApuestas.setVisible(false);
        barraApuestas.setManaged(false);
    }

    private void actualizarControlesApuestas() {
        boolean apuestas = esApuestas();
        barraApuestas.setVisible(apuestas);
        barraApuestas.setManaged(apuestas);

        boolean disponible = turnoDisponible();
        int turno = disponible ? ronda.getTurno() : -1;
        botonBetManual.setDisable(!disponible || !ronda.puedeBet());
        botonAutomatico.setDisable(!disponible);
        botonAutoCalle.setDisable(!disponible);
        for (AsientoVista asiento : mesa.getAsientos()) {
            asiento.marcarTurno(asiento.getIndice() == turno);
        }

        boteLabel.setText(ronda != null ? "Bote: " + formatoBB(ronda.getBote()) : "Bote: —");

        if (!apuestas) turnoLabel.setText("");
        else if (ronda == null) turnoLabel.setText("Pulsa Nueva mano para empezar");
        else if (turno >= 0) turnoLabel.setText("Turno: " + RondaApuestas.POSICIONES[turno]);
        else if (ronda.manoTerminada()) turnoLabel.setText("Mano finalizada. " + ganadoresTexto);
        else if (ronda.calleTerminada()) turnoLabel.setText("Apuestas del " + manoActual.getCalle() + " terminadas");
        else turnoLabel.setText("Calculando equity...");
    }

    // Se puede decidir cuando ya está calculada la equity de la calle y queda alguien por hablar
    private boolean turnoDisponible() {
        return esApuestas() && !ocupado && ronda != null && equityCalleLista && !ronda.calleTerminada();
    }

    // Al empezar una mano o una calle: sin colores ni equity de la calle
    private void limpiarDecisiones() {
        Arrays.fill(colorRangoDecision, null);
        Arrays.fill(colorEMDecision, null);
        Arrays.fill(equityCalle, 0);
        equityCalleLista = false;
        actualizarRangos();
    }

    private String textoTurno() {
        int turno = ronda == null ? -1 : ronda.getTurno();
        if (turno < 0) return "";
        return "Turno de " + RondaApuestas.POSICIONES[turno]
                + (ronda.hayApuesta() ? " (hay una apuesta: Call o Fold)." : " (sin apuesta previa: Bet o Fold).");
    }

    // Bet manual: el usuario fuerza la apuesta sin tener en cuenta el rango ni el EM.
    // Solo si no hay una apuesta previa en la calle.
    private void betManual() {
        if (!turnoDisponible() || !ronda.puedeBet()) return;
        int turno = ronda.getTurno();
        AsientoVista asiento = mesa.getAsiento(turno);
        List<Carta> cartas = manoActual.getManos().get(turno);

        // Si el jugador tiene rango y EM escritos, se pintan igualmente (como en el enunciado)
        try {
            Rango rango = obtenerRango(asiento);
            if (rango != null) {
                colorRangoDecision[turno] = rango.contiene(cartas.get(0), cartas.get(1)) ? FONDO_DENTRO : FONDO_FUERA;
            }
        } catch (IllegalArgumentException e) {
            // Rango no válido: no se pinta
        }
        try {
            colorEMDecision[turno] = equityCalle[turno] >= obtenerEM(asiento) ? FONDO_DENTRO : FONDO_FUERA;
        } catch (IllegalArgumentException e) {
            // Sin EM o EM no válido: no se pinta
        }

        aplicarAccion(RondaApuestas.Accion.BET, " (manual)", "Bet forzado por el usuario.");
    }

    // Decisión automática del jugador que tiene el turno. Necesita su rango y su EM.
    // Devuelve false si no se ha podido decidir.
    private boolean decisionAutomatica() {
        if (!turnoDisponible()) return false;
        int turno = ronda.getTurno();
        AsientoVista asiento = mesa.getAsiento(turno);
        String nombre = RondaApuestas.POSICIONES[turno];

        Rango rango;
        double equityMinimo;
        try {
            rango = obtenerRango(asiento);
            if (rango == null) throw new IllegalArgumentException("escribe su rango para decidir automáticamente.");
            equityMinimo = obtenerEM(asiento);
        } catch (IllegalArgumentException e) {
            mostrarEstado(nombre + ": " + e.getMessage(), true);
            return false;
        }

        List<Carta> cartas = manoActual.getManos().get(turno);
        boolean enRango = rango.contiene(cartas.get(0), cartas.get(1));
        double equity = equityCalle[turno];
        boolean llegaAlEM = equity >= equityMinimo;

        // Rango: verde si la mano está dentro. EM: verde si el equity llega al EM.
        colorRangoDecision[turno] = enRango ? FONDO_DENTRO : FONDO_FUERA;
        colorEMDecision[turno] = llegaAlEM ? FONDO_DENTRO : FONDO_FUERA;

        String motivo = Rango.clase(cartas.get(0), cartas.get(1))
                + (enRango ? " dentro" : " fuera") + " del rango; equity "
                + formatoEquity(equity) + (llegaAlEM ? " >= " : " < ")
                + "EM " + String.format("%.2f %%", equityMinimo) + ".";
        aplicarAccion(ronda.decidir(enRango, equity, equityMinimo), " (automático)", motivo);
        return true;
    }

    // Decide automáticamente a todos los jugadores que faltan por hablar en la calle.
    // Se para si a alguno le falta el rango o el EM.
    private void decidirRestoCalle() {
        while (turnoDisponible() && decisionAutomatica()) {
            // cada vuelta decide un jugador
        }
    }

    private void aplicarAccion(RondaApuestas.Accion accion, String modo, String motivo) {
        int turno = ronda.getTurno();
        AsientoVista asiento = mesa.getAsiento(turno);
        ronda.aplicar(accion);

        String texto = switch (accion) {
            case BET -> "Bet " + formatoBB(RondaApuestas.APUESTA) + modo;
            case CALL -> "Call " + formatoBB(RondaApuestas.APUESTA);
            case FOLD -> "Fold";
        };
        if (accion == RondaApuestas.Accion.FOLD) {
            asiento.setRetirado(true);
        }
        asiento.setAccion(texto);
        actualizarRangos();

        // El turno siguiente ya se ve en la barra de apuestas
        String mensaje = RondaApuestas.POSICIONES[turno] + ": " + texto + ". " + motivo;
        if (ronda.manoTerminada()) {
            finalizarMano();
        } else if (ronda.calleTerminada()) {
            mostrarEstado(mensaje + " Pulsa \"Mostrar "
                    + manoActual.getCalle().siguiente().name().toLowerCase(java.util.Locale.ROOT) + "\".", false);
        } else {
            mostrarEstado(mensaje, false);
        }
        actualizarControles();
    }

    // Fin de la mano: si solo queda uno se lleva el bote; si no, showdown en el river
    // (con empate el bote se reparte a partes iguales)
    private void finalizarMano() {
        List<Integer> enMano = manoActual.getJugadoresEnMano();
        List<Integer> ganadores = new ArrayList<>();
        String como;

        if (enMano.size() == 1) {
            ganadores.add(enMano.get(0));
            como = "el resto ha hecho Fold";
        } else {
            List<Carta> board = manoActual.getBoardVisible();
            List<Jugador> jugadores = new ArrayList<>();
            List<ValorMano> mejores = new ArrayList<>();
            for (int indice : enMano) {
                Jugador jugador = new Jugador(RondaApuestas.POSICIONES[indice],
                        new ArrayList<>(manoActual.getManos().get(indice)), new ArrayList<>(board));
                jugadores.add(jugador);
                mejores.add(jugador.getMejorMano());
            }
            ValorMano mejor = Collections.max(mejores, ValorMano::compararCon);
            String nombreMano = "";
            for (int i = 0; i < mejores.size(); i++) {
                if (mejores.get(i).compararCon(mejor) == 0) {
                    ganadores.add(enMano.get(i));
                    nombreMano = jugadores.get(i).getNombreMano();
                    destacarMejorMano(mesa.getAsiento(enMano.get(i)), mejores.get(i).getCartas());
                }
            }
            como = "showdown con " + nombreMano;
        }

        List<String> nombres = new ArrayList<>();
        for (int indice : ganadores) {
            mesa.getAsiento(indice).marcarGanador(true);
            nombres.add(RondaApuestas.POSICIONES[indice]);
        }
        ganadoresTexto = (ganadores.size() == 1 ? "Gana " : "Empate: ") + String.join(", ", nombres);
        String reparto = ganadores.size() == 1
                ? "Gana " + nombres.get(0) + " (" + como + ") y se lleva el bote de " + formatoBB(ronda.getBote()) + "."
                : "Empate entre " + String.join(", ", nombres) + " (" + como + "): "
                        + formatoBB(ronda.getBote() / ganadores.size()) + " para cada uno.";
        mostrarEstado("Mano finalizada. " + reparto, false);
    }

    // Equity mínimo (EM) escrito en el asiento, de 0 a 100
    private double obtenerEM(AsientoVista asiento) {
        String texto = asiento.getEMTexto();
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("escribe su equity mínimo (EM) para decidir automáticamente.");
        }
        double valor;
        try {
            valor = Double.parseDouble(texto.trim().replace("%", "").replace(',', '.').trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("EM no válido: \"" + texto.trim() + "\"");
        }
        if (valor < 0 || valor > 100) throw new IllegalArgumentException("el EM debe estar entre 0 y 100.");
        return valor;
    }

    // Equity (0..100) con 2 decimales sin redondear, como en el enunciado: 97,2222 -> "97,22 %"
    private static String formatoEquity(double porcentaje) {
        return String.format("%.2f %%", Math.floor(porcentaje * 100 + 1e-9) / 100);
    }

    // 3 -> "3 BB", 10.5 -> "10,5 BB"
    private static String formatoBB(double bb) {
        return (bb == Math.rint(bb) ? String.format("%.0f", bb) : String.format("%.2f", bb).replaceAll("0$", ""))
                + " BB";
    }

    // Resalta las cinco cartas que forman la mejor mano del jugador
    private void destacarMejorMano(AsientoVista asiento, List<Carta> mejorMano) {
        List<CartaVista> vistas = new ArrayList<>(asiento.getCartasVista());
        vistas.addAll(mesa.getBoard());

        for (Carta cartaMejor : mejorMano) {
            for (CartaVista vista : vistas) {
                if (vista.representa(cartaMejor)) {
                    vista.destacar();
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
