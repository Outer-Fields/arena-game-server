package io.mindspice.okragameserver.game.gameroom.action;

import io.mindspice.okragameserver.game.enums.ActionType;
import io.mindspice.okragameserver.game.enums.EffectType;
import io.mindspice.okragameserver.game.enums.PowerEnums;
import io.mindspice.okragameserver.game.gameroom.gameutil.LuckModifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class ActivePowerTest {

    @Test
    void shieldTest() {
        // card can be null it is only used for removing powers based off their originating card
        var shieldMelee = new ActivePower(null, PowerEnums.PowerType.SHIELD_MELEE, 1, 0.05);
        var shieldMagic = new ActivePower(null, PowerEnums.PowerType.SHIELD_MAGIC, 1, 0.05);
        var shieldRanged = new ActivePower(null, PowerEnums.PowerType.SHIELD_RANGED, 1, 0.05);

        // Test that powers only shield the right type
        // MELEE
        assertEquals(0.95, shieldMelee.getActionDefense(ActionType.MELEE, 0).get(PowerEnums.PowerReturn.SHIELD));
        assertFalse(shieldMelee.getActionDefense(ActionType.RANGED, 0).containsKey(PowerEnums.PowerReturn.SHIELD));
        assertFalse(shieldMelee.getActionDefense(ActionType.MAGIC, 0).containsKey(PowerEnums.PowerReturn.SHIELD));
        // MAGIC
        assertEquals(0.95, shieldMagic.getActionDefense(ActionType.MAGIC, 0).get(PowerEnums.PowerReturn.SHIELD));
        assertFalse(shieldMagic.getActionDefense(ActionType.RANGED, 0).containsKey(PowerEnums.PowerReturn.SHIELD));
        assertFalse(shieldMagic.getActionDefense(ActionType.MELEE, 0).containsKey(PowerEnums.PowerReturn.SHIELD));
        // RANGED
        assertEquals(0.95, shieldRanged.getActionDefense(ActionType.RANGED, 0).get(PowerEnums.PowerReturn.SHIELD));
        assertFalse(shieldRanged.getActionDefense(ActionType.MAGIC, 0).containsKey(PowerEnums.PowerReturn.SHIELD));
        assertFalse(shieldRanged.getActionDefense(ActionType.MELEE, 0).containsKey(PowerEnums.PowerReturn.SHIELD));

    }

    @Test
    void resistTest() {
        var resistMagic = new ActivePower(null, PowerEnums.PowerType.RESIST_MAGIC, 0.5, 1);
        var resistPoison = new ActivePower(null, PowerEnums.PowerType.RESIST_POISON, 0.5, 1);
        var resistParalysis = new ActivePower(null, PowerEnums.PowerType.RESIST_PARALYSIS, 0.5, 1);
        var resistSleep = new ActivePower(null, PowerEnums.PowerType.RESIST_SLEEP, 0.5, 1);
        var resistDeBuff = new ActivePower(null, PowerEnums.PowerType.RESIST_DEBUFF, 0.5, 1);
        var resistConfusion = new ActivePower(null, PowerEnums.PowerType.RESIST_CONFUSION, 0.5, 1);
        var resistInsight = new ActivePower(null, PowerEnums.PowerType.RESIST_INSIGHT, 0.5, 1);
        var reflection = new ActivePower(null, PowerEnums.PowerType.REFLECTION, 0.5, 1);

        var debuffs = List.of(EffectType.DE_CORE, EffectType.DE_MEDITATE, EffectType.DE_INVIGORATE, EffectType.DE_FORTIFY);
        var insights = List.of(EffectType.INSIGHT_HAND, EffectType.INSIGHT_STATUS, EffectType.INSIGHT_ALL_DECKS,
                               EffectType.INSIGHT_ACTION_DECK, EffectType.INSIGHT_ABILITY_DECK);

        for (int j = 0; j <= 20; ++j) {
            var bounds = LuckModifier.luckModCalc(j);
            int low = (int) (0.5 * 100000 * (1 + bounds[0]));
            int high = (int) (0.5 * 100000 * (1 + bounds[1]));

            var rMagicCount = 0;
            var rPoisonCount = 0;
            var rParalysisCount = 0;
            var rSleepCount = 0;
            var rDeBuffCount = 0;
            var rConfusionCount = 0;
            var rInsightCount = 0;
            var rReflectCount = 0;
            var rReflectCount2 = 0;

            for (int i = 0; i < 100000; ++i) {
                rMagicCount += (resistMagic.getActionDefense(ActionType.MAGIC, j).containsKey(PowerEnums.PowerReturn.RESIST) ? 1 : 0);
                rPoisonCount += (resistPoison.getEffectDefense(EffectType.POISON, j).containsKey(PowerEnums.PowerReturn.RESIST) ? 1 : 0);
                rParalysisCount += (resistParalysis.getEffectDefense(EffectType.PARALYSIS, j).containsKey(PowerEnums.PowerReturn.RESIST) ? 1 : 0);
                rSleepCount += (resistSleep.getEffectDefense(EffectType.SLEEP, j).containsKey(PowerEnums.PowerReturn.RESIST) ? 1 : 0);
                rDeBuffCount += (resistDeBuff.getEffectDefense(debuffs.get(i % 4), j).containsKey(PowerEnums.PowerReturn.RESIST) ? 1 : 0);
                rConfusionCount += (resistConfusion.getEffectDefense(EffectType.CONFUSION, j).containsKey(PowerEnums.PowerReturn.RESIST) ? 1 : 0);
                rInsightCount += (resistInsight.getEffectDefense(insights.get(i % 5), j).containsKey(PowerEnums.PowerReturn.RESIST) ? 1 : 0);
                rReflectCount += (reflection.getEffectDefense(EffectType.DE_FORTIFY, j).containsKey(PowerEnums.PowerReturn.REFLECT) ? 1 : 0);
                rReflectCount2 += (reflection.getActionDefense(ActionType.MAGIC, j).containsKey(PowerEnums.PowerReturn.REFLECT) ? 1 : 0);
            }
            System.out.println("Magic Resist: " + low + "|" + rMagicCount + "|" + high);
            assertTrue(rMagicCount >= low && rMagicCount <= high);

            System.out.println("Poison Resist: " + low + "|" + rPoisonCount + "|" + high);
            assertTrue(rPoisonCount >= low && rPoisonCount <= high);

            System.out.println("Paralysis Resist: " + low + "|" + rParalysisCount + "|" + high);
            assertTrue(rParalysisCount >= low && rParalysisCount <= high);

            System.out.println("Sleep Resist: " + low + "|" + rSleepCount + "|" + high);
            assertTrue(rSleepCount >= low && rSleepCount <= high);

            System.out.println("De-Buff Resist: " + low + "|" + rDeBuffCount + "|" + high);
            assertTrue(rDeBuffCount >= low && rDeBuffCount <= high);

            System.out.println("Confusion Resist: " + low + "|" + rConfusionCount + "|" + high);
            assertTrue(rConfusionCount >= low && rConfusionCount <= high);

            System.out.println("Insight Resist: " + low + "|" + rInsightCount + "|" + high);
            assertTrue(rInsightCount >= low && rInsightCount <= high);

            System.out.println("Reflect 1: " + low + "|" + rReflectCount + "|" + high);
            assertTrue(rReflectCount >= low && rReflectCount <= high);

            System.out.println("Reflect 2: " + low + "|" + rReflectCount2 + "|" + high);
            assertTrue(rReflectCount2 >= low && rReflectCount2 <= high);

        }
    }

    @Test
    void offenseTest() {
        var buffMelee = new ActivePower(null, PowerEnums.PowerType.BUFF_MELEE, 0.5, 0.5);
        var buffMagic = new ActivePower(null, PowerEnums.PowerType.BUFF_MAGIC, 0.5, 0.5);
        var buffRanged = new ActivePower(null, PowerEnums.PowerType.BUFF_RANGED, 0.5, 0.5);
        var doubled = new ActivePower(null, PowerEnums.PowerType.DOUBLE, 0.5, 0.5);

        assertEquals(0.5, buffMelee.getActionOffense(ActionType.MELEE, 1).get(PowerEnums.PowerReturn.BUFF));
        assertNotEquals(0.5, buffMelee.getActionOffense(ActionType.MAGIC, 1).get(PowerEnums.PowerReturn.BUFF));
        assertNotEquals(0.5, buffMelee.getActionOffense(ActionType.RANGED, 1).get(PowerEnums.PowerReturn.BUFF));

        assertEquals(0.5, buffMagic.getActionOffense(ActionType.MAGIC, 1).get(PowerEnums.PowerReturn.BUFF));
        assertNotEquals(0.5, buffMagic.getActionOffense(ActionType.MELEE, 1).get(PowerEnums.PowerReturn.BUFF));
        assertNotEquals(0.5, buffMagic.getActionOffense(ActionType.RANGED, 1).get(PowerEnums.PowerReturn.BUFF));

        assertEquals(0.5, buffRanged.getActionOffense(ActionType.RANGED, 1).get(PowerEnums.PowerReturn.BUFF));
        assertNotEquals(0.5, buffRanged.getActionOffense(ActionType.MELEE, 1).get(PowerEnums.PowerReturn.BUFF));
        assertNotEquals(0.5, buffRanged.getActionOffense(ActionType.MAGIC, 1).get(PowerEnums.PowerReturn.BUFF));



        for (int j = 0; j <= 20; ++j) {
            var bounds = LuckModifier.luckModCalc(j);
            int low = (int) (0.5 * 300000 * (1 + bounds[0]));
            int high = (int) (0.5 * 300000 * (1 + bounds[1]));
            var doubleCount = 0; // Resetting the count for each value of luck

            for (int i = 0; i < 100000; ++i) {
                doubleCount += (doubled.getActionOffense(ActionType.MELEE, j).containsKey(PowerEnums.PowerReturn.DOUBLE) ? 1 : 0); // Using j instead of 1
                doubleCount += (doubled.getActionOffense(ActionType.MAGIC, j).containsKey(PowerEnums.PowerReturn.DOUBLE) ? 1 : 0); // Using j instead of 1
                doubleCount += (doubled.getActionOffense(ActionType.RANGED, j).containsKey(PowerEnums.PowerReturn.DOUBLE) ? 1 : 0); // Using j instead of 1
            }

            System.out.println("Double: " + low + "|" + doubleCount + "|" + high);
            assertTrue(doubleCount >= low && doubleCount <= high);
        }



    }


}


