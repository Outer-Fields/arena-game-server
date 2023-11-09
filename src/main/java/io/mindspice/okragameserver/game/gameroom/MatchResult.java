package io.mindspice.okragameserver.game.gameroom;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.gameroom.state.PlayerGameState;

import java.util.List;
import java.util.UUID;


public record MatchResult(
        UUID matchId,
        EndFlag endFlag,
        int roundCount,
        List<PlayerGameState> winners,
        List<PlayerGameState> losers,
        PlayerGameState player1,
        PlayerGameState player2,
        List<Integer> freeGameIds
) {

    public static MatchResult singleWinner(UUID matchId, EndFlag endFlag, int roundCount, PlayerGameState winningPlayer,
            PlayerGameState player1, PlayerGameState player2,  List<Integer> freeGameIds) {
        List<PlayerGameState> winners = List.of(winningPlayer);
        List<PlayerGameState> losers = List.of(winners.contains(player1) ? player2 : player1);
        return new MatchResult(matchId, endFlag ,roundCount, winners, losers, player1, player2, freeGameIds);
    }

    public static MatchResult dualWinners(UUID matchId, int roundCount,
            PlayerGameState player1, PlayerGameState player2,  List<Integer> freeGameIds) {
        return new MatchResult(
                matchId, EndFlag.BOTH, roundCount, List.of(player1, player2), List.of(), player1, player2, freeGameIds);
    }

    public static MatchResult noWinners(UUID matchId, EndFlag endFlag, int roundCount,
            PlayerGameState player1, PlayerGameState player2) {
        return new MatchResult(matchId, endFlag, roundCount,
                List.of(), List.of(player1, player2), player1, player2, List.of());
    }

    public static MatchResult unReadied(UUID matchId, PlayerGameState player1, PlayerGameState player2) {
        return new MatchResult(matchId, EndFlag.UNREADIED, 0, List.of(), List.of(), player1, player2, List.of());
    }

    public enum EndFlag {
        BOTH,
        NONE,
        WINNER,
        DISCONNECT,
        UNREADIED
    }

    public String getJsonLog() {
        try {
            return new JsonUtils.ObjectBuilder()
                    .put("match_id", matchId)
                    .put("end_flag", endFlag)
                    .put("winners", winners.stream().map(PlayerGameState::getId).toList())
                    .put("losers", losers.stream().map(PlayerGameState::getId).toList())
                    .buildString();
        } catch (JsonProcessingException e) {
            return "Error writing match json";
        }
    }
}

