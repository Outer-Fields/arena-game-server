package io.mindspice.okragameserver.game.gameroom.pawn;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.cards.*;
import io.mindspice.okragameserver.game.enums.*;
import io.mindspice.okragameserver.game.gameroom.action.ActivePower;
import io.mindspice.okragameserver.game.gameroom.effect.ActiveEffect;
import io.mindspice.okragameserver.game.gameroom.effect.Effect;
import io.mindspice.okragameserver.game.gameroom.gameutil.Utils;
import io.mindspice.okragameserver.schema.websocket.outgoing.game.CardHand;
import io.mindspice.okragameserver.schema.websocket.outgoing.game.EffectStats;
import io.mindspice.okragameserver.schema.websocket.outgoing.game.NetEffect;
import io.mindspice.okragameserver.util.Log;
import io.mindspice.okragameserver.util.gamelogger.PawnRecord;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.mindspice.okragameserver.game.enums.PowerEnums.PowerType.INCREASE_LUCK;
import static io.mindspice.okragameserver.game.enums.StatType.*;


public class Pawn {

    private final PawnIndex index;
    private final PawnCard pawnCard;
    private volatile boolean isDead = false;
    private volatile boolean isActive = true;
    private volatile boolean sentDead = false;

    // Stats Contexts
    private final Map<StatType, Integer> stats;
    private final Map<StatType, Integer> statsMax;

    // These hold a static copy of the card hand for re-deals
    private final List<ActionCard> actionDeckStatic;
    private final List<AbilityCard> abilityDeckStatic;
    private final List<PowerCard> powerDeckStatic;

    // These are the card hands that are manipulated through play
    private final List<ActionCard> actionDeck = new ArrayList<>();
    private final List<AbilityCard> abilityDeck = new ArrayList<>();
    private final List<PowerCard> powerDeck = new ArrayList<>();

    // Card context
    private WeaponCard weaponCard1;
    private WeaponCard weaponCard2;
    private ActionCard actionCard1;
    private ActionCard actionCard2;
    private AbilityCard abilityCard1;
    private AbilityCard abilityCard2;
    private PowerCard powerCard;
    private final TalismanCard talisman;
    private final Alignment alignment;

    private final List<ActivePower> activePowers = new ArrayList<>();
    private final List<ActiveEffect> statusEffects = new ArrayList<>();
    // Used to not send effect/stat info multiple times in the same round, if no change has occurred
    private volatile int effectHash = 0;
    private volatile int statsHash = 0;
    private volatile int playableCardHash;

    Random rand = new Random(System.nanoTime());

    // Lock due to thread-pool executor using different threads at times
    public final ReentrantLock lock = new ReentrantLock(true);

    public Pawn(PawnIndex index, PawnCard pawnCard, TalismanCard talisman, WeaponCard weaponCard1, WeaponCard weapon2,
            List<ActionCard> actionDeckStatic, List<AbilityCard> abilityDeckStatic, List<PowerCard> powerDeckStatic) {
        this.index = index;
        this.pawnCard = pawnCard;
        this.talisman = talisman;
        this.actionDeckStatic = List.copyOf(actionDeckStatic);
        this.abilityDeckStatic = List.copyOf(abilityDeckStatic);
        this.powerDeckStatic = List.copyOf(powerDeckStatic);
        this.weaponCard1 = weaponCard1;
        this.weaponCard2 = weapon2;
        this.stats = Collections.synchronizedMap(pawnCard.stats.clone());
        this.statsMax = Collections.synchronizedMap(pawnCard.statsMax.clone());
        this.alignment = talisman.alignment;
        actionDeck.addAll(this.actionDeckStatic);
        abilityDeck.addAll(this.abilityDeckStatic);
        powerDeck.addAll(this.powerDeckStatic);
        Collections.shuffle(actionDeck, rand);
        Collections.shuffle(abilityDeck, rand);
        Collections.shuffle(powerDeck, rand);

        talisman.statChange.forEach((k, v) -> {
            if (k == StatType.WP) { // Limit raising WP higher than pawn max, as this is important to balance as it affect regeneration.
                stats.put(k, Math.min(stats.get(k) + v, pawnCard.statsMax.get(WP)));
            } else {
                stats.put(k, Math.max(stats.get(k) + v, 0));
            }
        });

        talisman.maxChange.forEach((k, v) -> {
            statsMax.put(k, statsMax.get(k) + v);
        });

    }

