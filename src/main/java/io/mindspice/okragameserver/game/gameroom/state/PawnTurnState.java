package io.mindspice.okragameserver.game.gameroom.state;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;


public class PawnTurnState {
    private final Pawn pawn;
    private volatile boolean active = true;
    private volatile boolean confused = false;
    private volatile boolean paralyzed = false;

    public PawnTurnState(Pawn pawn) {
        this.pawn = pawn;
        pawn.setActive(true);
    }

    public Pawn getPawn() {
        return pawn;
    }

    public PawnIndex getIndex() {
        return pawn.getIndex();
    }

    public void setActive(boolean active) {
        pawn.setActive(active);
        this.active = active;
    }

    public void setConfused(boolean confused) {
        this.confused = confused;
    }

    public void setParalyzed(boolean paralyzed) {
        this.paralyzed = paralyzed;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isConfused() {
        return confused;
    }

    public boolean isParalyzed() {
        return paralyzed;
    }

    public ObjectNode getLogInfo() {
        return new JsonUtils.ObjectBuilder()
                .put("pawn_index", pawn.getIndex())
                .put("is_active", active)
                .put("is_confused", confused)
                .put("is_paralyzed", paralyzed)
                .buildNode();
    }

}
