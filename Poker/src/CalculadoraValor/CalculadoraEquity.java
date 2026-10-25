package CalculadoraValor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Calcula la equity de varios jugadores por simulación Monte Carlo.
//
// Cada jugador tiene un Rango (una mano concreta es un rango de 1 combo).
// En cada iteración se elige una mano de cada rango sin repetir cartas, se
// completa el board con cartas aleatorias y se reparte el bote entre los
// ganadores (los empates reparten a partes iguales).
public final class CalculadoraEquity {

    public static final class Resultado {
        public final double[] equity;     // 0..1, incluye la parte proporcional de los empates
        public final double[] victorias;  // probabilidad de ganar en solitario
        public final double[] empates;    // probabilidad de empatar
        public final int iteraciones;

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

        // Orden de asignación: primero los que tienen menos opciones
        Integer[] orden = new Integer[n];
        for (int i = 0; i < n; i++) orden[i] = i;
        java.util.Arrays.sort(orden, (x, y) -> Integer.compare(posibles.get(x).size(), posibles.get(y).size()));

        boolean determinista = todosFijos && conocidas == 5;
        int objetivo = determinista ? 1 : iteraciones;
        long maxIntentos = determinista ? 1 : (long) iteraciones * 30;

        double[] equity = new double[n];
        double[] victorias = new double[n];
        double[] empates = new double[n];

        int[][] manos = new int[n][2];
        int[] siete = new int[7];
        int[] puntos = new int[n];
        int[] b = new int[5];

        int validas = 0;
        long intentos = 0;

        while (validas < objetivo && intentos < maxIntentos) {
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

            // 3. Evaluamos a cada jugador
            int mejor = -1;
            int ganadores = 0;
            for (int i = 0; i < n; i++) {
                siete[0] = manos[i][0];
                siete[1] = manos[i][1];
                System.arraycopy(b, 0, siete, 2, 5);
                puntos[i] = EvaluadorRapido.evaluar(siete, 7);

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
            validas++;
        }

        if (validas == 0) {
            throw new IllegalArgumentException(
                    "No se pudo repartir: las manos o rangos elegidos comparten demasiadas cartas");
        }

        for (int i = 0; i < n; i++) {
            equity[i] /= validas;
            victorias[i] /= validas;
            empates[i] /= validas;
        }

        return new Resultado(equity, victorias, empates, validas);
    }
}
