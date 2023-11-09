package io.mindspice.okragameserver.game.gameroom.gameutil;

import io.mindspice.okragameserver.game.enums.EffectType;
import io.mindspice.okragameserver.game.gameroom.effect.ActiveEffect;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import io.mindspice.okragameserver.util.Log;

import java.util.Collections;


public class CureLogic {

    public static void cureEffect(Pawn pawn, EffectType effect, double amount) {
        var tAmount = amount;
        for (var activeEffect : pawn.getStatusEffects().stream().filter(af -> af.getType() == effect)
                .filter(e -> e.getEffectType().isNegative)
                .filter(ActiveEffect::isCurable)
                .sorted(Collections.reverseOrder()).toList()) {
            if (tAmount <= 0) { break; }
            if (activeEffect.getAmount() < tAmount) {
                tAmount -= activeEffect.getAmount();
                pawn.removeStatusEffect(activeEffect);
                Log.SERVER.debug(CureLogic.class, "Cured Effect:" + activeEffect.getType());
            } else {
                activeEffect.subtractAmount(tAmount);
                Log.SERVER.debug(CureLogic.class, "Reduced Effect:" + activeEffect.getType() + " Amount: " + tAmount);
                tAmount = 0;
            }
        }
    }

    public static void cureAny(Pawn pawn, double amount) {
        var tAmount = amount;
        for (var activeEffect : pawn.getStatusEffects().stream()
                .filter(e -> e.getEffectType().isNegative)
                .filter(ActiveEffect::isCurable).toList()) {
            if (tAmount <= 0) { break; }
            if (activeEffect.getAmount() < tAmount) {
                tAmount -= activeEffect.getAmount();
                pawn.removeStatusEffect(activeEffect);
                Log.SERVER.debug(CureLogic.class, "Cured Effect:" + activeEffect.getType());
            } else {
                activeEffect.subtractAmount(tAmount);
                tAmount = 0;
                Log.SERVER.debug(CureLogic.class, "Reduced Effect:" + activeEffect.getType() + " Amount: " + tAmount);

            }
        }
    }
}
