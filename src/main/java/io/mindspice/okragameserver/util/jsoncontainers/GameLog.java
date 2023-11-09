package io.mindspice.okragameserver.util.jsoncontainers;

import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.enums.StatType;
import io.mindspice.okragameserver.game.gameroom.effect.ActiveEffect;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import io.mindspice.okragameserver.schema.websocket.incoming.NetGameAction;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


public class GameLog {
    public final ArrayList<ActionLog> actionLog = new ArrayList<>();
    public final ArrayList<InterimLog> interimLog = new ArrayList<>();
    public final ArrayList<StatLog> statLog = new ArrayList<>();
    public final ArrayList<String> warnings = new ArrayList<>();

  public static class ActionLog {
      public int round;
      public int playerId;
      public NetGameAction netGameAction;


      public ActionLog(int playerId, int round, NetGameAction nga) {
          this.playerId = playerId;
          this.round = round;
          this.netGameAction = nga;
      }
  }

  public static class InterimLog {
      public int round;
      public int playerId;
      public List<String> playerInterimStates;
      public List<String> enemyInterimStates;

  }

  public static class StatLog {
      public int round;
      public int playerId;
      public List<PawnStats> pawnStats;
  }

  public static class PawnStats {
      public PawnIndex pawnIndex;
      public Map<StatType, Integer> pawnStats;
      public List<String> effects;
      public List<String> cards;


      public PawnStats(Pawn pawn) {

          pawnIndex = pawn.getIndex();
          pawnStats = pawn.getStatsMap();
          effects = pawn.getStatusEffects().stream().map(ActiveEffect::toString).toList();

      }
  }


}

