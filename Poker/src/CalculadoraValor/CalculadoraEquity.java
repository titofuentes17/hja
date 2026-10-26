package CalculadoraValor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Calcula la equity de varios jugadores.
//
// Cada jugador tiene un Rango (una mano concreta es un rango de 1 combo).
//  - Si todas las manos son conocidas, la equity es EXACTA: se recorren todas
//    las formas posibles de completar el board (como "Enumerate All" de PokerStove).
//  - Si algún jugador tiene un rango de varias manos, se estima por simulación
//    Monte Carlo: en cada iteración se elige una mano de cada rango sin repetir
//    cartas y se completa el board con cartas aleatorias.
// En los dos casos el bote se reparte entre los ganadores (los empates reparten
// a partes iguales).
//
// Omaha (calcularOmaha): cada jugador tiene 4 cartas y su mano se forma con
// exactamente 2 cartas propias y exactamente 3 comunes.
public final class CalculadoraEquity {

    public static final class Resultado {
        public final double[] equity;     // 0..1, incluye la parte proporcional de los empates
        public final double[] victorias;  // probabilidad de ganar en solitario
        public final double[] empates;    // probabilidad de empatar
        public final int iteraciones;     // boards evaluados

        Resultado(double[] equity, double[] victorias, double[] empates, int iteraciones) {
            this.equity = equity;
            this.victorias = victorias;
            this.empates = empates;
            this.iteraciones = iteraciones;
        }
    }

    private CalculadoraEquity() {}

    // rangos: un Rango por jugador | board: 0, 3, 4 o 5 cartas ya conocidas
    public static Resultado calcular(List<Rango> rangos, List<Carta> board, int iteraciones, Random rnd) {
        if (rangos.size() < 2) {
            throw new IllegalArgumentException("Hacen falta al menos dos jugadores");
        }
        return calcular(rangos, board, List.of(), iteraciones, rnd);
    }

    // Los retirados no compiten por el bote, pero sus cartas bloquean rangos y board.
    // Esta sobrecarga también admite el único superviviente de una mano con Fold.
    public static Resultado calcular(List<Rango> rangos, List<Carta> board,
            List<Carta> cartasRetiradas, int iteraciones, Random rnd) {
        int n = rangos.size();
        if (n < 1 || n > 10 || iteraciones <= 0 || rnd == null) {
            throw new IllegalArgumentException("Jugadores, iteraciones o generador aleatorio no válidos");
        }
        int tamanoBoard = board.size();
        if (tamanoBoard != 0 && tamanoBoard != 3 && tamanoBoard != 4 && tamanoBoard != 5) {
            throw new IllegalArgumentException("El board debe tener 0, 3, 4 o 5 cartas");
        }
        java.util.Set<Integer> conocidasIds = new java.util.HashSet<>();
        ManoEquity.validar(board, conocidasIds);
        ManoEquity.validar(cartasRetiradas, conocidasIds);
        if (conocidasIds.size() + 2 * n + (5 - tamanoBoard) > 52) {
            throw new IllegalArgumentException("No quedan cartas suficientes para completar el reparto");
        }

        // Cartas bloqueadas: comunitarias visibles y manos retiradas conocidas.
        long mascaraBloqueadas = 0;
        int[] boardIds = new int[5];
        for (int i = 0; i < board.size(); i++) {
            boardIds[i] = Cartas.id(board.get(i));
            mascaraBloqueadas |= 1L << boardIds[i];
        }
        for (Carta carta : cartasRetiradas) mascaraBloqueadas |= 1L << Cartas.id(carta);
        int conocidas = board.size();

        // Manos posibles de cada jugador sin ninguna de las cartas bloqueadas.
        List<List<int[]>> posibles = new ArrayList<>();
        boolean todosFijos = true;
        for (int i = 0; i < n; i++) {
            List<int[]> validas = new ArrayList<>();
            for (int[] combo : rangos.get(i).getCombos()) {
                if (((mascaraBloqueadas >> combo[0]) & 1) == 0 && ((mascaraBloqueadas >> combo[1]) & 1) == 0) {
                    validas.add(combo);
                }
            }
            if (validas.isEmpty()) {
                throw new IllegalArgumentException(
                        "Las cartas del jugador " + (i + 1) + " no son compatibles con el board o las cartas retiradas");
            }
            if (validas.size() > 1) {
                todosFijos = false;
            }
            posibles.add(validas);
        }

        if (n == 1) {
            return new Resultado(new double[] {1}, new double[] {1}, new double[] {0}, 1);
        }

        if (todosFijos) {
            return enumerar(posibles, boardIds, conocidas, mascaraBloqueadas);
        }

        // Orden de asignación: primero los que tienen menos opciones
        Integer[] orden = new Integer[n];
        for (int i = 0; i < n; i++) orden[i] = i;
        java.util.Arrays.sort(orden, (x, y) -> Integer.compare(posibles.get(x).size(), posibles.get(y).size()));

        long maxIntentos = (long) iteraciones * 30;

        double[] equity = new double[n];
        double[] victorias = new double[n];
        double[] empates = new double[n];

        int[][] manos = new int[n][2];
        int[] siete = new int[7];
        int[] puntos = new int[n];
        int[] b = new int[5];

        int validas = 0;
        long intentos = 0;

        while (validas < iteraciones && intentos < maxIntentos) {
            if (Thread.currentThread().isInterrupted()) {
                throw new java.util.concurrent.CancellationException("Cálculo cancelado");
            }
            intentos++;

            // 1. Elegimos la mano de cada jugador sin repetir cartas
            long usadas = mascaraBloqueadas;
            boolean ok = true;
            for (int idx : orden) {
                List<int[]> lista = posibles.get(idx);
                int[] combo = lista.size() == 1 ? lista.get(0) : lista.get(rnd.nextInt(lista.size()));
                long bits = (1L << combo[0]) | (1L << combo[1]);
                if ((usadas & bits) != 0) {
                    ok = false;
                    break;
                }
                usadas |= bits;
                manos[idx][0] = combo[0];
                manos[idx][1] = combo[1];
            }
            if (!ok) {
                continue;
            }

            // 2. Completamos el board
            for (int i = 0; i < conocidas; i++) {
                b[i] = boardIds[i];
            }
            for (int i = conocidas; i < 5; i++) {
                int carta;
                do {
                    carta = rnd.nextInt(52);
                } while (((usadas >> carta) & 1) != 0);
                usadas |= 1L << carta;
                b[i] = carta;
            }

            // 3. Evaluamos a cada jugador y repartimos el bote
            repartirBote(manos, b, siete, puntos, equity, victorias, empates, false);
            validas++;
        }

        if (validas == 0) {
            throw new IllegalArgumentException(
                    "No se pudo repartir: las manos o rangos elegidos comparten demasiadas cartas");
        }

        return normalizar(equity, victorias, empates, validas);
    }


