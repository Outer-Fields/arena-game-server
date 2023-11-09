package io.mindspice.okragameserver.game.gameroom.gameutil;


import io.mindspice.okragameserver.game.enums.ActionType;
import io.mindspice.okragameserver.game.enums.StatType;

import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;


import java.util.Map;

import static io.mindspice.okragameserver.game.enums.StatType.*;

public class DamageModifier {

    public static int posStatScale(Pawn attackPawn, int damage) {
        return (int) Math.round(damage * LuckModifier.luckMod(attackPawn.getStat(StatType.LP)));
    }

    public static void defendDamage(Pawn defendPawn, ActionType actionType, Map<StatType, Integer> damageMap) {
        switch (actionType) {
            case MELEE -> defendDamageMelee(defendPawn, damageMap);
            case MAGIC -> defendDamageMagic(defendPawn, damageMap);
            case RANGED -> defendDamageRanged(defendPawn, damageMap);
        }
    }

    private static void defendDamageMelee(Pawn defendPawn, Map<StatType, Integer> damageMap) {
        int currVal = damageMap.get(HP);
        int defendDamage = (int) Math.round((double) (defendPawn.getStat(DP) / 8)
                * LuckModifier.luckMod(defendPawn.getStat(StatType.LP)));
        damageMap.put(HP, Math.max((currVal - defendDamage), (damageMap.get(HP) / 2)));
    }

    private static void defendDamageMagic(Pawn defendPawn, Map<StatType, Integer> damageMap) {
        int currVal = damageMap.get(HP);
        int defendDamage = (int) Math.round((double) (defendPawn.getStat(MP) / 8)
                * LuckModifier.luckMod(defendPawn.getStat(StatType.LP)));
        damageMap.put(HP, Math.max((currVal - defendDamage), (damageMap.get(HP) / 2)));
    }

    private static void defendDamageRanged(Pawn defendPawn, Map<StatType, Integer> damageMap) {
        int currVal = damageMap.get(HP);
        if (defendPawn.getStat(DP) <= 250) {
            return;
        }
        int defendDamage = (int) Math.round((double) (defendPawn.getStat(DP) / 8)
                * LuckModifier.luckMod(defendPawn.getStat(StatType.LP)));
        damageMap.put(HP, Math.max((currVal - defendDamage), (damageMap.get(HP) / 2)));
    }

    public static int defendDamageStat(Pawn defendPawn, int statDamage) {
        int defendDamage = (int) Math.round(statDamage * LuckModifier.inverseLuckMod(defendPawn.getStat(StatType.LP)));
        return (Math.max(defendDamage, 0));
    }

    // returns damage from a poison "tick" with luck modifier scaling
    public static int poisonDamage(Pawn poisonedPawn, int poisonDamage) {
        return (int) Math.round(poisonDamage
                * LuckModifier.inverseLuckMod(poisonedPawn.getStat(StatType.LP)));
    }


}
