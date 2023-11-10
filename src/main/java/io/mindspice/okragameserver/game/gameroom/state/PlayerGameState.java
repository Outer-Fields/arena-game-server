package io.mindspice.okragameserver.game.gameroom.state;

import io.mindspice.okragameserver.game.enums.EffectType;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.cards.PotionCard;
import io.mindspice.okragameserver.game.gameroom.NetCombatManager;
import io.mindspice.okragameserver.game.gameroom.effect.ActiveEffect;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import io.mindspice.okragameserver.game.player.Player;
import io.mindspice.okragameserver.schema.PawnSet;

import java.util.*;
import java.util.stream.Collectors;

import static io.mindspice.okragameserver.game.enums.StatType.HP;


public class PlayerGameState {

    // Thread visibility on this class is protected by volatiles, all the methods calling methods on pawns
    //  are protected internally by the pawn class reentrant lock

    private volatile boolean isReady = false;
    private volatile boolean playerLost = false;
    private final Player player;
    private List<Pawn> pawns = new ArrayList<>(3);
    private volatile String name;
    public volatile int timedOutAmount = 0;
    private final List<PotionCard> potionCards = new ArrayList<>();
    private volatile int potionTokens = 0;
    private volatile int tokensUsed = 0;
    private volatile NetCombatManager combatManager;
    private volatile UUID roomId;

    //FIXME add full constructor
//    public PlayerGameState(Player player) {
//        this.player = player;
//    }

    public PlayerGameState(Player player, PawnSet ps) {
        this.player = player;
        for (int i = 0; i < 3; ++i) {
            PawnIndex pi = null;
            switch (i) {
                case 0 -> pi = PawnIndex.PAWN1;
                case 1 -> pi = PawnIndex.PAWN2;
                case 2 -> pi = PawnIndex.PAWN3;
            }
            pawns.add(new Pawn(
                    pi,
                    ps.pawnLoadouts()[i].pawnCard(),
                    ps.pawnLoadouts()[i].talismanCard(),
                    ps.pawnLoadouts()[i].weaponCard1(),
                    ps.pawnLoadouts()[i].weaponCard2(),
                    ps.pawnLoadouts()[i].actionDeck(),
                    ps.pawnLoadouts()[i].abilityDeck(),
                    ps.pawnLoadouts()[i].powerDeck())
            );
        }
    }

    public PlayerGameState(Player player) {
        this.player = player;
    }

