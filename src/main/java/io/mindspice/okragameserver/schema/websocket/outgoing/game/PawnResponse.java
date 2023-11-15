package io.mindspice.okragameserver.schema.websocket.outgoing.game;

import io.mindspice.okragameserver.game.enums.ActionFlag;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.enums.StatType;
import io.mindspice.okragameserver.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class PawnResponse {
    public PawnIndex pawn;
    public Set<ActionFlag> action_flags;
    public Map<StatType, Integer> stat_update;
    public List<EffectStats> effect_update;
    public boolean is_dead;
    public int multi;

    public PawnResponse(PawnIndex pawn, List<ActionFlag> actionFlags, Map<StatType, Integer> statsUpdate,
            List<EffectStats> effectUpdate, boolean isDead) {
        this.pawn = pawn;
        this.stat_update = statsUpdate;
        this.effect_update = effectUpdate;
        this.is_dead = isDead;
        if (actionFlags == null) {
            action_flags = Set.of();
            multi = 0;
        } else {
            multi = ActionFlag.getMultiSum(actionFlags);
            this.action_flags = new HashSet<>(actionFlags);
        }
    }
}
