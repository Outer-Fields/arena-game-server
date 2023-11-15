package io.mindspice.okragameserver.schema.websocket.outgoing.game;

import io.mindspice.okragameserver.game.enums.Animation;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.enums.PlayerAction;
import io.mindspice.okragameserver.game.enums.StatType;
import io.mindspice.okragameserver.schema.websocket.outgoing.NetMsg;
import io.mindspice.okragameserver.schema.websocket.outgoing.OutMsgType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class NetTurnResponse extends NetMsg {
    public boolean is_invalid = false;
    public boolean is_failed = false;
    public PawnIndex action_pawn;
    public List<PawnResponse> affected_pawns_player;
    public List<PawnResponse> affected_pawns_enemy;
    public Map<StatType, Integer> cost;
    public Animation animation;
    public PlayerAction card_slot;
    public String invalid_msg;


    public NetTurnResponse(boolean isPlayer) {
        super(OutMsgType.TURN_RESPONSE, isPlayer);
    }
}
