package io.mindspice.okragameserver.schema.websocket.outgoing.lobby;

import io.mindspice.okragameserver.schema.websocket.outgoing.OutMsgType;

public class NetPlayerFunds {
    public final OutMsgType msg_type = OutMsgType.PLAYER_FUNDS;
    public int okra_tokens;
    public int potion_tokens;
    public int nft_drops;

    public NetPlayerFunds(int[] funds) {
        okra_tokens = funds[0];
        potion_tokens = funds[1];
        nft_drops = funds[2];
    }

    public NetPlayerFunds() {
    }
}
