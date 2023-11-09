package io.mindspice.okragameserver.schema.websocket.incoming;

import io.mindspice.okragameserver.game.enums.CardPack;
import io.mindspice.okragameserver.game.cards.PotionCard;
import io.mindspice.okragameserver.schema.PawnSet;

import java.util.HashMap;
import java.util.List;

public class NetLobbyMsg {
    public InMsgType msg_type;
    public int pawn_set_num;
    public List<PotionCard> potionCards;
    public PawnSet pawn_set_data;
    public HashMap<PotionCard, Integer> potion_purchase;
    public CardPack pack_purchase;
    public String xch_address;
    public String name;
}