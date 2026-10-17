package CalculadoraValor;

// Evaluador de 5 a 7 cartas pensado para las simulaciones de equity.
// Devuelve un entero: cuanto mayor, mejor es la mano.
//
//   categoria = puntuacion >> 20
//     0 carta alta, 1 pareja, 2 doble pareja, 3 trío, 4 escalera,
//     5 color, 6 full, 7 póker, 8 escalera de color (incluye la real)
//
// Cartas como enteros 0..51 (ver Cartas.id): rango = id >> 2, palo = id & 3.
public final class EvaluadorRapido {

    private EvaluadorRapido() {}

    public static int evaluar(int[] c, int n) {
        int[] cont = new int[13];
        int[] maskPalo = new int[4];
        int[] contPalo = new int[4];
        int mask = 0;

        for (int i = 0; i < n; i++) {
            int r = c[i] >> 2;
            int p = c[i] & 3;
            cont[r]++;
            contPalo[p]++;
            maskPalo[p] |= 1 << r;
            mask |= 1 << r;
        }

        // Color (y escalera de color)
        int maskColor = 0;
        for (int p = 0; p < 4; p++) {
            if (contPalo[p] >= 5) {
                maskColor = maskPalo[p];
            }
        }
        if (maskColor != 0) {
            int top = topEscalera(maskColor);
            if (top >= 0) {
                return (8 << 20) | top;
            }
        }

        // Póker, trío y parejas (se recorre de mayor a menor)
        int poker = -1;
        int trio = -1;
        int[] parejas = {-1, -1, -1};
        int numParejas = 0;

        for (int r = 12; r >= 0; r--) {
            int k = cont[r];
            if (k == 4) {
                poker = r;
            } else if (k == 3) {
                if (trio < 0) {
                    trio = r;
                } else {
                    // un segundo trío sirve como pareja para el full
                    parejas[numParejas++] = r;
                }
            } else if (k == 2) {
                parejas[numParejas++] = r;
            }
        }

        if (poker >= 0) {
            int kicker = mejorRangoExcluyendo(cont, poker, -1, 1)[0];
            return (7 << 20) | (poker << 4) | kicker;
        }

        if (trio >= 0 && numParejas > 0) {
            return (6 << 20) | (trio << 4) | parejas[0];
        }

        if (maskColor != 0) {
            int[] top5 = new int[5];
            int k = 0;
            for (int r = 12; r >= 0 && k < 5; r--) {
                if ((maskColor & (1 << r)) != 0) {
                    top5[k++] = r;
                }
            }
            return (5 << 20) | empaquetar(top5, 5);
        }

        int topEsc = topEscalera(mask);
        if (topEsc >= 0) {
            return (4 << 20) | topEsc;
        }

        if (trio >= 0) {
            int[] k = mejorRangoExcluyendo(cont, trio, -1, 2);
            return (3 << 20) | (trio << 8) | (k[0] << 4) | k[1];
        }

        if (numParejas >= 2) {
            int[] k = mejorRangoExcluyendo(cont, parejas[0], parejas[1], 1);
            return (2 << 20) | (parejas[0] << 8) | (parejas[1] << 4) | k[0];
        }

        if (numParejas == 1) {
            int[] k = mejorRangoExcluyendo(cont, parejas[0], -1, 3);
            return (1 << 20) | (parejas[0] << 12) | (k[0] << 8) | (k[1] << 4) | k[2];
        }

        int[] k = mejorRangoExcluyendo(cont, -1, -1, 5);
        return empaquetar(k, 5);
    }

    public static int categoria(int puntuacion) {
        return puntuacion >> 20;
    }

    // Devuelve el rango más alto de la escalera contenida en la máscara
    // (la rueda A-2-3-4-5 devuelve 3, el '5'), o -1 si no hay escalera.
    private static int topEscalera(int mask) {
        for (int top = 12; top >= 4; top--) {
            if (((mask >> (top - 4)) & 31) == 31) {
                return top;
            }
        }
        if ((mask & 0x100F) == 0x100F) {
            return 3;
        }
        return -1;
    }

    // Los 'cuantos' rangos más altos presentes, saltando dos rangos excluidos
    private static int[] mejorRangoExcluyendo(int[] cont, int excl1, int excl2, int cuantos) {
        int[] res = new int[Math.max(cuantos, 1)];
        int k = 0;
        for (int r = 12; r >= 0 && k < cuantos; r--) {
            if (cont[r] > 0 && r != excl1 && r != excl2) {
                res[k++] = r;
            }
        }
        return res;
    }

    private static int empaquetar(int[] rangos, int n) {
        int v = 0;
        for (int i = 0; i < n; i++) {
            v = (v << 4) | rangos[i];
        }
        return v;
    }
}