    public void regenerate() {
        if (isDead) { return; }
        lock.lock();
        try {
            var regenMap = pawnCard.regen.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> Math.min(e.getValue() * stats.get(WP), statsMax.get(e.getKey())),
                    (e1, e2) -> e1,
                    () -> new EnumMap<>(StatType.class)
            ));
            updateStats(regenMap, true);
        } finally {
            lock.unlock();
        }
    }

    public void dealNewCards() {
        try {
            lock.lock();
            var oldPowers = activePowers
                    .stream()
                    .filter(p -> p.getCard() == this.powerCard).toList();

            activePowers.removeAll(oldPowers);
            powerCard = powerDeck.get(powerDeck.size() - 1);
            powerDeck.remove(powerCard);
            activePowers.addAll(powerCard.getActivePowers());

            if (actionCard1 == null) {
                actionCard1 = actionDeck.get(actionDeck.size() - 1);
                actionDeck.remove(actionCard1);
            }
            if (actionCard2 == null) {
                actionCard2 = actionDeck.get(actionDeck.size() - 1);
                actionDeck.remove(actionCard2);
            }
            if (abilityCard1 == null) {
                abilityCard1 = abilityDeck.get(abilityDeck.size() - 1);
                abilityDeck.remove(abilityCard1);
            }
            if (abilityCard2 == null) {
                abilityCard2 = abilityDeck.get(abilityDeck.size() - 1);
                abilityDeck.remove(abilityCard2);
            }
        } finally {
            lock.unlock();
        }
    }

    public void resetCardHand() {
        try {
            lock.lock();
            actionDeck.clear();
            actionDeck.addAll(actionDeckStatic);
            abilityDeck.clear();
            abilityDeck.addAll(abilityDeckStatic);
            powerDeck.clear();
            powerDeck.addAll(powerDeckStatic);

            Collections.shuffle(actionDeck);
            Collections.shuffle(abilityDeck);
            Collections.shuffle(powerDeck);
        } finally {
            lock.unlock();
        }
    }

    public void replaceCard(Card oldCard, Card newCard) {
        try {
            lock.lock();
            if (weaponCard1 == oldCard) { weaponCard1 = (WeaponCard) newCard; }
            if (weaponCard2 == oldCard) { weaponCard2 = (WeaponCard) newCard; }
            if (actionCard1 == oldCard) { actionCard1 = (ActionCard) newCard; }
            if (actionCard2 == oldCard) { actionCard2 = (ActionCard) newCard; }
            if (abilityCard1 == oldCard) { abilityCard1 = (AbilityCard) newCard; }
            if (abilityCard2 == oldCard) { abilityCard2 = (AbilityCard) newCard; }
            if (powerCard == oldCard) { powerCard = (PowerCard) newCard; }
        } finally {
            lock.unlock();
        }
    }


    /* CHECKS */

    public boolean isDead() {
        try {
            lock.lock();
            if (getStat(HP) <= 0) {
                isDead = true;
                isActive = false;
            }

            return isDead;
        } finally {
            lock.unlock();
        }
    }

    public boolean isActive() {
        return isActive; // volatile
    }

    /* SETTERS */

    public void updateStat(StatType statType, int amount, boolean isIncrease) {
        try {
            lock.lock();
            if (amount == 0) { return; } // needed
            if (amount < 0) {
                amount = 0;
                Log.SERVER.debug(this.getClass(), "Negative stats update");
            }
            int statOld = stats.get(statType);
            int statNew;

            if (isIncrease) {
                statNew = statOld + amount;
            } else {
                statNew = statOld - amount;
            }
            if (statNew > statsMax.get(statType)) {
                stats.put(statType, statsMax.get(statType));
            } else { stats.put(statType, Math.max(statNew, 0)); }

            if (getStat(HP) <= 0) {
                isDead = true;
                isActive = false;
            }
        } finally {
            lock.unlock();
        }
    }

    public void updateStats(Map<StatType, Integer> statMap, boolean isIncrease) {
        try {
            lock.lock();
            statMap.forEach((key, value) -> {
                updateStat(key, value, isIncrease);
            });
        } finally {
            lock.unlock();
        }
    }

    public void updateStatsMax(Map<StatType, Integer> statMap, boolean isIncrease) {
        try {
            lock.lock();
            statMap.forEach((key, value) -> {
                updateStatMax(key, value, isIncrease);
            });
        } finally {
            lock.unlock();
        }
    }

    // Be careful, can shoot foot with wrong inputs.
    public void updateStatMax(StatType statType, int amount, boolean isIncrease) {
        try {
            lock.lock();
            if (statType == WP || statType == LP) {
                return;
            }
            if (amount < 0) { amount = 0; }
            int statOld = statsMax.get(statType);
            int statNew;

            if (isIncrease) {
                statNew = statOld + amount;
            } else {
                statNew = statOld - amount;
            }
            statsMax.put(statType, Math.max(statNew, 0));
        } finally {
            lock.unlock();
        }
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive; // volatile
    }

    public void addStatusEffect(Effect effect) {
        try {
            lock.lock();
            statusEffects.add(new ActiveEffect(effect, this));
        } finally {
            lock.unlock();
        }
    }

    public boolean hasStatusInsight() {
        try {
            lock.lock();
            return statusEffects.stream().anyMatch(e -> e.getType() == EffectType.INSIGHT_STATUS);
        } finally {
            lock.unlock();
        }
    }

    public void removeStatusEffect(ActiveEffect activeEffect) {
        try {
            lock.lock();
            var rm = statusEffects.removeIf(e -> e.getId().equals(activeEffect.getId()));
            Log.SERVER.debug(this.getClass(), " Effect Remove Success:" + rm);
        } finally {
            lock.unlock();
        }
    }

    /* GETTERS */

    public List<ActiveEffect> getStatusEffects() {
        try {
            lock.lock();
            return statusEffects;
        } finally {
            lock.unlock();
        }
    }

    public PawnCard getPawnCard() {
        try {
            lock.lock();
            return pawnCard;
        } finally {
            lock.unlock();
        }
    }

    public List<PowerEnums.PowerType> getPowers() {
        try {
            lock.lock();
            List<PowerEnums.PowerType> powers = new ArrayList<>();
            activePowers.forEach((activePower) -> powers.add(activePower.getType()));
            return powers;
        } finally {
            lock.unlock();
        }
    }

    //this is used by the mod and gotten from another thread hence the copy before iterating
    public List<EffectType> getEffects() {
        try {
            lock.lock();
            List<ActiveEffect> effects = new ArrayList<>(statusEffects);
            return effects.stream().map(ActiveEffect::getType).toList();
        } finally {
            lock.unlock();
        }
    }

    public ActionCard getActionCard(int i) {
        try {
            lock.lock();
            if (i == 1) {
                return actionCard1;
            } else {
                return actionCard2;
            }
        } finally {
            lock.unlock();
        }
    }

    public AbilityCard getAbilityCard(int i) {
        try {
            lock.lock();
            if (i == 1) {
                return abilityCard1;
            } else {
                return abilityCard2;
            }
        } finally {
            lock.unlock();
        }
    }

    public void setActionCard(ActionCard card, int idx) {
        try {
            lock.lock();
            if (idx < 1 || idx > 2) {
                throw new IllegalStateException("Invalid set index");
            }
            if (idx == 1) {
                actionCard1 = card;
            } else {
                actionCard2 = card;
            }
        } finally {
            lock.unlock();
        }
    }

    public void setAbilityCard(AbilityCard card, int idx) {
        try {
            lock.lock();
            if (idx < 1 || idx > 2) {
                throw new IllegalStateException("Invalid set index");
            }
            if (idx == 1) {
                abilityCard1 = card;
            } else {
                abilityCard2 = card;
            }
        } finally {
            lock.unlock();
        }
    }

    public TalismanCard getTalisman() {
        try {
            lock.lock();
            return talisman;
        } finally {
            lock.unlock();
        }
    }

    public Map<PlayerAction, Card> getActionCards() {
        try {
            lock.lock();
            Map<PlayerAction, Card> rtnMap = new EnumMap<>(PlayerAction.class);
            if (actionCard1 != null) { rtnMap.put(PlayerAction.ACTION_CARD_1, actionCard1); }
            if (actionCard2 != null) { rtnMap.put(PlayerAction.ACTION_CARD_2, actionCard2); }
            return rtnMap;
        } finally {
            lock.unlock();
        }
    }

    public Map<PlayerAction, Card> getAbilityCards() {
        try {
            lock.lock();
            Map<PlayerAction, Card> rtnMap = new EnumMap<>(PlayerAction.class);
            if (abilityCard1 != null) { rtnMap.put(PlayerAction.ABILITY_CARD_1, abilityCard1); }
            if (abilityCard2 != null) { rtnMap.put(PlayerAction.ABILITY_CARD_2, abilityCard2); }
            return rtnMap;
        } finally {
            lock.unlock();
        }
    }

    public Map<PlayerAction, Card> getWeaponCards() {
        try {
            lock.lock();
            Map<PlayerAction, Card> rtnMap = new EnumMap<>(PlayerAction.class);
            if (weaponCard1 != null) { rtnMap.put(PlayerAction.WEAPON_CARD_1, weaponCard1); }
            if (weaponCard2 != null) { rtnMap.put(PlayerAction.WEAPON_CARD_2, weaponCard2); }
            return rtnMap;
        } finally {
            lock.unlock();
        }
    }

    public Map<PlayerAction, Card> getAllCards() {
        try {
            lock.lock();
            Map<PlayerAction, Card> rtnMap = new EnumMap<>(PlayerAction.class);
            if (actionCard1 != null) { rtnMap.put(PlayerAction.ACTION_CARD_1, actionCard1); }
            if (actionCard2 != null) { rtnMap.put(PlayerAction.ACTION_CARD_2, actionCard2); }
            if (abilityCard1 != null) { rtnMap.put(PlayerAction.ABILITY_CARD_1, abilityCard1); }
            if (abilityCard2 != null) { rtnMap.put(PlayerAction.ABILITY_CARD_2, abilityCard2); }
            if (weaponCard1 != null) { rtnMap.put(PlayerAction.WEAPON_CARD_1, weaponCard1); }
            if (weaponCard2 != null) { rtnMap.put(PlayerAction.WEAPON_CARD_2, weaponCard2); }
            return rtnMap;
        } finally {
            lock.unlock();
        }
    }

    public PlayerAction getCardSlot(Card card) {
        try {
            lock.lock();
            if (actionCard1 == card) { return PlayerAction.ACTION_CARD_1; }
            if (actionCard2 == card) { return PlayerAction.ACTION_CARD_2; }
            if (abilityCard1 == card) { return PlayerAction.ABILITY_CARD_1; }
            if (abilityCard2 == card) { return PlayerAction.ABILITY_CARD_2; }
            if (weaponCard1 == card) { return PlayerAction.WEAPON_CARD_1; }
            if (weaponCard2 == card) { return PlayerAction.WEAPON_CARD_2; }
            return null;
        } finally {
            lock.unlock();
        }
    }

    public List<EffectType> getStatusEnums() {
        try {
            lock.lock();
            List<EffectType> status = new ArrayList<>();
            for (ActiveEffect e : statusEffects) {
                status.add(e.getType());
            }
            return status;
        } finally {
            lock.unlock();
        }
    }

    public int getActionHandSize() {
        try {
            lock.lock();
            return actionDeck.size();
        } finally {
            lock.unlock();
        }
    }

    public List<ActionCard> getActionDeck() {
        try {
            lock.lock();
            return actionDeckStatic;
        } finally {
            lock.unlock();
        }
    }

    public List<AbilityCard> getAbilityDeck() {
        try {
            lock.lock();
            return abilityDeckStatic;
        } finally {
            lock.unlock();

        }
    }

    // could replace with streams
    public EnumMap<PowerEnums.PowerReturn, Double> getPowerAction(boolean isOffense, ActionType actionType) {
        try {
            lock.lock();
            EnumMap<PowerEnums.PowerReturn, Double> returnMap = new EnumMap<>(PowerEnums.PowerReturn.class);
            int luck = getStat(LP);
            for (ActivePower p : activePowers) {
                var tmpRtn = isOffense
                        ? p.getActionOffense(actionType, luck)
                        : p.getActionDefense(actionType, luck);
                tmpRtn.forEach((key, value) -> {
                    if (returnMap.containsKey(key)) {
                        returnMap.put(key, value + returnMap.get(key));
                    } else {
                        returnMap.put(key, value);
                    }
                });
            }
            return returnMap;
        } finally {
            lock.unlock();
        }
    }

    public EnumMap<PowerEnums.PowerReturn, Double> getPowerAbilityDefense(EffectType effectType) {
        try {
            lock.lock();
            EnumMap<PowerEnums.PowerReturn, Double> returnMap = new EnumMap<>(PowerEnums.PowerReturn.class);
            int luck = getStat(LP);
            for (ActivePower p : activePowers) {
                p.getEffectDefense(effectType, luck).forEach((key, value) -> {
                    if (returnMap.containsKey(key)) {
                        returnMap.put(key, value + returnMap.get(key));
                    } else {
                        returnMap.put(key, value);
                    }
                });
            }
            return returnMap;
        } finally {
            lock.unlock();
        }
    }

    // For the bot
    public int getActionPotential() {
        try {
            lock.lock();
            int ap = 0;
            if (actionCard1 != null) { ap += actionCard1.getStats().getLevel() * 10; }
            if (actionCard2 != null) { ap += actionCard2.getStats().getLevel() * 10; }
            if (abilityCard1 != null) { ap += abilityCard1.getStats().getLevel() * 10; }
            if (abilityCard2 != null) { ap += abilityCard2.getStats().getLevel() * 10; }
            if (weaponCard1 != null) { ap += weaponCard1.getStats().getLevel() * 10; }
            if (weaponCard2 != null) { ap += weaponCard2.getStats().getLevel() * 10; }
            ap += getStat(HP) / 5.0;
            ap += getStat(SP) / 5.0;
            ap += getStat(MP) / 5.0;
            ap += getStat(DP) / 5.0;

            for (EffectType e : getEffects()) {
                if (e.effectClass == EffectType.EffectClass.CURSE) {
                    if (e == EffectType.SLEEP) {
                        ap -= 500;
                    } else if (e == EffectType.CONFUSION) {
                        ap -= 500;
                    } else if (e == EffectType.PARALYSIS) {
                        ap -= 500;
                    } else {
                        ap += -150;
                    }
                }
                if (e.effectClass == EffectType.EffectClass.MODIFIER) {
                    if (e.isNegative) {
                        ap -= e.scalar / 3;
                    } else {
                        ap += e.scalar / 3;
                    }
                }
            }
            return (int) ap;
        } finally {
            lock.unlock();
        }
    }

    public void removeWeaponCard(WeaponCard card) {
        try {
            lock.lock();
            if (weaponCard1 == card) { weaponCard1 = null; }
            if (weaponCard2 == card) { weaponCard2 = null; }
        } finally {
            lock.unlock();
        }
    }

    public void removeActionCard(int i) {
        try {
            lock.lock();
            if (i == 1) {
                actionCard1 = null;
            } else {
                actionCard2 = null;
            }
        } finally {
            lock.unlock();
        }
    }

    public void removeAbilityCard(int i) {
        try {
            lock.lock();
            if (i == 1) {
                abilityCard1 = null;
            } else {
                abilityCard2 = null;
            }
        } finally {
            lock.unlock();
        }

    }

    public Alignment getAlignment() {
        return alignment; // final
    }

    public List<ActionCard> getActionDeckStatic() {
        try {
            lock.lock();
            return actionDeckStatic;
        } finally {
            lock.unlock();
        }
    }

    public List<AbilityCard> getAbilityDeckStatic() {
        try {
            lock.lock();
            return abilityDeckStatic;
        } finally {
            lock.unlock();
        }
    }

    public List<PowerCard> getPowerDeckStatic() {
        try {
            lock.lock();
            return powerDeckStatic;
        } finally {
            lock.unlock();
        }
    }

    public int getStat(StatType statType) {
        try {
            lock.lock();
            if (statType == LP) {
                double luckScalar = 1;
                for (ActivePower p : activePowers) {
                    if (p.getType() == INCREASE_LUCK) {
                        luckScalar += p.getLuckMod();
                    }
                }
                int scaledLuck = (int) (stats.get(LP) * luckScalar);
                return Math.min(scaledLuck, 20);
            }
            return stats.get(statType);
        } finally {
            lock.unlock();
        }
    }

    public Map<StatType, Integer> getHp() {
        try {
            lock.lock();
            return Collections.singletonMap(StatType.HP, stats.get(StatType.HP));
        } finally {
            lock.unlock();
        }
    }

    public Integer getStatMax(StatType statType) {
        try {
            lock.lock();
            return statsMax.get(statType);
        } finally {
            lock.unlock();
        }
    }

    public PawnIndex getIndex() {
        return index; // final
    }

    public Map<StatType, Integer> getStatsMap() {
        try {
            lock.lock();
            return Collections.unmodifiableMap(stats);
        } finally {
            lock.unlock();
        }
    }

    public Map<StatType, Integer> getStatsMaxMap() {
        try {
            lock.lock();
            return Collections.unmodifiableMap(statsMax);
        } finally {
            lock.unlock();
        }
    }

    public WeaponCard getWeaponCard(int i) {
        try {
            lock.lock();
            if (i == 1) {
                return weaponCard1;
            } else {
                return weaponCard2;
            }
        } finally {
            lock.unlock();
        }
    }

    public void setWeapon(WeaponCard weapon, int idx) {
        try {
            lock.lock();
            if (idx < 1 || idx > 2) {
                throw new IllegalStateException("Incorrect set index");
            }
            if (idx == 1) {
                weaponCard1 = weapon;
            } else {
                weaponCard2 = weapon;
            }
        } finally {
            lock.unlock();
        }
    }

    public PowerCard getPowerCard() {
        try {
            lock.lock();
            return powerCard;
        } finally {
            lock.unlock();
        }
    }

    public List<ActivePower> getActivePowers() {
        try {
            lock.lock();
            return activePowers;
        } finally {
            lock.unlock();
        }
    }

    public CardHand getCardHand(boolean isFirst) {
        try {
            lock.lock();
            var hand = new CardHand();
            if (isFirst) {
                hand.PAWN_CARD = pawnCard;
                hand.WEAPON_CARD_1 = weaponCard1;
                hand.WEAPON_CARD_2 = weaponCard2;
                hand.TALISMAN_CARD = talisman;
            }
            hand.ACTION_CARD_1 = actionCard1;
            hand.ACTION_CARD_2 = actionCard2;
            hand.ABILITY_CARD_1 = abilityCard1;
            hand.ABILITY_CARD_2 = abilityCard2;
            hand.POWER_CARD = powerCard;
            return hand;
        } finally {
            lock.unlock();
        }
    }

    public CardHand getEnemyCardHand() {
        try {
            lock.lock();
            var hand = new CardHand();
            hand.PAWN_CARD = pawnCard;
            hand.WEAPON_CARD_1 = weaponCard1;
            hand.WEAPON_CARD_2 = weaponCard2;
            return hand;
        } finally {
            lock.unlock();
        }
    }

    public Card getAction(PlayerAction action) {
        try {
            lock.lock();
            switch (action) {

                case WEAPON_CARD_1 -> {
                    return weaponCard1;
                }
                case WEAPON_CARD_2 -> {
                    return weaponCard2;
                }
                case ACTION_CARD_1 -> {
                    return actionCard1;
                }
                case ACTION_CARD_2 -> {
                    return actionCard2;
                }
                case ABILITY_CARD_1 -> {
                    return abilityCard1;
                }
                case ABILITY_CARD_2 -> {
                    return abilityCard2;
                }
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    public String getStatsLog() {
        try {
            lock.lock();
            return new JsonUtils.ObjectBuilder()
                    .put("pawn_index", index)
                    .put("is_dead", isDead)
                    .put("stats", stats)
                    .put("stats_max", statsMax)
                    .put("active_powers", activePowers.stream().map(ActivePower::getType).toList())
                    .put("effects", statusEffects.stream().map(ActiveEffect::getType).toList())
                    .put("stat_hash", statsHash)
                    .put("effect_hash", effectHash)
                    .buildString();
        } catch (JsonProcessingException e) {
            return "LOG ERROR!" + e.getMessage();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        try {
            lock.lock();
            final StringBuilder sb = new StringBuilder("Pawn:\n");
            sb.append("  index: ").append(index);
            sb.append("\n");
            sb.append("  pawnCard: ").append(pawnCard);
            sb.append("\n");
            sb.append("  isDead: ").append(isDead);
            sb.append("\n");
            sb.append("  isActive: ").append(isActive);
            sb.append("\n");
            sb.append("  sentDead: ").append(sentDead);
            sb.append("\n");
            sb.append("  stats: ").append(stats);
            sb.append("\n");
            sb.append("  statsMax: ").append(statsMax);
            sb.append("\n");
            sb.append("  actionDeckStatic: ").append(actionDeckStatic);
            sb.append("\n");
            sb.append("  abilityDeckStatic: ").append(abilityDeckStatic);
            sb.append("\n");
            sb.append("  powerDeckStatic: ").append(powerDeckStatic);
            sb.append("\n");
            sb.append("  actionDeck: ").append(actionDeck);
            sb.append("\n");
            sb.append("  abilityDeck: ").append(abilityDeck);
            sb.append("\n");
            sb.append("  powerDeck: ").append(powerDeck);
            sb.append("\n");
            sb.append("  weapon1: ").append(weaponCard1);
            sb.append("\n");
            sb.append("  weapon2: ").append(weaponCard2);
            sb.append("\n");
            sb.append("  actionCard1: ").append(actionCard1);
            sb.append("\n");
            sb.append("  actionCard2: ").append(actionCard2);
            sb.append("\n");
            sb.append("  abilityCard1: ").append(abilityCard1);
            sb.append("\n");
            sb.append("  abilityCard2: ").append(abilityCard2);
            sb.append("\n");
            sb.append("  powerCard: ").append(powerCard);
            sb.append("\n");
            sb.append("  talisman: ").append(talisman);
            sb.append("\n");
            sb.append("  alignment: ").append(alignment);
            sb.append("\n");
            sb.append("  activePowers: ").append(activePowers);
            sb.append("\n");
            sb.append("  statusEffects: ").append(statusEffects);
            sb.append("\n");
            sb.append("  effectHash: ").append(effectHash);
            sb.append("\n");
            sb.append("  statsHash: ").append(statsHash);
            sb.append("\n");
            sb.append("  playableCardHash: ").append(playableCardHash);
            sb.append("\n");
            return sb.toString();
        } finally {
            lock.unlock();
        }
    }

    public String getCardLog() {
        try {
            lock.lock();
            return new JsonUtils.ObjectBuilder()
                    .put("weapon_1", weaponCard1)
                    .put("weapon_2", weaponCard2)
                    .put("pawn_card", pawnCard)
                    .put("action_card_1", actionCard1)
                    .put("action_card_2", actionCard2)
                    .put("ability_card_1", abilityCard1)
                    .put("ability_card_2", abilityCard2)
                    .put("power_card", powerCard)
                    .buildString();
        } catch (JsonProcessingException e) {
            return "LOG ERROR!" + e.getMessage();
        } finally {
            lock.unlock();
        }
    }

    public PawnRecord getPawnRecord() {
        try {
            lock.lock();
            return new PawnRecord(
                    index,
                    stats,
                    statsHash,
                    statsMax,
                    statusEffects.stream().map(ActiveEffect::getEffectRecord).toList(),
                    effectHash,
                    activePowers.stream().map(ActivePower::getPowerRecord).toList(),
                    pawnCard,
                    powerCard,
                    weaponCard1,
                    weaponCard2,
                    actionCard1,
                    actionCard2,
                    abilityCard1,
                    abilityCard2,
                    talisman
            );
        } finally {
            lock.unlock();
        }
    }

    // Group stats by their type, and sum their amount and rollOffRound/expectedRollOffRound
    // EffectStat converts these into enums representing effect amount and time for ui
    public List<EffectStats> getNetEffects() {
        try {
            lock.lock();
            effectHash = statusEffects.hashCode();
            return statusEffects.stream()
                    .collect(Collectors.groupingBy(ActiveEffect::getType)).entrySet().stream()
                    .map(entry -> {
                        var v = entry.getValue();
                        return new EffectStats(
                                entry.getKey(),
                                v.stream().mapToInt(ActiveEffect::getAmount).sum(),
                                v.stream().mapToInt(ActiveEffect::getExpectedRollOffRound).max().orElse(0),
                                v.stream().allMatch(ActiveEffect::isCurable));

                    }).toList();
        } finally {
            lock.unlock();
        }
    }

    public Map<StatType, Integer> getNetStats() {
        try {
            lock.lock();
            statsHash = stats.hashCode();
            return stats;
        } finally {
            lock.unlock();
        }
    }

    public boolean hasEffectsChanged() {
        try {
            lock.lock();
            return effectHash != statusEffects.hashCode();
        } finally {
            lock.unlock();
        }
    }

    public boolean hasStatsChanged() {
        try {
            lock.lock();
            return statsHash != stats.hashCode();
        } finally {
            lock.unlock();
        }
    }

    public Map<PlayerAction, Integer> getPlayableCards() {
        try {
            lock.lock();
            Map<PlayerAction, Integer> playableCards = new EnumMap<>(PlayerAction.class);
            playableCards.put(PlayerAction.WEAPON_CARD_1, weaponCard1 == null || isDead ? -1 : isActive && canDoCost(weaponCard1) ? 1 : 0);
            playableCards.put(PlayerAction.WEAPON_CARD_2, weaponCard2 == null || isDead ? -1 : isActive && canDoCost(weaponCard2) ? 1 : 0);
            playableCards.put(PlayerAction.ACTION_CARD_1, actionCard1 == null || isDead ? -1 : isActive && canDoCost(actionCard1) ? 1 : 0);
            playableCards.put(PlayerAction.ACTION_CARD_2, actionCard2 == null || isDead ? -1 : isActive && canDoCost(actionCard2) ? 1 : 0);
            playableCards.put(PlayerAction.ABILITY_CARD_1, abilityCard1 == null || isDead ? -1 : isActive && canDoCost(abilityCard1) ? 1 : 0);
            playableCards.put(PlayerAction.ABILITY_CARD_2, abilityCard2 == null || isDead ? -1 : isActive && canDoCost(abilityCard2) ? 1 : 0);
            playableCardHash = playableCards.hashCode();
            return playableCards;
        } finally {
            lock.unlock();
        }
    }

    private boolean canDoCost(Card card) {
        try {
            lock.lock();
            var statMap = card.getStats().getCost().asMap();
            return statMap.entrySet().stream()
                    .filter(e -> StatType.costStats().contains(e.getKey()))
                    .allMatch(entry -> getStat(entry.getKey()) >= entry.getValue());
        } finally {
            lock.unlock();
        }
    }

    public void setSentDead() {
        sentDead = true; //volatile
    }

    public boolean haveSentDead() {
        return sentDead; //volatile
    }

    /* FOR TESTING & BOT */

    public void setActionCard1(ActionCard card) {
        this.actionCard1 = card;
    }

    public void setActionCard2(ActionCard card) {
        this.actionCard2 = card;
    }

    public void setAbilityCard1(AbilityCard card) {
        this.abilityCard1 = card;
    }

    public void setAbilityCard2(AbilityCard card) {
        this.abilityCard2 = card;
    }

    public Card getCardByAction(PlayerAction action) {
        switch (action) {
            case WEAPON_CARD_1 -> { return weaponCard1; }
            case WEAPON_CARD_2 -> { return weaponCard2; }
            case ACTION_CARD_1 -> { return actionCard1; }
            case ACTION_CARD_2 -> { return actionCard2; }
            case ABILITY_CARD_1 -> { return abilityCard1; }
            case ABILITY_CARD_2 -> { return abilityCard2; }
            default -> { return null; }
        }
    }

    public int getStatsHash() {
        return statsHash; // volatile
    }

    public int getEffectHash() {
        return effectHash; // volatile
    }

    public void setPowerCard(PowerCard card) {
        try {
            lock.lock();
            var oldPowers = activePowers
                    .stream()
                    .filter(p -> p.getCard() == this.powerCard).toList();

            activePowers.removeAll(oldPowers);
            this.powerCard = getPowerCard();
        } finally {
            lock.unlock();
        }
    }

    public int getCardCount() {
        try {
            lock.lock();
            int i = 0;

            if (abilityCard1 != null) { ++i; }
            if (abilityCard2 != null) { ++i; }
            if (actionCard1 != null) { ++i; }
            if (actionCard2 != null) { ++i; }
            return i;
        } finally {
            lock.unlock();
        }
    }

}
