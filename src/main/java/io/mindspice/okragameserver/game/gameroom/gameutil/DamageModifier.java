package io.mindspice.okragameserver.game.gameroom.gameutil;


import io.mindspice.okragameserver.game.enums.ActionFlag;
import io.mindspice.okragameserver.game.enums.ActionType;
import io.mindspice.okragameserver.game.enums.StatType;

import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import io.mindspice.okragameserver.game.gameroom.state.PawnInterimState;

import java.util.Map;

import static io.mindspice.okragameserver.game.enums.StatType.*;

public class DamageModifier {

    public static int posStatScale(Pawn attackPawn, int damage) {
        return (int) Math.round(damage * LuckModifier.luckMod(attackPawn.getStat(StatType.LP)));
    }

    public static void defendDamage(PawnInterimState defendPawn, ActionType actionType, Map<StatType, Integer> damageMap) {
        switch (actionType) {
            case MELEE -> defendDamageMelee(defendPawn, damageMap);
            case MAGIC -> defendDamageMagic(defendPawn, damageMap);
            case RANGED -> defendDamageRanged(defendPawn, damageMap);
        }
    }

    private static void defendDamageMelee(PawnInterimState defendPawn, Map<StatType, Integer> damageMap) {
        int currVal = damageMap.get(HP);
        int defendDamage = (int) Math.round((double) (defendPawn.getPawn().getStat(DP) / 8)
                * LuckModifier.luckMod(defendPawn.getPawn().getStat(StatType.LP)));
        damageMap.put(HP, Math.max((currVal - defendDamage), (damageMap.get(HP) / 2)));
    }

    private static void defendDamageMagic(PawnInterimState defendPawn, Map<StatType, Integer> damageMap) {
        int currVal = damageMap.get(HP);
        int defendDamage = (int) Math.round((double) (defendPawn.getPawn().getStat(MP) / 8)
                * LuckModifier.luckMod(defendPawn.getPawn().getStat(StatType.LP)));
        damageMap.put(HP, Math.max((currVal - defendDamage), (damageMap.get(HP) / 2)));
    }

    private static void defendDamageRanged(PawnInterimState defendPawn, Map<StatType, Integer> damageMap) {
        int currVal = damageMap.get(HP);
        if (defendPawn.getPawn().getStat(DP) <= 250) {
            defendPawn.addFlag(ActionFlag.DEFENSE_FAILED);
            return;
        }
        int defendDamage = (int) Math.round((double) (defendPawn.getPawn().getStat(DP) / 8)
                * LuckModifier.luckMod(defendPawn.getPawn().getStat(StatType.LP)));
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
