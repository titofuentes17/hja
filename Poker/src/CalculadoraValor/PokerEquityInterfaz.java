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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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

    // Controles de rango (generales; cada jugador tiene los suyos en su asiento)
    private ComboBox<String> rankingCombo;
    private CheckBox repartirEnRango;
    private CheckBox equityRango;

    // Controles de cartas
    private ComboBox<String> modoCartas;
    private ComboBox<String> modoBoard;
    private ComboBox<String> calleBoard;
    private TextField boardManual;

    private Button botonRepartir;
    private Label estado;

    // Manos repartidas en el último reparto, por asiento (null si aún no hay reparto
    // o si el asiento no juega). Sirve para comprobar si cada mano está en su rango.
    private List<List<Carta>> manosRepartidas;


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

        // Cada asiento avisa a la interfaz cuando cambia su rango
        for (AsientoVista asiento : mesa.getAsientos()) {
            asiento.setOnRangoCambiado(this::actualizarRangos);
        }

        ScrollPane desplazable = new ScrollPane(raiz);
        desplazable.setFitToWidth(true);
        desplazable.setFitToHeight(true);
        desplazable.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        fondoGeneral.getChildren().add(desplazable);

        actualizarRangos();

        Scene escena = new Scene(fondoGeneral, 1340, 880);
        escenario.setTitle("Calculadora de Equity - Poker");
        escenario.setScene(escena);
        escenario.show();
    }


    // =========================================================
    // PANEL DE CONTROL (RANGOS + CARTAS)
    // =========================================================

    private VBox construirPanelControl() {
        // ---------- Rangos ----------
        Label tituloRango = tituloSeccion("RANGOS");

        Label ayudaRango = etiqueta("Cada jugador tiene su cuadro de rango bajo su equity: escribe un "
                + "rango (AA,KK,AKs,77+,A5s-A2s) o un porcentaje de mejores manos. "
                + "Si usas uno, el otro se desactiva.");
        ayudaRango.setWrapText(true);
        ayudaRango.setMaxWidth(290);
        ayudaRango.setStyle("-fx-text-fill: #8FA89C; -fx-font-size: 11px;");

        Label etiquetaRanking = etiqueta("Ranking para los porcentajes:");

        rankingCombo = new ComboBox<>();
        rankingCombo.getItems().addAll(RankingManos.NOMBRES);
        rankingCombo.setValue(RankingManos.NOMBRES.get(0));
        rankingCombo.setMaxWidth(Double.MAX_VALUE);
        rankingCombo.setOnAction(e -> actualizarRangos());

        Label leyenda = new Label("Verde: la mano está dentro del rango\n"
                + "Rojo: la mano está fuera del rango\n"
                + "Naranja: el rango no es válido");
        leyenda.setStyle("-fx-text-fill: #E2E2E2; -fx-font-size: 11px;");

        repartirEnRango = new CheckBox("Repartir las manos dentro de los rangos");
        repartirEnRango.setStyle("-fx-text-fill: #E2E2E2; -fx-font-size: 12px;");

        equityRango = new CheckBox("Equity del rango completo");
        equityRango.setStyle("-fx-text-fill: #E2E2E2; -fx-font-size: 12px;");

        Label notaRango = etiqueta("Sin marcar «Repartir», cada jugador recibe una mano cualquiera y el "
                + "cuadro indica si está dentro o fuera. Con «Equity del rango completo», la equity de "
                + "un jugador con rango se calcula sobre todas sus manos posibles, no solo la repartida.");
        notaRango.setWrapText(true);
        notaRango.setMaxWidth(290);
        notaRango.setStyle("-fx-text-fill: #8FA89C; -fx-font-size: 10px;");

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
                tituloRango, ayudaRango, etiquetaRanking, rankingCombo, leyenda,
                repartirEnRango, equityRango, notaRango, limpiarRangos,
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
            if (!asiento.tieneRango()) {
                asiento.mostrarRango(null, "", null);
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

        // Hasta que termine el reparto no se indica si cada mano está dentro de su rango
        manosRepartidas = null;
        actualizarRangos();

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

        // Ya están todas las cartas repartidas: comprobamos quién está dentro de su rango
        manosRepartidas = new ArrayList<>();
        for (int i = 0; i < MesaEquityVista.NUM_ASIENTOS; i++) {
            List<Carta> mano = reparto.manos.get(i);
            manosRepartidas.add(mano == null ? null : new ArrayList<>(mano));
        }
        actualizarRangos();

        final List<Rango> rangos = new ArrayList<>();
        for (AsientoVista asiento : reparto.activos) {
            asiento.setEquity("...", null);

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

            Rango rangoEquity = reparto.rangosEquity[asiento.getIndice()];
            if (rangoEquity != null) {
                detalle = "Equity del rango completo (" + rangoEquity.size() + " combos).  " + detalle;
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
