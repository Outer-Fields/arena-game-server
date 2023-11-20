package io.mindspice.okragameserver.game.gameroom.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.core.Settings;
import io.mindspice.okragameserver.game.enums.*;
import io.mindspice.okragameserver.game.gameroom.effect.Insight;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import io.mindspice.okragameserver.game.gameroom.state.PawnInterimState;
import io.mindspice.okragameserver.util.Log;

import java.util.*;
import java.util.stream.Collectors;

import static io.mindspice.okragameserver.game.enums.ActionFlag.*;


public class ActionReturn {
    public List<PawnInterimState> targetPawnStates;
    public List<PawnInterimState> playerPawnStates;
    public Map<StatType, Integer> cost;
    public String animation;
    public List<Insight> insight;
    public boolean isInvalid;
    public boolean isFailed = false;
    public boolean hasInsight = false;
    public InvalidMsg invalidMsg; // for debug/log purpose
    private String cardName;
    public PlayerAction action;

//    public ActionReturn(List<PawnInterimState> playerPawnStates, Animation animation) {
//        assert (!playerPawnStates.isEmpty());
//        this.playerPawnStates = playerPawnStates;
//        this.targetPawnStates = new ArrayList<>();
//        this.animation = animation;
//        this.isInvalid = false;
//    }

    private ActionReturn(List<PawnInterimState> playerPawnStates, String cardName, InvalidMsg invalidMsg) {
        assert (!playerPawnStates.isEmpty());
        this.playerPawnStates = playerPawnStates;
        this.targetPawnStates = new ArrayList<>();
        this.animation = null;
        this.isInvalid = true;
        this.cardName = cardName;
        this.invalidMsg = invalidMsg;
    }

    public ActionReturn(List<PawnInterimState> playerPawnStates, List<PawnInterimState> targetPawnStates,
            String animation, String cardName) {
        assert (!playerPawnStates.isEmpty());
        this.playerPawnStates = playerPawnStates;
        this.targetPawnStates = targetPawnStates;
        this.animation = animation;
        this.isInvalid = false;
        this.cardName = cardName;
    }

    public void setCost(Map<StatType, Integer> cost) {
        this.cost = cost;
    }

    public void initInsight() {
        hasInsight = true;
        if (insight == null) { insight = new ArrayList<>(3); }
    }

    public void setAction(PlayerAction action) {
        this.action = action;
    }

    public void addInsight(Insight insight) {
        this.insight.add(insight);
    }

    public void doConfusion() {
        var playerState = playerPawnStates.get(0);
        playerState.getActionFlags().clear();

        for (var eis : targetPawnStates) {
            playerState.addDamage(eis.getDamageMap());
            playerState.addFlag(CONFUSED);
            playerState.addFlag(DAMAGED);
            isFailed = true;
            if (eis.getEffects() != null) {
                for (var effect : eis.getEffects()) { playerState.addEffect(effect); }
                playerState.addFlag(EFFECTED);
            }
            eis.nullEffects();
            eis.nullDamage();
        }
    }

    public void doParalysis() {
        var playerState = playerPawnStates.get(0);
        playerState.getActionFlags().clear();
        playerState.addFlag(ActionFlag.PARALYZED);
       // isFailed = true;
        for (var eis : targetPawnStates) {
            eis.nullFlags();
            eis.nullEffects();
            eis.nullDamage();
        }
        if (playerPawnStates.size() > 1) {
            for (int i =1; i < playerPawnStates.size(); ++i) {
                var pis = playerPawnStates.get(i);
                pis.nullFlags();
                pis.nullEffects();
                pis.nullDamage();
            }
        }
    }

    public void doAction() {
        // Remove un-effected interim states, first player pawn must be kept for animation indexing
        Iterator<PawnInterimState> pisIter = playerPawnStates.listIterator(1); // Start from the second pawn
        while (pisIter.hasNext()) {
            var pis = pisIter.next();
            if (pis.getActionFlags().isEmpty()) { pisIter.remove(); }
        }
        targetPawnStates.removeIf(pis -> pis.getActionFlags().isEmpty());

        if (targetPawnStates.isEmpty() && (playerPawnStates.size() == 1 && playerPawnStates.get(0).getActionFlags().isEmpty())) {
            isFailed = true;
            return;
        }

        for (PawnInterimState pis : playerPawnStates) {
            pis.doDamage();
            pis.doEffect();
        }

        for (PawnInterimState pis : targetPawnStates) {
            pis.doDamage();
            pis.doEffect();
        }
        assert (!playerPawnStates.isEmpty());
    }

    public static ActionReturn getInvalid(Pawn playerPawn, InvalidMsg msg, String cardName) {
        if (Settings.GET().advancedDebug) {
            try {
                Log.SERVER.debug(ActionReturn.class, "Error encountered playing card:" + cardName
                        + " | Msg: " + msg + " | " + JsonUtils.writeString(playerPawn.getPawnRecord()));
            } catch (JsonProcessingException ignored) { }
            Log.SERVER.logStackTrace();
        }
        return new ActionReturn(
                new ArrayList<>(List.of(new PawnInterimState(playerPawn))),
                cardName,
                msg
        );
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("ActionReturn:\n");
        sb.append("  targetPawnStates: ").append(targetPawnStates);
        sb.append("\n");
        sb.append("  playerPawnStates: ").append(playerPawnStates);
        sb.append("\n");
        sb.append("  animation: ").append(animation);
        sb.append("\n");
        sb.append("  insight: ").append(insight);
        sb.append("\n");
        sb.append("  isInvalid: ").append(isInvalid);
        sb.append("\n");
        sb.append("  isFailed: ").append(isFailed);
        sb.append("\n");
        sb.append("  hasInsight: ").append(hasInsight);
        sb.append("\n");
        sb.append("  msg: ").append(invalidMsg);
        sb.append("\n");
        sb.append("  cardName: ").append("\"").append(cardName).append('\"');
        sb.append("\n");
        return sb.toString();
    }

    public JsonNode getLogInfo() {
        return new JsonUtils.ObjectBuilder()
                .put("msg", invalidMsg)
                .put("player_pawn_states", playerPawnStates.stream().map(PawnInterimState::getLogInfo).toList())
                .put("target_pawn_states", targetPawnStates.stream().map(PawnInterimState::getLogInfo).collect(Collectors.toList()))
                .put("insight", insight)
                .put("is_invalid", isInvalid)
                .put("is_failed", isFailed)
                .buildNode();
    }

//    public ActionReturn invalidate(InvalidMsg msg) {
//        if (Settings.get().advancedDebug) { Log.SERVER.logStackTrace(); }
//        isInvalid = true;
//        playerPawnStates = playerPawnStates.subList(0,1);
//        targetPawnStates = List.of();
//        insight = null;
//        animation = null;
//        this.msg = msg;
//        return this;
//    }


}
