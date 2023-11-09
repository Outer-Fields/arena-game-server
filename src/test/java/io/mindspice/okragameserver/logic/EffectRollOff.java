package io.mindspice.okragameserver.logic;

import io.mindspice.okragameserver.game.cards.PawnCard;
import io.mindspice.okragameserver.game.cards.TalismanCard;
import io.mindspice.okragameserver.game.enums.ActionClass;
import io.mindspice.okragameserver.game.enums.EffectType;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.gameroom.effect.ActiveEffect;
import io.mindspice.okragameserver.game.gameroom.effect.Effect;
import io.mindspice.okragameserver.game.gameroom.gameutil.LuckModifier;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static io.mindspice.okragameserver.game.enums.StatType.LP;


public class EffectRollOff {
    Pawn pawn = new Pawn(PawnIndex.PAWN1,
            PawnCard.DARK_SENTINEL,
            TalismanCard.BALANCE_BEAD,
            null,
            null,
            List.of(),
            List.of(),
            List.of()
    );

    @Test
    public void testRollOff() throws InterruptedException {
        for (int i = 0; i < 100; ++i) {
            pawn.addStatusEffect(new Effect(ActionClass.SINGLE, EffectType.DE_CORE, false, 200, 0.75, true));
        }

        var iBreak = 0;
        int i = 0;
        int total = 0;
        var RList = new ArrayList<Integer>(100);
        while (iBreak != 100) {
            Iterator<ActiveEffect> effectItr = pawn.getStatusEffects().iterator();

            while (effectItr.hasNext()) {
                ActiveEffect effect = effectItr.next();
                System.out.println(LuckModifier.chanceCalc(0.5, pawn.getStat(LP)));
                effect.doEffect();
                if (!effect.update()) {
                    effectItr.remove();
                    System.out.println("Removed");
                    iBreak++;
                    RList.add(i);
                }
            }
            ++i;
        }

        System.out.println("average round" + (RList.stream().mapToInt(Integer::intValue).summaryStatistics()));

    }
}