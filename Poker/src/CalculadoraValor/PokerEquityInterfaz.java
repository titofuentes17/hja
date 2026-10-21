package CalculadoraValor;

import java.util.ArrayList;
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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

// Calculadora de equity para 6 jugadores.
//
//  - El hero (abajo) puede definir un RANGO de dos formas excluyentes:
//      * en texto (AA,KK,AKs,77+,A5s-A2s...)
//      * por porcentaje de mejores manos según un ranking
//  - Las cartas de los jugadores y el board pueden ser aleatorios o manuales.
//  - Todos los jugadores muestran su equity en un cuadro de solo lectura.
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

    // Controles del hero
    private TextField rangoTexto;
    private TextField porcentaje;
    private ComboBox<String> rankingCombo;
    private Label resumenRango;
    private CheckBox equityRango;
    private CheckBox heroEnRango;

    // Mano con la que juega el hero en el último reparto (null si aún no hay)
    private List<Carta> manoHero;

    // Controles de cartas
    private ComboBox<String> modoCartas;
    private ComboBox<String> modoBoard;
    private ComboBox<String> calleBoard;
    private TextField boardManual;

    private Button botonRepartir;
    private Label estado;


    // =========================================================
    // VENTANA
    // =========================================================

    @Override
    public void start(Stage escenario) {
        StackPane fondoGeneral = new StackPane();
        fondoGeneral.setStyle("-fx-background-color: linear-gradient(to bottom, #071A14, #0B2B20, #07110E);");

        BorderPane raiz = new BorderPane();
        raiz.setPadding(new Insets(8));
        raiz.setStyle("-fx-background-color: transparent;");

        Label titulo = new Label("♠  CALCULADORA DE EQUITY  ♥");
        titulo.setStyle("-fx-font-size: 21px; -fx-font-weight: bold; -fx-text-fill: #F5F5F5;");
        VBox parteSuperior = new VBox(titulo);
        parteSuperior.setAlignment(Pos.CENTER);
        raiz.setTop(parteSuperior);

        crupier = new CrupierVista();
        mesa = new MesaEquityVista();

        VBox centro = new VBox(2, crupier, mesa);
        centro.setAlignment(Pos.CENTER);
        raiz.setCenter(centro);

        raiz.setRight(construirPanelControl());
        BorderPane.setMargin(raiz.getRight(), new Insets(0, 0, 0, 10));

        ScrollPane desplazable = new ScrollPane(raiz);
        desplazable.setFitToWidth(true);
        desplazable.setFitToHeight(true);
        desplazable.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        fondoGeneral.getChildren().add(desplazable);

        actualizarResumenRango();

        Scene escena = new Scene(fondoGeneral, 1330, 800);
        escenario.setTitle("Calculadora de Equity - Poker");
        escenario.setScene(escena);
        escenario.show();
    }


    // =========================================================
    // PANEL DE CONTROL (RANGO DEL HERO + CARTAS)
    // =========================================================

    private VBox construirPanelControl() {
        // ---------- Rango del hero ----------
        Label tituloRango = tituloSeccion("RANGO DEL HERO");

        rangoTexto = new TextField();
        rangoTexto.setPromptText("Texto: AA,KK,AKs,77+,A5s-A2s");

        Label o = etiqueta("— o —");
        o.setMaxWidth(Double.MAX_VALUE);
        o.setAlignment(Pos.CENTER);

        porcentaje = new TextField();
        porcentaje.setPromptText("Top % (ej. 15)");
        porcentaje.setPrefWidth(110);

        rankingCombo = new ComboBox<>();
        rankingCombo.getItems().addAll(RankingManos.NOMBRES);
        rankingCombo.setValue(RankingManos.NOMBRES.get(0));
        rankingCombo.setPrefWidth(170);

        HBox filaPorcentaje = new HBox(8, porcentaje, rankingCombo);
        filaPorcentaje.setAlignment(Pos.CENTER_LEFT);

        resumenRango = new Label();
        resumenRango.setWrapText(true);
        resumenRango.setMaxWidth(290);

        equityRango = new CheckBox("Equity del rango completo del hero");
        equityRango.setStyle("-fx-text-fill: #E2E2E2; -fx-font-size: 12px;");

        heroEnRango = new CheckBox("Repartir al hero una mano de su rango");
        heroEnRango.setStyle("-fx-text-fill: #E2E2E2; -fx-font-size: 12px;");

        Label notaRango = etiqueta("Con esta opción la equity del hero se calcula sobre todas las "
                + "manos de su rango, no solo sobre la mano repartida.");
        notaRango.setWrapText(true);
        notaRango.setMaxWidth(290);
        notaRango.setStyle("-fx-text-fill: #8FA89C; -fx-font-size: 10px;");

        // Los dos cuadros de rango son mutuamente excluyentes:
        // si uno tiene texto, el otro queda deshabilitado
        rangoTexto.textProperty().addListener((obs, antes, ahora) -> {
            boolean enUso = !ahora.isBlank();
            porcentaje.setDisable(enUso);
            rankingCombo.setDisable(enUso);
            actualizarResumenRango();
        });

        porcentaje.textProperty().addListener((obs, antes, ahora) -> {
            rangoTexto.setDisable(!ahora.isBlank());
            actualizarResumenRango();
        });

        rankingCombo.setOnAction(e -> actualizarResumenRango());

        // ---------- Cartas ----------
        Label tituloCartas = tituloSeccion("CARTAS");

        modoCartas = new ComboBox<>();
        modoCartas.getItems().addAll("Cartas aleatorias", "Cartas manuales");
        modoCartas.setValue("Cartas aleatorias");
        modoCartas.setMaxWidth(Double.MAX_VALUE);

        Label ayudaCartas = etiqueta("En modo manual escribe las cartas en cada asiento "
                + "(si lo dejas vacío, ese jugador recibe cartas aleatorias).");
        ayudaCartas.setWrapText(true);
        ayudaCartas.setMaxWidth(290);
        ayudaCartas.setStyle("-fx-text-fill: #8FA89C; -fx-font-size: 10px;");

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
            calleBoard.setDisable(manual);
            boardManual.setDisable(!manual);
        });

        // ---------- Botón y estado ----------
        botonRepartir = new Button("▶  REPARTIR Y CALCULAR");
        botonRepartir.setMaxWidth(Double.MAX_VALUE);
        botonRepartir.setStyle("-fx-background-color: #C9A227;" + "-fx-text-fill: #111111;" + "-fx-font-weight: bold;"
                + "-fx-font-size: 13px;" + "-fx-background-radius: 8;" + "-fx-padding: 8 22 8 22;" + "-fx-cursor: hand;");
        botonRepartir.setOnAction(e -> repartir());

        estado = new Label("Pulsa REPARTIR para empezar.");
        estado.setWrapText(true);
        estado.setMaxWidth(290);
        estado.setStyle("-fx-text-fill: " + COLOR_NEUTRO + "; -fx-font-size: 12px;");

        VBox panel = new VBox(7,
                tituloRango, rangoTexto, o, filaPorcentaje, resumenRango, heroEnRango, equityRango, notaRango,
                new Separator(),
                tituloCartas, modoCartas, ayudaCartas, modoBoard, calleBoard, boardManual,
                new Separator(),
                botonRepartir, estado);

        panel.setPadding(new Insets(12));
        panel.setPrefWidth(320);
        panel.setMinWidth(320);
        panel.setMaxWidth(320);
        panel.setAlignment(Pos.TOP_LEFT);
        panel.setStyle("-fx-background-color: rgba(5,18,14,0.92);" + "-fx-border-color: #C9A227;" + "-fx-border-width: 2;"
                + "-fx-border-radius: 10;" + "-fx-background-radius: 10;");

        return panel;
    }

    private Label tituloSeccion(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #D6B85A;");
        return l;
    }

    private Label etiqueta(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #E2E2E2;");
        return l;
    }

    private void mostrarEstado(String mensaje, boolean error) {
        estado.setText(mensaje);
        estado.setStyle("-fx-text-fill: " + (error ? COLOR_ERROR : COLOR_NEUTRO) + "; -fx-font-size: 12px;");
    }


    // =========================================================
    // RANGO DEL HERO
    // =========================================================

    // Devuelve el rango definido por el usuario, o null si no hay ninguno.
    // Lanza IllegalArgumentException si lo escrito no es válido.
    private Rango obtenerRangoHero() {
        String texto = rangoTexto.getText();
        if (texto != null && !texto.isBlank()) {
            return Rango.desdeTexto(texto);
        }

        String pct = porcentaje.getText();
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

    // Actualiza el mensaje bajo los cuadros de rango y el color de fondo del cuadro en uso:
    //   verde    -> la mano del hero está dentro del rango
    //   rojo     -> la mano del hero está fuera del rango
    //   naranja  -> el texto del rango no es válido
    //   sin color-> todavía no hay mano repartida (o no hay rango)
    private void actualizarResumenRango() {
        Rango rango = null;
        String error = null;

        try {
            rango = obtenerRangoHero();
        } catch (IllegalArgumentException e) {
            error = e.getMessage();
        }

        if (error != null) {
            resumenRango.setText(error);
            resumenRango.setStyle("-fx-text-fill: " + COLOR_ERROR + "; -fx-font-size: 12px;");
            pintarCampoRango(FONDO_INVALIDO);
            return;
        }

        if (rango == null) {
            resumenRango.setText("Sin rango: el hero puede recibir cualquier mano.");
            resumenRango.setStyle("-fx-text-fill: " + COLOR_NEUTRO + "; -fx-font-size: 12px;");
            pintarCampoRango(null);
            return;
        }

        String texto = "Rango válido: " + rango.size() + " combinaciones ("
                + String.format("%.1f", rango.porcentaje()) + " % de las manos).";

        if (manoHero == null) {
            resumenRango.setText(texto);
            resumenRango.setStyle("-fx-text-fill: " + COLOR_OK + "; -fx-font-size: 12px;");
            pintarCampoRango(null);
            return;
        }

        boolean dentro = rango.contiene(manoHero.get(0), manoHero.get(1));
        texto += "\nMano del hero: " + Utils.cartasAString(manoHero)
                + " (" + Rango.clase(manoHero.get(0), manoHero.get(1)) + ") → "
                + (dentro ? "DENTRO del rango" : "FUERA del rango");

        resumenRango.setText(texto);
        resumenRango.setStyle("-fx-text-fill: " + (dentro ? COLOR_OK : COLOR_ERROR) + "; -fx-font-size: 12px;");
        pintarCampoRango(dentro ? FONDO_DENTRO : FONDO_FUERA);
    }

    // Pinta el fondo del cuadro de rango que se está usando (el otro queda sin color)
    private void pintarCampoRango(String colorFondo) {
        rangoTexto.setStyle("");
        porcentaje.setStyle("");

        if (colorFondo == null) {
            return;
        }

        TextField enUso = !rangoTexto.getText().isBlank() ? rangoTexto : porcentaje;
        enUso.setStyle("-fx-control-inner-background: " + colorFondo + "; -fx-text-fill: #111111;");
    }


    // =========================================================
    // PREPARACIÓN DEL REPARTO (validación y elección de cartas)
    // =========================================================

    private static final class Reparto {
        final List<AsientoVista> activos = new ArrayList<>();
        final List<List<Carta>> manos = new ArrayList<>();   // una entrada por asiento (null si no juega)
        List<Carta> board = new ArrayList<>();
        Rango rangoHeroParaEquity;                          // null si se calcula con la mano repartida
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

        // ---------- Hero: mano aleatoria según su rango ----------
        Rango rangoHero;
        try {
            rangoHero = obtenerRangoHero();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rango del hero: " + e.getMessage());
        }

        if (equityRango.isSelected() && rangoHero != null) {
            reparto.rangoHeroParaEquity = rangoHero;
        }

        if (reparto.manos.get(0) == null) {
            // Por defecto el hero recibe una mano cualquiera y el cuadro de rango indica
            // si cae dentro o fuera. Con la casilla marcada, se reparte dentro del rango.
            Rango fuente = (heroEnRango.isSelected() && rangoHero != null) ? rangoHero : Rango.todas();

            List<int[]> libres = new ArrayList<>();
            for (int[] combo : fuente.getCombos()) {
                if (!usadas.contains(combo[0]) && !usadas.contains(combo[1])) {
                    libres.add(combo);
                }
            }
            if (libres.isEmpty()) {
                throw new IllegalArgumentException(
                        "El rango del hero no es compatible con las cartas ya elegidas (board o jugadores).");
            }

            int[] elegido = libres.get(rnd.nextInt(libres.size()));
            usadas.add(elegido[0]);
            usadas.add(elegido[1]);

            List<Carta> mano = new ArrayList<>();
            mano.add(Cartas.carta(elegido[0]));
            mano.add(Cartas.carta(elegido[1]));
            reparto.manos.set(0, mano);
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
        final Reparto reparto;
        try {
            reparto = prepararReparto();
        } catch (IllegalArgumentException e) {
            mostrarEstado(e.getMessage(), true);
            return;
        }

        botonRepartir.setDisable(true);
        mostrarEstado("Repartiendo...", false);
        mesa.reiniciarTodo();

        // Hasta que termine el reparto no se indica si el hero está dentro del rango
        manoHero = null;
        actualizarResumenRango();

        long retraso = 0;
        final long intervalo = 650;

        // Dos vueltas de cartas a los jugadores
        for (int numeroCarta = 0; numeroCarta < 2; numeroCarta++) {
            final int indiceCarta = numeroCarta;
            for (AsientoVista asiento : reparto.activos) {
                final AsientoVista destino = asiento;
                final Carta carta = reparto.manos.get(asiento.getIndice()).get(indiceCarta);
                programar(retraso, () -> crupier.repartirCarta(destino.getCartaVista(indiceCarta), carta));
                retraso += intervalo;
            }
        }

        // Board
        for (int i = 0; i < reparto.board.size(); i++) {
            final int indice = i;
            final Carta carta = reparto.board.get(i);
            programar(retraso, () -> crupier.repartirCarta(mesa.getCartaBoard(indice), carta));
            retraso += intervalo;
        }

        programar(retraso + 900, () -> calcularEquity(reparto));
    }

    private void programar(long milisegundos, Runnable accion) {
        PauseTransition pausa = new PauseTransition(Duration.millis(milisegundos));
        pausa.setOnFinished(e -> accion.run());
        pausa.play();
    }

    private void calcularEquity(Reparto reparto) {
        mostrarEstado("Calculando equity...", false);

        // Ya están todas las cartas repartidas: comprobamos si el hero está en su rango
        manoHero = new ArrayList<>(reparto.manos.get(0));
        actualizarResumenRango();

        final List<Rango> rangos = new ArrayList<>();
        for (AsientoVista asiento : reparto.activos) {
            asiento.setEquity("...", null);

            if (asiento.getIndice() == 0 && reparto.rangoHeroParaEquity != null) {
                rangos.add(reparto.rangoHeroParaEquity);
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
                return CalculadoraEquity.calcular(rangos, board, ITERACIONES, new Random());
            }
        };

        tarea.setOnSucceeded(e -> {
            mostrarResultado(reparto, tarea.getValue());
            botonRepartir.setDisable(false);
        });

        tarea.setOnFailed(e -> {
            Throwable causa = tarea.getException();
            for (AsientoVista asiento : reparto.activos) {
                asiento.setEquity("", null);
            }
            mostrarEstado("No se pudo calcular: " + (causa == null ? "error desconocido" : causa.getMessage()), true);
            botonRepartir.setDisable(false);
        });

        Thread hilo = new Thread(tarea);
        hilo.setDaemon(true);
        hilo.start();
    }

    private void mostrarResultado(Reparto reparto, CalculadoraEquity.Resultado resultado) {
        for (int i = 0; i < reparto.activos.size(); i++) {
            AsientoVista asiento = reparto.activos.get(i);

            String texto = String.format("%.2f %%", resultado.equity[i] * 100);
            String detalle = String.format("Gana: %.2f %%   Empata: %.2f %%",
                    resultado.victorias[i] * 100, resultado.empates[i] * 100);

            if (asiento.getIndice() == 0 && reparto.rangoHeroParaEquity != null) {
                detalle = "Equity del rango completo (" + reparto.rangoHeroParaEquity.size() + " combos).  " + detalle;
            }
            asiento.setEquity(texto, detalle);
        }

        // Nombre de la mejor mano de cada jugador (con las clases ya existentes)
        if (reparto.board.size() >= 3) {
            List<Jugador> jugadores = new ArrayList<>();
            for (AsientoVista asiento : reparto.activos) {
                Jugador jugador = new Jugador(asiento.getIdJugador(),
                        new ArrayList<>(reparto.manos.get(asiento.getIndice())),
                        new ArrayList<>(reparto.board));
                jugadores.add(jugador);
                asiento.setManoTexto(jugador.getNombreMano());
            }

            // Con el board completo hay showdown: resaltamos a los ganadores
            if (reparto.board.size() == 5) {
                Jugador mejor = Collections.max(jugadores);
                for (int i = 0; i < jugadores.size(); i++) {
                    if (jugadores.get(i).compareTo(mejor) == 0) {
                        AsientoVista asiento = reparto.activos.get(i);
                        asiento.marcarGanador(true);
                        destacarMejorMano(asiento, jugadores.get(i));
                    }
                }
            }
        }

        mostrarEstado("", false);
    }

    // Resalta las cinco cartas que forman la mejor mano del jugador
    private void destacarMejorMano(AsientoVista asiento, Jugador jugador) {
        List<CartaVista> vistas = new ArrayList<>(asiento.getCartasVista());
        vistas.addAll(mesa.getBoard());

        for (Carta cartaMejor : jugador.getMejorMano().getCartas()) {
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
