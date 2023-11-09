package io.mindspice.okragameserver.schema.websocket.outgoing.game;

import io.mindspice.okragameserver.game.enums.EffectLength;
import io.mindspice.okragameserver.game.enums.EffectStrength;
import io.mindspice.okragameserver.game.enums.EffectType;

public class EffectStats {
    public final EffectType effect;
    public final EffectStrength strength;
    public final EffectLength length;
    public final boolean curable;

    public EffectStats(EffectType effect, int amount, long rollOff, boolean curable) {
        this.effect = effect;
        this.curable = curable;

        // TODO this is bugged for anything not a modifier it will always be light
        var sAmount = amount * effect.scalar;
        if ( sAmount <= 100) {
            strength = EffectStrength.LIGHT;
        } else if (sAmount <= 200) {
            strength = EffectStrength.MODERATE;
        } else {
            strength = EffectStrength.HEAVY;
        }

        if (rollOff < 3) {
            length = EffectLength.SHORT;
        } else if ( rollOff < 5) {
            length = EffectLength.AVERAGE;
        } else {
            length = EffectLength.LONG;
        }
    }

}