    public void send(Object msgObj) {
        player.send(msgObj);
    }

//    public boolean doPotion(PotionCard potion) {
//        if (!potionCards.contains(potion) || potionTokens < potion.level) { // Cost to play is same as level
//            return false;
//        }
//        potionCards.remove(potion);
//        potionTokens -= potion.level;
//        tokensUsed += potion.level;
//        return true;
//    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;

    }

    public UUID getRoomId() {
        if (player.getGameRoom() == null) { return null; }
        return player.getGameRoom().getRoomId();
    }

    public void dealNewHands(boolean isFirst) {
        for (Pawn p : pawns) {
            if (!p.isDead()) {
                p.dealNewCards();
            }
        }
        combatManager.sendCardUpdate(isFirst);
    }

    public void resetHand() {
        for (Pawn p : pawns) {
            if (!p.isDead()) {
                p.resetCardHand();
            }
        }
    }



    /* SETTERS */

    public void setName(String name) {
        this.name = name;
    }

    public void setPlayerLost(boolean playerLost) {
        this.playerLost = playerLost;
    }

    public void setPotions(List<PotionCard> potionCards) {
        if (potionCards == null) {
            return;
        }
        this.potionCards.addAll(potionCards);
    }


    /* GETTERS */

    public Player getPlayer() {
        return player;
    }

    public Pawn getPawn(PawnIndex pawnIndex) {

        switch (pawnIndex) {
            case PAWN1 -> {
                return pawns.get(0);
            }
            case PAWN2 -> {
                return pawns.get(1);
            }
            case PAWN3 -> {
                return pawns.get(2);
            }
        }
        return null;
    }

    public int getId() {
        return player.getId();
    }

    public List<PotionCard> getPotions() {
        return List.of(); // FIXME this log will be added later
        //return potionCards;
    }

    public String getName() {
        return name;
    }

    public int getTimedOutAmount() {
        return timedOutAmount;
    }

    public List<PotionCard> getPotionCards() {
        return potionCards;
    }

    public int getTokensUsed() {
        return tokensUsed;
    }

    public List<Pawn> getPawns() {
        return pawns;
    }

    public List<Pawn> getLivingPawns() {
        return pawns.stream().filter(p -> !p.isDead()).toList();
    }

    public NetCombatManager getCombatManager() {
        return combatManager;
    }

    public void setCombatManager(NetCombatManager combatManager) {
        this.combatManager = combatManager;
    }

    public int getPotionTokens() {
        return potionTokens;
    }





    /* BOOLEANS */

    public boolean isPlayerLost() {
        return playerLost;
    }

    public boolean lossCheck() {
        //     if (playerLost) return true;
        for (Pawn p : pawns) {
            if (!p.isDead()) {
                return false;
            }
        }
        return true;
    }

    // for bot

    public List<ActiveEffect> getNegativeStatus(PawnIndex pawnIndex) {
        return getPawn(pawnIndex).getStatusEffects()
                .stream()
                .filter(e -> e.getEffectType().isNegative)
                .collect(Collectors.toList());
    }

    public int activePawnCount() {
        return pawns.stream().filter(p -> p.isActive() && !p.isDead()).toList().size();
    }

    public List<ActiveEffect> getDisablingStatuses(PawnIndex pawnIndex) {
        var statuses = getNegativeStatus(pawnIndex);
        if (statuses.isEmpty()) { return null; }
        var disablingEffects = new ArrayList<ActiveEffect>();

        for (ActiveEffect e : statuses) {
            if (e.getType() == EffectType.SLEEP || e.getType() == EffectType.CONFUSION
                    || e.getType() == EffectType.PARALYSIS) {
                if (!e.getEffect().isRollOffChance && e.getRollOffRounds() > 1) {
                    disablingEffects.add(e);
                } else {
                    disablingEffects.add(e);
                }
            }
        }
        return disablingEffects;
    }

    public boolean hasMortallyLowPawn() {
        for (Pawn p : getLivingPawns()) {
            if (p.getStat(HP) < (p.getStatMax(HP) / 6)) { return true; }
        }
        return false;
    }

    public boolean isMortallyLowPawn(PawnIndex pawnIndex) {
        var pawn = getPawn(pawnIndex);
        return pawn.getStat(HP) < pawn.getStatMax(HP) / 6;
    }

    public List<Pawn> getLowPawns() {
        return getLivingPawns()
                .stream()
                .filter(p -> p.getStat(HP) < (p.getStatMax(HP) / 2))
                .sorted(Comparator.comparingInt(p -> p.getStat(HP)))
                //.map(Pawn::getIndex)
                .toList();
    }

    public Pawn getPawnHighestAP() {
        return getLivingPawns()
                .stream()
                .max(Comparator.comparingDouble(Pawn::getActionPotential)).get();
    }

    public PawnIndex mostImportantLowPawn() {
        Pawn lowPawn = null;
        Optional<Pawn> pawn = getLivingPawns()
                .stream()
                .filter(p -> p.getStat(HP) < (p.getStatMax(HP) / 6))
                .max(Comparator.comparingDouble(Pawn::getActionPotential));

        if (pawn.isPresent()) { lowPawn = pawn.get(); }

        if (lowPawn == null) {
            pawn = getLivingPawns()
                    .stream()
                    .sorted(Comparator.comparing(p -> p.getStat(HP)))
                    .max(Comparator.comparingDouble(Pawn::getActionPotential));
        }
        if (pawn.isPresent()) { lowPawn = pawn.get(); }

        return lowPawn == null ? PawnIndex.PAWN1 : lowPawn.getIndex();
    }

    public List<Pawn> getMortalPawns() {
        return getLivingPawns().stream()
                .filter(p -> p.getStat(HP) < p.getStatMax(HP) / 6)
                .toList();
    }

    public int getTotalHP() {
        return getLivingPawns()
                .stream()
                .map(p -> p.getStat(HP))
                .reduce(0, Integer::sum);
    }

    public PlayerGameState getPlayerState() {
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PlayerGameState{");
        sb.append("isReady=").append(isReady);
        sb.append(",\n playerLost=").append(playerLost);
        sb.append(",\n player=").append(player);
        sb.append(",\n pawnLoadouts=").append(pawns);
        sb.append(",\n name='").append(name).append('\'');
        sb.append(",\n timedOutAmount=").append(timedOutAmount);
        sb.append(",\n potionCards=").append(potionCards);
        sb.append(",\n potionTokens=").append(potionTokens);
        sb.append(",\n tokensUsed=").append(tokensUsed);
        sb.append(",\n combatManager=").append(combatManager);
        sb.append('}');
        return sb.toString();
    }

//    public JsonNode getLogInfo() {
//        return new JsonUtils.ObjectBuilder()
//                .put("player_id", player.getId())
//                .put("potion_tokens", potionTokens)
//                .put("potion_tokens_used", tokensUsed)
//                .put("timed_out_amount", timedOutAmount)
//                .put("player_lost", playerLost)
//                .put("potion_cards", potionCards)
//                .put("pawn_info",
//                     Arrays.toString(new JsonNode[]{
//                             pawns[0].getStatsLog(),
//                             pawns[1].getStatsLog(),
//                             pawns[2].getStatsLog()
//                     }))
//                .buildNode();
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        PlayerGameState that = (PlayerGameState) o;
        return Objects.equals(player.getId(), that.player.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(player.getId());
    }

    public void setPawns(List<Pawn> pawns) {
        this.pawns = pawns;
    }
}
