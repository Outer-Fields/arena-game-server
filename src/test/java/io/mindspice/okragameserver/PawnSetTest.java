package io.mindspice.okragameserver;

import io.mindspice.okragameserver.game.cards.AbilityCard;
import io.mindspice.okragameserver.game.cards.ActionCard;
import io.mindspice.okragameserver.game.cards.PowerCard;
import io.mindspice.okragameserver.schema.PawnSet;
import org.junit.jupiter.api.Test;


public class PawnSetTest {


    @Test
    void testLimits() {
        for(int i =0; i < 2000; ++i) {
            PawnSet ps = PawnSet.getRandomPawnSet2();
            for(var lo : ps.pawnLoadouts()) {
                System.out.println(lo.actionDeck().stream().mapToInt(ActionCard::getLevel).summaryStatistics().getAverage());
                System.out.println(lo.abilityDeck().stream().mapToInt(AbilityCard::getLevel).summaryStatistics().getAverage());
                System.out.println(lo.powerDeck().stream().mapToInt(PowerCard::getLevel).summaryStatistics().getAverage());
            }
        }
    }
}
