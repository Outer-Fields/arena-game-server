package io.mindspice.okragameserver.schema.websocket.outgoing.game;

import io.mindspice.okragameserver.game.cards.AbilityCard;
import io.mindspice.okragameserver.game.cards.ActionCard;
import io.mindspice.okragameserver.game.cards.PowerCard;
import io.mindspice.okragameserver.game.enums.EffectType;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.enums.StatType;
import io.mindspice.okragameserver.game.gameroom.effect.Insight;
import io.mindspice.okragameserver.schema.websocket.outgoing.NetMsg;
import io.mindspice.okragameserver.schema.websocket.outgoing.OutMsgType;

import java.util.EnumMap;
import java.util.List;


public class NetInsight extends NetMsg {
    public final List<Insight> insight;

    public NetInsight(List<Insight> insight) {
        super(OutMsgType.INSIGHT, true);
        this.insight = insight;
    }
}
