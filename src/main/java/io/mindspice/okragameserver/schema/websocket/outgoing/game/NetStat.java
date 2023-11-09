package io.mindspice.okragameserver.schema.websocket.outgoing.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.enums.StatType;
import io.mindspice.okragameserver.schema.websocket.outgoing.NetMsg;
import io.mindspice.okragameserver.schema.websocket.outgoing.OutMsgType;

import java.util.EnumMap;
import java.util.Map;


public class NetStat extends NetMsg {
    public Map<StatType, Integer> pawn_1;
    public Map<StatType, Integer> pawn_2;
    public Map<StatType, Integer> pawn_3;

    public NetStat(boolean isPlayer) {
        super(OutMsgType.STAT_UPDATE, isPlayer);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return pawn_1 == null && pawn_2 == null & pawn_3 == null;
    }

    public void setStats(PawnIndex pawnIndex, Map<StatType, Integer> stats) {
        switch(pawnIndex) {
            case PAWN1 -> pawn_1 = stats;
            case PAWN2 -> pawn_2 = stats;
            case PAWN3 -> pawn_3 = stats;
        }
    }

    @JsonIgnore // Used to debug an issue
    public boolean validate() {
        if (isPlayer) { return true; }
        return !(pawn_1 != null && pawn_1.size() > 1
                || pawn_2 != null && pawn_2.size() > 1
                || pawn_3 != null && pawn_3.size() > 1);
    }

}
