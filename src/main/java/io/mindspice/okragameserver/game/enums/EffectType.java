package io.mindspice.okragameserver.game.enums;

public enum EffectType {


    // TODO may add a resurrection chance effect
    // Sleep needs an amount set for cures to work right 150 is a good default
    PARALYSIS(false, false, null, EffectClass.CURSE, true, "",350),
    POISON(false, false, StatType.HP, EffectClass.CURSE, true, "", 350),
    SLEEP(false, false, null, EffectClass.CURSE, true, "", 350),
    CONFUSION(false, false, null, EffectClass.CURSE, true, "", 350),
    INSIGHT_STATUS(true, false, null, EffectClass.INSIGHT, false, "",1),
    INSIGHT_HAND(true, false, null, EffectClass.INSIGHT, false, "",1),
    INSIGHT_ALL_DECKS(true, false, null, EffectClass.INSIGHT, false, "",1),
    INSIGHT_ACTION_DECK(true, false, null, EffectClass.INSIGHT, false, "", 1),
    INSIGHT_ABILITY_DECK(true, false, null, EffectClass.INSIGHT, false, "", 1),
    FORTUNE(true, true, StatType.LP, EffectClass.MODIFIER, false, "",50),
    MISFORTUNE(false, true, StatType.LP, EffectClass.MODIFIER, true, "",50),
    FORTIFY(true, true, StatType.DP, EffectClass.MODIFIER, false, "",1),
    DE_FORTIFY(false, true, StatType.DP, EffectClass.MODIFIER, true, "",1),
    MEDITATE(true, true, StatType.MP, EffectClass.MODIFIER, false, "",1),
    DE_MEDITATE(false, true, StatType.MP, EffectClass.MODIFIER, true, "",1),
    CORE(true, true, StatType.SP, EffectClass.MODIFIER, false, "",1),
    DE_CORE(false, true, StatType.SP, EffectClass.MODIFIER, true, "",1),
    INVIGORATE(true, true, StatType.HP, EffectClass.MODIFIER, false, "",1),
    DE_INVIGORATE(false, true, StatType.HP, EffectClass.MODIFIER, true, "",1),
    WILLFULNESS(true, true, StatType.WP, EffectClass.MODIFIER, false, "",100),
    FEEBLENESS(false, true, StatType.WP, EffectClass.MODIFIER, true, "", 100),
    HEAL(true, false, StatType.HP, EffectClass.CURE, false, "",1),
    AWAKEN(true, false, null, EffectClass.CURE, false, "SLEEP",1),
    CURE_POISON(true, false, null, EffectClass.CURE, false, "POISON",1),
    CLEAR_CONFUSION(true, false, null, EffectClass.CURE, false, "CONFUSION",1),
    HEAL_PARALYSIS(true, false, null, EffectClass.CURE, false, "PARALYSIS",1),
    CURE_ANY(true,false,null,EffectClass.CURE, false, "ALL", 1),
    SIPHON_HEALTH(true, false, StatType.HP, EffectClass.SIPHON, true, "",1),
    SIPHON_DEFENSE(true, false, StatType.DP, EffectClass.SIPHON, true, "",1),
    SIPHON_MANA(true, false, StatType.MP, EffectClass.SIPHON, true, "",1),
    SIPHON_STRENGTH(true, false, StatType.SP, EffectClass.SIPHON, true, "",1),
    SIPHON_LUCK(true, false, StatType.LP, EffectClass.SIPHON, true, "",1);

    public final boolean isPlayer;
    public final boolean isInit;
    public final StatType statType;
    public final EffectClass effectClass;
    public final boolean isNegative;
    public final String cureType; //Kind of hackish used for bot FIXME why does bot need string?
    public final int scalar; // Used for strength calculations, when sending to player;


    EffectType(boolean isPlayer, boolean isInit,
               StatType statType, EffectClass effectClass, boolean isNegative, String cureType, int scalar) {
        this.isPlayer = isPlayer;
        this.isInit = isInit;
        this.statType = statType;
        this.effectClass = effectClass;
        this.isNegative = isNegative;
        this.cureType = cureType;
        this.scalar = scalar;
    }

    public enum EffectClass {
        MODIFIER,
        SIPHON,
        CURSE,
        CURE,
        INSIGHT;
    }


    //WILL power damage effect lower regen
}