    // =========================================================
    // OMAHA
    // =========================================================

    // manos: 4 cartas por jugador | board: 0, 3, 4 o 5 cartas ya conocidas.
    // Las cartas de los retirados no compiten, pero no pueden salir en el board.
    public static Resultado calcularOmaha(List<List<Carta>> manos, List<Carta> board,
            List<Carta> cartasRetiradas) {
        int n = manos.size();
        if (n < 1 || n > 10) {
            throw new IllegalArgumentException("Número de jugadores no válido");
        }
        int tamanoBoard = board.size();
        if (tamanoBoard != 0 && tamanoBoard != 3 && tamanoBoard != 4 && tamanoBoard != 5) {
            throw new IllegalArgumentException("El board debe tener 0, 3, 4 o 5 cartas");
        }

        // Comprueba que no haya cartas repetidas entre manos, board y retirados
        java.util.Set<Integer> conocidasIds = new java.util.HashSet<>();
        ManoEquity.validar(board, conocidasIds);
        ManoEquity.validar(cartasRetiradas, conocidasIds);
        for (List<Carta> mano : manos) {
            if (mano.size() != 4) {
                throw new IllegalArgumentException("En Omaha cada jugador tiene 4 cartas");
            }
            ManoEquity.validar(mano, conocidasIds);
        }

        if (n == 1) {
            return new Resultado(new double[] {1}, new double[] {1}, new double[] {0}, 1);
        }

        long usadas = 0;
        int[] boardIds = new int[5];
        for (int i = 0; i < tamanoBoard; i++) {
            boardIds[i] = Cartas.id(board.get(i));
            usadas |= 1L << boardIds[i];
        }
        for (Carta carta : cartasRetiradas) usadas |= 1L << Cartas.id(carta);

        int[][] ids = new int[n][4];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 4; j++) {
                ids[i][j] = Cartas.id(manos.get(i).get(j));
                usadas |= 1L << ids[i][j];
            }
        }
        return enumerarManos(ids, boardIds, tamanoBoard, usadas, true);
    }


    // =========================================================
    // EQUITY EXACTA (todas las manos conocidas)
    // =========================================================

    // Hold'em: comprueba que las manos no compartan cartas y enumera
    private static Resultado enumerar(List<List<int[]>> posibles, int[] boardIds, int conocidas,
            long mascaraBloqueadas) {
        int n = posibles.size();
        int[][] manos = new int[n][];
        long usadas = mascaraBloqueadas;
        for (int i = 0; i < n; i++) {
            manos[i] = posibles.get(i).get(0);
            long bits = (1L << manos[i][0]) | (1L << manos[i][1]);
            if ((usadas & bits) != 0) {
                throw new IllegalArgumentException("Hay cartas repetidas entre las manos de los jugadores");
            }
            usadas |= bits;
        }
        return enumerarManos(manos, boardIds, conocidas, usadas, false);
    }

    // Recorre todas las formas de completar el board con las cartas que quedan
    // en el mazo ('usadas' son las cartas que ya no pueden salir).
    // Con 6 jugadores de Hold'em: river 1 board, turn 36, flop 666, preflop 658.008.
    private static Resultado enumerarManos(int[][] manos, int[] boardIds, int conocidas,
            long usadas, boolean omaha) {
        int[] mazo = new int[52 - Long.bitCount(usadas)];
        int k = 0;
        for (int carta = 0; carta < 52; carta++) {
            if (((usadas >> carta) & 1) == 0) {
                mazo[k++] = carta;
            }
        }

        Enumeracion e = new Enumeracion(manos, mazo, boardIds, omaha);
        e.recorrer(conocidas, 0);
        return normalizar(e.equity, e.victorias, e.empates, e.casos);
    }

    private static final class Enumeracion {
        final int[][] manos;
        final int[] mazo;
        final boolean omaha;
        final int[] b = new int[5];
        final int[] siete = new int[7];
        final int[] puntos;
        final double[] equity;
        final double[] victorias;
        final double[] empates;
        int casos;

        Enumeracion(int[][] manos, int[] mazo, int[] boardIds, boolean omaha) {
            this.manos = manos;
            this.mazo = mazo;
            this.omaha = omaha;
            System.arraycopy(boardIds, 0, b, 0, 5);
            int n = manos.length;
            puntos = new int[n];
            equity = new double[n];
            victorias = new double[n];
            empates = new double[n];
        }

        // Rellena la posición 'pos' del board con cada carta del mazo a partir de 'desde'
        void recorrer(int pos, int desde) {
            if (pos == 5) {
                repartirBote(manos, b, siete, puntos, equity, victorias, empates, omaha);
                casos++;
                return;
            }
            for (int i = desde; i < mazo.length; i++) {
                if (pos < 4 && Thread.currentThread().isInterrupted()) {
                    throw new java.util.concurrent.CancellationException("Cálculo cancelado");
                }
                b[pos] = mazo[i];
                recorrer(pos + 1, i + 1);
            }
        }
    }


    // =========================================================
    // COMÚN
    // =========================================================

    // Evalúa a cada jugador con el board 'b' y reparte el bote entre los ganadores
    private static void repartirBote(int[][] manos, int[] b, int[] buffer, int[] puntos,
            double[] equity, double[] victorias, double[] empates, boolean omaha) {
        int n = manos.length;
        int mejor = -1;
        int ganadores = 0;
        for (int i = 0; i < n; i++) {
            puntos[i] = puntuar(manos[i], b, buffer, omaha);

            if (puntos[i] > mejor) {
                mejor = puntos[i];
                ganadores = 1;
            } else if (puntos[i] == mejor) {
                ganadores++;
            }
        }

        for (int i = 0; i < n; i++) {
            if (puntos[i] == mejor) {
                equity[i] += 1.0 / ganadores;
                if (ganadores == 1) victorias[i] += 1;
                else empates[i] += 1;
            }
        }
    }

    // Las 10 formas de elegir 3 cartas de las 5 del board (para Omaha)
    private static final int[][] TRIOS_BOARD = {
        {0, 1, 2}, {0, 1, 3}, {0, 1, 4}, {0, 2, 3}, {0, 2, 4},
        {0, 3, 4}, {1, 2, 3}, {1, 2, 4}, {1, 3, 4}, {2, 3, 4}
    };

    // Puntuación de la mejor mano de un jugador con el board 'b' (5 cartas).
    //   Hold'em: la mejor mano de sus 7 cartas.
    //   Omaha:   la mejor de las 6 x 10 = 60 manos con exactamente 2 propias y 3 comunes.
    private static int puntuar(int[] mano, int[] b, int[] buffer, boolean omaha) {
        if (!omaha) {
            buffer[0] = mano[0];
            buffer[1] = mano[1];
            System.arraycopy(b, 0, buffer, 2, 5);
            return EvaluadorRapido.evaluar(buffer, 7);
        }

        int mejor = -1;
        for (int i = 0; i < mano.length; i++) {
            for (int j = i + 1; j < mano.length; j++) {
                buffer[0] = mano[i];
                buffer[1] = mano[j];
                for (int[] trio : TRIOS_BOARD) {
                    buffer[2] = b[trio[0]];
                    buffer[3] = b[trio[1]];
                    buffer[4] = b[trio[2]];
                    mejor = Math.max(mejor, EvaluadorRapido.evaluar(buffer, 5));
                }
            }
        }
        return mejor;
    }

    private static Resultado normalizar(double[] equity, double[] victorias, double[] empates, int casos) {
        for (int i = 0; i < equity.length; i++) {
            equity[i] /= casos;
            victorias[i] /= casos;
            empates[i] /= casos;
        }
        return new Resultado(equity, victorias, empates, casos);
    }
}
