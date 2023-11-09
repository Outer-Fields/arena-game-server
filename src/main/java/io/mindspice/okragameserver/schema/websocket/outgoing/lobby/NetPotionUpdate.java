package io.mindspice.okragameserver.schema.websocket.outgoing.lobby;

import io.mindspice.okragameserver.game.cards.PotionCard;
import io.mindspice.okragameserver.schema.websocket.outgoing.OutMsgType;

import java.util.HashMap;

public class NetPotionUpdate {
    public final OutMsgType msg_type = OutMsgType.POTIONS_UPDATE;
    public HashMap<PotionCard, Integer> owned_potions;
}
