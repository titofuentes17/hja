package CalculadoraValor;

/* Fases de una misma mano de Hold'em. */
public enum Calle {
    PREFLOP(0), FLOP(3), TURN(4), RIVER(5);

    private final int comunitarias;

    Calle(int comunitarias) {
        this.comunitarias = comunitarias;
    }

    public int getComunitarias() {
        return comunitarias;
    }

    public Calle siguiente() {
        if (this == RIVER) throw new IllegalStateException("La mano ya está en el river.");
        return values()[ordinal() + 1];
    }
}
