package io.mindspice.okragameserver.util;

import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.bot.state.BotPlayerState;
import io.mindspice.okragameserver.game.bot.state.TreeFocusState;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import io.mindspice.okragameserver.util.jsoncontainers.BotLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

public class BotLogger {
    private final BotLog.Log log;

    public BotLogger(int botId) {
        this.log = new BotLog.Log(botId);
    }

    public void logTurnStart(BotPlayerState bps) {
        var startStats = new BotLog.TurnStartStats();
        startStats.round = bps.getGameRoom().getRound();


        for (Pawn p : bps.getLivingPawns()) {
            var pData = new BotLog.PawnData();
            pData.index = p.getIndex();
            p.getStatsMap().forEach((key, value) -> pData.stats.put(key.toString(), value));
            pData.activeEffects.addAll(p.getEffects());
            pData.activePowers.addAll(p.getPowers());
            pData.actionCard1 = p.getActionCard(1);
            pData.actionCard2 = p.getActionCard(2);
            pData.abilityCard1 = p.getAbilityCard(1);
            pData.abilityCard2 = p.getAbilityCard(2);
            pData.powerCard = p.getPowerCard();
            startStats.playerPawns.add(pData);
        }

        for (Pawn p : bps.getEnemyState().getLivingPawns()) {
            var pData = new BotLog.PawnData();
            pData.index = p.getIndex();
            p.getStatsMap().forEach((key, value) -> pData.stats.put(key.toString(), value));
            pData.activeEffects.addAll(p.getEffects());
            pData.activePowers.addAll(p.getPowers());
            pData.actionCard1 = p.getActionCard(1);
            pData.actionCard2 = p.getActionCard(2);
            pData.abilityCard1 = p.getAbilityCard(1);
            pData.abilityCard2 = p.getAbilityCard(2);
            pData.powerCard = p.getPowerCard();
            startStats.enemyPawns.add(pData);
        }
        log.turnStartStats.add(startStats);
    }

    public void logActionStats(BotPlayerState bps, TreeFocusState focusState, PawnIndex selfIndex) {

        var tas = new BotLog.TurnActionStats();
        tas.round = bps.getGameRoom().getRound();
        var p = bps.getPawn(selfIndex);

        var pData = new BotLog.PawnData();
        pData.index = p.getIndex();
        p.getStatsMap().forEach((key, value) -> pData.stats.put(key.toString(), value));
        pData.activeEffects.addAll(p.getEffects());
        pData.activePowers.addAll(p.getPowers());
        pData.actionCard1 = p.getActionCard(1);
        pData.actionCard2 = p.getActionCard(2);
        pData.abilityCard1 = p.getAbilityCard(1);
        pData.abilityCard2 = p.getAbilityCard(2);
        pData.powerCard = p.getPowerCard();
        tas.playerPawn = pData;

        if (focusState.focusPawn != null) {
            var e = bps.getEnemyState().getPawn(focusState.focusPawn);
            var eData = new BotLog.PawnData();
            eData.index = e.getIndex();
            p.getStatsMap().forEach((key, value) -> eData.stats.put(key.toString(), value));
            eData.activeEffects.addAll(e.getEffects());
            eData.activePowers.addAll(e.getPowers());
            eData.actionCard1 = e.getActionCard(1);
            eData.actionCard2 = e.getActionCard(2);
            eData.abilityCard1 = e.getAbilityCard(1);
            eData.abilityCard2 = e.getAbilityCard(2);
            eData.powerCard = e.getPowerCard();
            tas.enemyPawn = eData;
        }
        tas.decisions = focusState.decisions;
        tas.action = focusState.action;
        log.turnActionStats.add(tas);
    }

    public void logException(String e) {
        if (log.exceptions == null) {
            log.exceptions = new ArrayList<>();
        }
        log.exceptions.add(e);
    }

    public void writeLog(BotPlayerState bps) {

        File file = new File(System.getProperty("user.dir") + File.separator + "BotLogs" + File.separator
                + bps.getGameRoom().getRoomId() + " | " + bps.getPlayerState().getId() + ".json");
        file.getParentFile().mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(file, true))) {
            pw.write(JsonUtils.writeString(log));

        } catch (Exception e) {
            System.out.println("Bot Log Exception: " + e);
        }
    }
}
