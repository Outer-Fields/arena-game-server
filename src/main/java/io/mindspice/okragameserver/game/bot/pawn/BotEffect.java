package io.mindspice.okragameserver.game.bot.pawn;

import io.mindspice.okragameserver.game.enums.EffectType;
import io.mindspice.okragameserver.game.gameroom.effect.Effect;
import io.mindspice.okragameserver.game.gameroom.gameutil.LuckModifier;

public class BotEffect {

    public Effect effect;
    public EffectType type;
    public boolean isChance;
    public double rollOff;

    public BotEffect(Effect effect) {
        this.effect = effect;
        this.type = effect.type;
        this.isChance = effect.isRollOffChance;
        if (effect.isRollOffChance) {
            this.rollOff = effect.rollOffChance;
        } else {
            this.rollOff = effect.rollOffRounds;
        }
    }

    public boolean update() {
        if (isChance) {
            if (LuckModifier.chanceCalc(rollOff, 0)) {
                return false;
            } else {
                return true;
            }
        } else {
            --rollOff;
            return rollOff < 1;
        }
    }
}
