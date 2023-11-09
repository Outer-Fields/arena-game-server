package io.mindspice.okragameserver.schema.websocket.outgoing.game;

import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.schema.websocket.outgoing.NetMsg;
import io.mindspice.okragameserver.schema.websocket.outgoing.OutMsgType;

import java.util.List;

public class NetEffect extends NetMsg {
    public List<EffectStats> pawn_1;
    public List<EffectStats> pawn_2;
    public List<EffectStats> pawn_3;

    public NetEffect(boolean isPlayer) {
        super(OutMsgType.EFFECT, isPlayer);
    }

    public boolean isEmpty() {
        return pawn_1 == null && pawn_2 == null && pawn_3 == null;
    }

    public void setEffects(PawnIndex pawnIndex, List<EffectStats> effects) {
        switch (pawnIndex) {

            case PAWN1 -> pawn_1 = effects;
            case PAWN2 -> pawn_2 = effects;
            case PAWN3 -> pawn_3 = effects;
        }
    }
}
