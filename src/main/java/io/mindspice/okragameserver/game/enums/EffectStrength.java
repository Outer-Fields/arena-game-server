package io.mindspice.okragameserver.game.enums;

public enum EffectStrength {
    LIGHT(0),
    MODERATE(1),
    HEAVY(2);

    public final int amount;

    EffectStrength(int amount) {
        this.amount = amount;
    }
}
