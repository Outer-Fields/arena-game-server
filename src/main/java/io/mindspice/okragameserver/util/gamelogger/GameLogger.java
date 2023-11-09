package io.mindspice.okragameserver.util.gamelogger;

import com.fasterxml.jackson.databind.JsonNode;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import io.mindspice.okragameserver.game.gameroom.state.PlayerGameState;
import io.mindspice.okragameserver.schema.websocket.incoming.NetGameAction;
import io.mindspice.okragameserver.util.Log;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;


public class GameLogger {
    private final static GameLogger INSTANCE = new GameLogger();
    private final static Map<UUID, RoundRecord> roundRecords = new HashMap<>();
    private final static Map<UUID, JsonAppendWriter> logFiles = new HashMap<>();

    private GameLogger() { }


    public static GameLogger GET() {
        return INSTANCE;
    }
    public void init(UUID roomId) {
        try {
            logFiles.put(roomId, new JsonAppendWriter(roomId));
        } catch (IOException e) {
            Log.SERVER.error(this.getClass(), "Error creating game log for RoomId:" + roomId);
        }
    }

    public void addRoundRecord(UUID roomId, RoundRecord roundRecord) {
        writeLog(roomId);
        roundRecords.put(roomId, roundRecord);
    }

    public void addRoundRecord(PlayerGameState player1, PlayerGameState player2) {
        UUID roomId = player1.getPlayer().getGameRoom().getRoomId();
        int round = player1.getPlayer().getGameRoom().getRound();
        List<JsonNode> player1PawnRecords = player1.getPawns().stream().map(p -> JsonUtils.toNode(p.getPawnRecord())).toList();
        List<JsonNode> player2PawnRecords = player2.getPawns().stream().map(p -> JsonUtils.toNode(p.getPawnRecord())).toList();
        PlayerRecord player1Record = new PlayerRecord(player1.getId(), player1PawnRecords);
        PlayerRecord player2Record = new PlayerRecord(player2.getId(), player2PawnRecords);
        RoundRecord record = new RoundRecord(round, player1Record, player2Record);
        if (roundRecords.containsKey(roomId)) { writeLog(roomId); }
        roundRecords.put(roomId, record);
    }

    public void addActionMsgIn(UUID roomId, int playerId, NetGameAction nga) {
        RoundRecord record = roundRecords.get(roomId);
        if (record == null) {
           // Log.SERVER.error(this.getClass(), "Round record not found for RoomId:" + roomId);
            return;
        }
        record.getPlayerById(playerId).actionsIn().add(nga);
    }

    public void addBotDecision(UUID roomId, int playerId, PawnIndex index , List<String> decisions) {
        RoundRecord record = roundRecords.get(roomId);
        if (record == null) {
           // Log.SERVER.error(this.getClass(), "Round record not found for RoomId:" + roomId);
            return;
        }
        record.getPlayerById(playerId).botDecisions().put(index, decisions);
    }

    public void addMsgOut(UUID roomId, int playerId, Object msg) {
        RoundRecord record = roundRecords.get(roomId);
        if (record == null) {
            //Log.SERVER.error(this.getClass(), "Round record not found for RoomId:" + roomId);
            return;
        }
        record.getPlayerById(playerId).outMessages().add(msg);
    }

    public void endLog(UUID roomId) {
        writeLog(roomId);
        try {
            logFiles.get(roomId).close();
        } catch (IOException e) {
            Log.SERVER.error(this.getClass(), "Error closing game log for RoomId:" + roomId);
        }
        roundRecords.remove(roomId);
        logFiles.remove(roomId);
    }

    private void writeLog(UUID roomId) {
        JsonAppendWriter writer = logFiles.get(roomId);
        if (writer != null) {
            try {
                writer.writeLog(roundRecords.get(roomId));
            } catch (IOException e) {
                Log.SERVER.error(this.getClass(), "Error writing game log for RoomId:" + roomId);
            }
        }
    }

    public void closeAllLogs() {
        for(var log : logFiles.values()) {
            try {
                log.close();
            } catch (IOException e) {
                Log.SERVER.error(this.getClass(), "IOException closing log file.");

            }
        }
    }
}
