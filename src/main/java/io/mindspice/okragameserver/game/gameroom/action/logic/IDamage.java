package io.mindspice.okragameserver.game.gameroom.action.logic;

import io.mindspice.okragameserver.game.enums.ActionType;
import io.mindspice.okragameserver.game.enums.SpecialAction;
import io.mindspice.okragameserver.game.enums.StatType;
import io.mindspice.okragameserver.game.gameroom.state.PawnInterimState;

import java.util.EnumMap;
import java.util.Map;


public interface IDamage {
    void doDamage(PawnInterimState player, PawnInterimState target, ActionType actionType,
                  SpecialAction special, Map<StatType, Integer> damage, boolean isSelf);
}
