package io.mindspice.okragameserver.game.gameroom.action.logic;

import io.mindspice.okragameserver.game.enums.StatType;
import io.mindspice.okragameserver.game.gameroom.action.ActionReturn;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;


public class Cost {

    public static final Cost GET = new Cost();

    private Cost() {
    }

    public boolean doCost(Pawn playerPawn, Map<StatType, Integer> cardCost, ActionReturn actionReturn) {
        for (var stat : cardCost.entrySet()) {
            if (playerPawn.getStat(stat.getKey()) < stat.getValue()) {
                return false;
            }
        }
        playerPawn.updateStats(cardCost, false);
        actionReturn.setCost(cardCost);
        return true;
    }
}
