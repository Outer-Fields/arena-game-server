package io.mindspice.okragameserver.schema.websocket.outgoing.lobby;

import io.mindspice.okragameserver.schema.websocket.outgoing.OutMsgType;
import io.mindspice.okragameserver.game.enums.CardDomain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public record NetOwnedCards (
    OutMsgType msg_type,
    Map<CardDomain, List<String>> owned_cards
    ) {
    public NetOwnedCards(Map<CardDomain, List<String>> owned_cards) {
        this(OutMsgType.OWNED_CARDS, owned_cards);
    }
}
