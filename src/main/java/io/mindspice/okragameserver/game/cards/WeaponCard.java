package io.mindspice.okragameserver.game.cards;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.enums.*;
import io.mindspice.okragameserver.game.gameroom.action.ActionReturn;
import io.mindspice.okragameserver.game.gameroom.action.StatMap;
import io.mindspice.okragameserver.game.gameroom.action.logic.*;
import io.mindspice.okragameserver.game.gameroom.effect.Effect;
import io.mindspice.okragameserver.game.gameroom.state.PlayerGameState;
import io.mindspice.okragameserver.util.CardUtil;

import static io.mindspice.okragameserver.game.enums.ActionClass.*;
import static io.mindspice.okragameserver.game.enums.ActionType.*;
import static io.mindspice.okragameserver.game.enums.Alignment.*;
import static io.mindspice.okragameserver.game.enums.EffectType.*;


public enum WeaponCard implements Card {
    ///////////
    // MELEE //
    ///////////
    OKRITHRIAL_GLADIUS(new CardStats.Builder(CollectionSet.OKRA, MELEE, 2, CHAOS)
            .setCost(new StatMap(0, 0, 80, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(225, 0, 0, 0, 1, 1, 0, 0))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, MISFORTUNE, true, 5, .35, true, .2, 1, 0, 0),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setNegSelfEffects(new Effect[]{
                    new Effect(MULTI, MISFORTUNE, true, 5, .55, true, .2, 1, 0, 0),
            })
            .setNegSelfEffectLogic(EffectCalc.BasicSelfNeg.GET(1), EffectLogic.Basic.GET)
            .setAnimation(AnimType.MELEE, WeaponSprite.OKRITHRIAL_GLADIUS)
            .setDescription("An ancient enchanted gladius crafted from okranite, a formidable weapon with a sharp edge and a strong enchantment." +
                    "Due to degradation over time the enchantment has aligned with chaos and use poses risk of self effects for the" +
                    "novice. Awarded to Okra Folk Holders")
            .build()),
    OKRITHRIAL_GLADIUS_GOLD(new CardStats.Builder(CollectionSet.OKRA, MELEE, 3, ORDER)
            .setCost(new StatMap(0, 0, 90, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(165, 0, 0, 0, 1, 1, 0, 0))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, MISFORTUNE, true, 5, .45, true, .25, 1, 0, 0),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(AnimType.MELEE, WeaponSprite.OKRITHRIAL_GLADIUS)
            .setDescription("An ancient enchanted gladius crafted from okranite, a formidable weapon with a strong enchantment." +
                    "One of the few quality artifacts from it's time that have maintained alignment with order, " +
                    "though it's edge is not as sharp. Awarded to Okra Folk Holders")
            .build()),
    MJOLNIR_HAMMER(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 2, CHAOS)
            .setCost(new StatMap(0, 0, 80, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(150, 0, 0, 0, 1, 1, .5, .75))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, DE_FORTIFY, true, 50, .75, true, .4, 1, .22, 1),
                    new Effect(MULTI, DE_CORE, true, 50, .75, true, .4, 1, .22, 1)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(AnimType.MELEE, WeaponSprite.MJOLNIR_HAMMER)
            .setDescription("Delivers a thunderous blow to your enemy, sending reverberations through the ground and " +
                    "rattling their armor and the armor of those near by.")
            .build()),
    MJOLNIR_HAMMER_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 3, CHAOS)
            .setCost(new StatMap(0, 0, 110, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(180, 0, 0, 0, 1, 1, .65, .75))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, DE_FORTIFY, true, 70, .75, true, .4, 1, .22, 1),
                    new Effect(MULTI, DE_CORE, true, 70, .75, true, .4, 1, .22, 1)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(AnimType.MELEE, WeaponSprite.MJOLNIR_HAMMER)
            .setDescription("Delivers a thunderous blow to your enemy, sending reverberations through the ground and " +
                    "rattling their armor and the armor those near by. Gold variant with more damage and thunder for a higher cost.")
            .build()),
    CUTLASS_OF_FURY(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 2, ORDER)
            .setCost(new StatMap(0, 0, 75, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(80, 0, 0, 0, .7, 1, 0, 0))
            .setDamageLogic(DamageCalc.BasicTarget.GET(7), DamageLogic.Basic.GET)
            .setAnimation(AnimType.MELEE, WeaponSprite.CUTLASS_OF_FURY)
            .setDescription("A swift and effective weapon, dealing a series of moderate slashing blows to your enemy for a low cost.")
            .build()),
    CUTLASS_OF_FURY_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 2, ORDER)
            .setCost(new StatMap(0, 0, 90, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(160, 0, 0, 0, .7, 1, 0, 0))
            .setDamageLogic(DamageCalc.BasicTarget.GET(4), DamageLogic.Basic.GET)
            .setAnimation(AnimType.MELEE, WeaponSprite.CUTLASS_OF_FURY)
            .setDescription("A swift and effective weapon, dealing a small series of strong slashing blows to your enemy for a moderate cost.")
            .build()),
    ZWEIHANDER_OF_EXCELLENCE(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 2, ORDER)
            .setCost(new StatMap(0, 0, 80, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(260, 0, 0, 0, 1.1, 1, 0, 0))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setAnimation(AnimType.MELEE, WeaponSprite.ZWEIHANDER_OF_EXCELLENCE)
            .setDescription("A excellently crafted long sword, delivering a devastating and highly successful blow to your enemy.")
            .build()),
    ZWEIHANDER_OF_EXCELLENCE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 4, ORDER)
            .setCost(new StatMap(0, 0, 140, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(300, 0, 0, 0, 1.1, 1, 0, 0))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSpecial(SpecialAction.IGNORE_DP)
            .setAnimation(AnimType.MELEE, WeaponSprite.ZWEIHANDER_OF_EXCELLENCE)
            .setDescription("A excellently crafted long sword, delivering a devastating and highly successful blow to your enemy." +
                    " Crafted by an exceptionally skilled sword smith this gold variant can bypass and penetrate most defenses.")
            .build()),
    DUMBBRINGERS_CUDGEL(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 2, CHAOS)
            .setCost(new StatMap(0, 0, 90, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(250, 0, 0, 0, 1, 1, 0, 0))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, PARALYSIS, true, .5, .65, true, .22, 1, 0, 0)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(AnimType.MELEE, WeaponSprite.DUMBBRINGERS_CUDGEL)
            .setDescription("A crude, brutal and effective weapon, dealing a strong concussive blow with a chance of confusion and possibly paralysis.")
            .build()),
    DUMBBRINGERS_CUDGEL_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 3, CHAOS)
            .setCost(new StatMap(0, 0, 120, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(300, 0, 0, 0, 1, 1, 0, 0))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, PARALYSIS, true, .5, .65, true, .35, 1, 0, 0)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(AnimType.MELEE, WeaponSprite.DUMBBRINGERS_CUDGEL)
            .setDescription("A crude, brutal and effective weapon, dealing a strong concussive blow with a chance of paralysis. " +
                    "Gold variant uses more strength for higher damage and chance of paralysis.")
            .build()),
    KNUCKLEDUSTER(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 2, CHAOS)
            .setCost(new StatMap(0, 0, 80, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(35, 0, 0, 0, .5, 1, 0, 0))
            .setDamageLogic(DamageCalc.BasicTarget.GET(20), DamageLogic.Basic.GET)
            .setAnimation(AnimType.MELEE, WeaponSprite.NONE)
            .setDescription("Choosing a chaotic route of pure hand to hand violence you strike with a flurry of punches " +
                    "using your trusted set of knuckledusters.")
            .build()),
    KNUCKLEDUSTER_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 3, CHAOS)
            .setCost(new StatMap(0, 0, 120, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(40, 0, 0, 0, .5, 1, 0, 0))
            .setDamageLogic(DamageCalc.BasicTarget.GET(20), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, CONFUSION, true, .25, .5, true, .15, 1, 0, 0)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(20), EffectLogic.Basic.GET)
            .setAnimation(AnimType.MELEE, WeaponSprite.NONE)
            .setDescription("Choosing a chaotic route of pure hand to hand violence you strike with a flurry of punches " +
                    "using your trusted set of knuckledusters. Putting more strength behind your punches each blow has a " +
                    "chance of dealing confusion.")
            .build()),
    STONEWALL_PAVISE(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 2, ORDER)
            .isPlayer()
            .setCost(new StatMap(0, 0, 125, 0))
            .setCostLogic(Cost.GET)
            .setPosSelfEffects(new Effect[]{
                    new Effect(SINGLE, FORTIFY, true, 250, 2, false, 1.25, 1, 0, 0)
            })
            .setPosSelfEffectLogic(EffectCalc.BasicSelfPos.GET(1), EffectLogic.Basic.GET)
            .setAnimation(AnimType.SHIELD, WeaponSprite.STONEWALL_PAVISE)
            .setDescription("Equipping your trusted pavise off your back, you brace your self for incoming damage, boosting your defense for 2 rounds.")
            .build()),
    STONEWALL_PAVISE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 4, ORDER)
            .isPlayer()
            .setCost(new StatMap(0, 0, 250, 0))
            .setCostLogic(Cost.GET)
            .setPosSelfEffects(new Effect[]{
                    new Effect(SINGLE, FORTIFY, true, 250, 4, false, 1.25, 1, 0, 0)
            })
            .setPosSelfEffectLogic(EffectCalc.BasicSelfPos.GET(1), EffectLogic.Basic.GET)
            .setAnimation(AnimType.SHIELD, WeaponSprite.STONEWALL_PAVISE)
            .setDescription("Equipping your trusted pavise off your back, you brace your self for incoming damage, boosting your defense for 4 rounds.")
            .build()),
    ////////////
    // RANGED //
    ////////////
    IMPERIAL_CROSSBOW(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 2, ORDER)
            .setCost(new StatMap(0, 0, 80, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(200, 0, 0, 0, 1, 1, 0, 0))
            .setDamageLogic(DamageCalc.VitalChance.GET(0.5, 1), DamageLogic.Basic.GET)
            .setAnimation(AnimType.ARROW_SINGLE, WeaponSprite.IMPERIAL_CROSSBOW)
            .setDescription("A standard issue imperial crossbow, deals fair damage with a 50/50 chance of a vital hit bypassing most defenses.")
            .build()),
    IMPERIAL_CROSSBOW_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 3, ORDER)
            .setCost(new StatMap(0, 0, 120, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(275, 0, 0, 0, 1, 1, 0, 0))
            .setDamageLogic(DamageCalc.VitalChance.GET(0.5, 1), DamageLogic.Basic.GET)
            .setAnimation(AnimType.ARROW_SINGLE, WeaponSprite.IMPERIAL_CROSSBOW)
            .setDescription("A standard issue imperial crossbow, deals fair damage with a 50/50 chance of a vital hit bypassing most defenses." +
                    " Gold variant deals higher damage for a higher cost.")
            .build()),
    SHADOW_DAGGER(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 3, CHAOS)
            .setCost(new StatMap(0, 0, 80, 40))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(500, 0, 0, 0, .45, 1, 0, 0))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSelfDamage(SINGLE, new StatMap(500, 0, 0, 0, .55, 1, 0, 0))
            .setSpecial(SpecialAction.IGNORE_DP)
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(1), DamageLogic.Basic.GET)
            .setAnimation(AnimType.SHADOW_STRIKE, WeaponSprite.SHADOW_DAGGER)
            .setDescription("Casting an aura of cloaking you attempt to use this dagger to deal a devastating wound to " +
                    "your opponents weakly armored back, bypassing most defenses.")
            .build()),
    SHADOW_DAGGER_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 3, CHAOS)
            .setCost(new StatMap(0, 0, 120, 60))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(800, 0, 0, 0, .45, 1, 0, 0))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSelfDamage(SINGLE, new StatMap(800, 0, 0, 0, .55, 1, 0, 0))
            .setSpecial(SpecialAction.IGNORE_DP)
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(1), DamageLogic.Basic.GET)
            .setAnimation(AnimType.SHADOW_STRIKE, WeaponSprite.SHADOW_DAGGER)
            .setDescription("Casting an aura of cloaking you attempt to use this dagger to deal a devastating wound to " +
                    "your opponents weakly armored back, bypassing most defenses. Gold variant, deals more damage, but at " +
                    "a higher risk, may luck be on your side.")
            .build()),
    BOW_OF_SWIFTNESS(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 2, CHAOS)
            .setCost(new StatMap(0, 0, 85, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(30, 0, 0, 0, .4, 1, .5, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(0.2, 10), DamageLogic.Basic.GET)
            .setAnimation(AnimType.ARROW_SINGLE, WeaponSprite.BOW_OF_SWIFTNESS)
            .setDescription("Light bow allowing you to release a swift volley of arrows upon the field. Damages multiple " +
                    "enemies with a low chance of a vital hit.")
            .build()),
    BOW_OF_SWIFTNESS_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 3, CHAOS)
            .setCost(new StatMap(0, 0, 130, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(50, 0, 0, 0, .4, 1, .5, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(0.25, 10), DamageLogic.Basic.GET)
            .setAnimation(AnimType.ARROW_SINGLE, WeaponSprite.BOW_OF_SWIFTNESS)
            .setDescription("Light bow allowing you to release a swift volley of arrows upon the field. Damages multiple " +
                    "enemies. Gold variant with higher cost, but more damage and higher chance of a vital hit.")
            .build()),
    JAVELIN_OF_MIGHT(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 3, ORDER)
            .setCost(new StatMap(0, 0, 100, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(350, 0, 0, 0, .65, 1, 0, 0))
            .setDamageLogic(DamageCalc.VitalChance.GET(0.8, 1), DamageLogic.Basic.GET)
            .setAnimation(AnimType.JAVELIN, WeaponSprite.NONE)
            .setDescription("A large and mighty javelin, not the most accurate, but a high chance of a vital hit if it lands.")
            .build()),
    JAVELIN_OF_MIGHT_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 4, ORDER)
            .setCost(new StatMap(0, 0, 125, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(400, 0, 0, 0, .65, 1, 0, 0))
            .setDamageLogic(DamageCalc.VitalChance.GET(1.25, 1), DamageLogic.Basic.GET)
            .setAnimation(AnimType.JAVELIN, WeaponSprite.NONE)
            .setDescription("A large and mighty javelin, not the most accurate, but a high chance of a vital hit if it lands." +
                    " Excellently crafted gold variant with a higher cost, but more damage and a guarantee vital hit if it lands.")
            .build()),
    BOW_OF_POISONING(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 2, CHAOS)
            .setCost(new StatMap(0, 0, 70, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(175, 0, 0, 0, 1, 1, 0, 0))
            .setDamageLogic(DamageCalc.VitalChance.GET(0.5, 1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, POISON, true, 50, .65, true, .5, 1, 0, 0)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(AnimType.ARROW_POISON_SINGLE, WeaponSprite.BOW_OF_POISONING)
            .setDescription("A simple bow, firing arrows dipped in a weak poison and a small chance of a vital hit.")
            .build()),
    BOW_OF_POISONING_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 3, CHAOS)
            .setCost(new StatMap(0, 0, 90, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(200, 0, 0, 0, 1, 1, 0, 0))
            .setDamageLogic(DamageCalc.VitalChance.GET(0.5, 1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, POISON, true, 50, .5, true, .6, 1, 0, 0)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(AnimType.ARROW_POISON_SINGLE, WeaponSprite.BOW_OF_POISONING)
            .setDescription("A simple bow, firing arrows dipped in a moderate poison and a small chance of a vital hit.")
            .build()),
    LONGBOW_OF_EXCELLENCE(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 2, ORDER)
            .setCost(new StatMap(0, 0, 120, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(240, 0, 0, 0, 1, 1, 0, 0))
            .setDamageLogic(DamageCalc.VitalChance.GET(1, 1), DamageLogic.Basic.GET)
            .setAnimation(AnimType.ARROW_SINGLE, WeaponSprite.LONGBOW_OF_EXCELLENCE)
            .setDescription("An excellently crafted long bow dealing high damage and a near guaranteed vital hit.")
            .build()),
    LONGBOW_OF_EXCELLENCE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 2, ORDER)
            .setCost(new StatMap(0, 0, 150, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(330, 0, 0, 0, 1, 1, 0, 0))
            .setDamageLogic(DamageCalc.VitalChance.GET(1, 1), DamageLogic.Basic.GET)
            .setAnimation(AnimType.ARROW_SINGLE, WeaponSprite.LONGBOW_OF_EXCELLENCE)
            .setDescription("An excellently crafted long bow dealing high damage and a near guaranteed vital hit." +
                    " Gold variant, over draw for more damage.")
            .build()),
    ///////////
    // MAGIC //
    ///////////
    STAFF_OF_HEALING(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, ORDER)
            .setCost(new StatMap(0, 0, 0, 100))
            .setCostLogic(Cost.GET)
            .setPosSelfTargetEffects(new Effect[]{
                    new Effect(SINGLE, HEAL, true, 250, 1, true, 1, 1, 0, 0),
            })
            .setPosSelfTargetEffectLogic(EffectCalc.BasicSelfPosTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(AnimType.HEAL, WeaponSprite.STAFF_OF_HEALING)
            .setDescription("Casts healing upon you or a teammate.")
            .isPlayer()
            .build()),
    STAFF_OF_HEALING_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, ORDER)
            .setCost(new StatMap(0, 0, 0, 160))
            .setCostLogic(Cost.GET)
            .setPosSelfTargetEffects(new Effect[]{
                    new Effect(SINGLE, HEAL, true, 400, 1, true, 1, 1, 0, 0),
            })
            .setPosSelfTargetEffectLogic(EffectCalc.BasicSelfPosTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(AnimType.HEAL, WeaponSprite.STAFF_OF_HEALING)
            .setDescription("Casts healing upon you or a teammate, gold variant with more healing power for a higher cost. ")
            .isPlayer()
            .build()),
    STAFF_OF_ROUGE_SIPHON(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, CHAOS)
            .setCost(new StatMap(0, 0, 0, 125))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, SIPHON_HEALTH, true, 175, 1, true, .4, 1, 0, 0),
                    new Effect(SINGLE, SIPHON_MANA, true, 70, 1, true, .4, 1, 0, 0),
                    new Effect(SINGLE, SIPHON_DEFENSE, true, 70, 1, true, .4, 1, 0, 0),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setNegSelfEffects(new Effect[]{
                    new Effect(SINGLE, DE_INVIGORATE, true, 2, .7, true, .3, 1, 0, 0),
                    new Effect(SINGLE, MISFORTUNE, true, 2, .7, true, .3, 1, 0, 0),
            })
            .setNegSelfEffectLogic(EffectCalc.BasicSelfNeg.GET(1), EffectLogic.Basic.GET)
            .setAnimation(AnimType.SIPHON_ROUGE, WeaponSprite.STAFF_OF_ROUGE_SIPHON)
            .setDescription("A chaotic and rouge staff, capable of siphoning multiple attributes, at the risk of a light curse.")
            .build()),
    STAFF_OF_ROUGE_SIPHON_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 4, CHAOS)
            .setCost(new StatMap(0, 0, 0, 175))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, SIPHON_HEALTH, true, 200, 1, true, .4, 1, 0, 0),
                    new Effect(SINGLE, SIPHON_MANA, true, 100, 1, true, .4, 1, 0, 0),
                    new Effect(SINGLE, SIPHON_DEFENSE, true, 100, 1, true, .4, 1, 0, 0),
                    new Effect(SINGLE, SIPHON_LUCK, true, 2, 1, true, .25, 1, 0, 0),
                    new Effect(SINGLE, SIPHON_STRENGTH, true, 100, 1, true, .4, 1, 0, 0),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setNegSelfEffects(new Effect[]{
                    new Effect(SINGLE, DE_INVIGORATE, true, 2, .67, true, .3, 1, 0, 0),
                    new Effect(SINGLE, MISFORTUNE, true, 2, .67, true, .3, 1, 0, 0),
                    new Effect(SINGLE, DE_MEDITATE, true, 125, .67, true, .3, 1, 0, 0),
            })
            .setNegSelfEffectLogic(EffectCalc.BasicSelfNeg.GET(1), EffectLogic.Basic.GET)
            .setAnimation(AnimType.SIPHON_ROUGE, WeaponSprite.STAFF_OF_ROUGE_SIPHON)
            .setDescription("A chaotic and rouge staff, capable of siphoning many attributes, at the risk of a moderate curse.")
            .build()),
    GLOVES_OF_INFERNO(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, ORDER)
            .setCost(new StatMap(0, 0, 0, 75))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(110, 0, 0, 0, .5, 1, .5, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(2), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, DE_FORTIFY, true, 75, .7, true, .4, 1, .4, 1)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(AnimType.FLAMES, WeaponSprite.NONE)
            .setDescription("Cast a scorching blast across the land, dealing moderate damage to health and a chance of lowering defense.")
            .build()),
    GLOVES_OF_INFERNO_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, ORDER)
            .setCost(new StatMap(0, 0, 0, 130))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(165, 0, 0, 0, .65, 1, .65, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(2), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, DE_FORTIFY, true, 100, .7, true, .4, 1, .4, 1)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(AnimType.FLAMES, WeaponSprite.NONE)
            .setDescription("Cast a scorching blast across the land, dealing moderate damage to health and a chance of lowering defense. " +
                    "Gold variant with higher damage.")
            .build()),
    STAFF_OF_FORTILITY(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, ORDER)
            .setCost(new StatMap(0, 0, 0, 120))
            .setCostLogic(Cost.GET)
            .setPosSelfTargetEffects(new Effect[]{
                    new Effect(SINGLE, FORTIFY, true, 125, 2, false, 1, 1)
            })
            .setPosSelfTargetEffectLogic(EffectCalc.BasicSelfPosTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(AnimType.BUFF, WeaponSprite.STAFF_OF_FORTILITY)
            .setDescription("Imparts you or a teammate moderately increased defense for 2 rounds")
            .isPlayer()
            .build()),
    STAFF_OF_FORTILITY_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 4, ORDER)
            .setCost(new StatMap(0, 0, 0, 175))
            .setCostLogic(Cost.GET)
            .setPosSelfTargetEffects(new Effect[]{
                    new Effect(SINGLE, FORTIFY, true, 200, 3, false, 1, 1)
            })
            .setPosSelfTargetEffectLogic(EffectCalc.BasicSelfPosTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(AnimType.BUFF, WeaponSprite.STAFF_OF_FORTILITY)
            .setDescription("Imparts you or a teammate highly increased defense for 3 rounds")
            .build()),
    STAFF_OF_THUNDERBOLT(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, ORDER)
            .setCost(new StatMap(0, 0, 0, 80))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(250, 0, 0, 0, 1, 1, 0, 0))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, DE_CORE, true, 100, .75, true, .35, 1, 0, 0)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(AnimType.BOLT_ELECTRIC, WeaponSprite.STAFF_OF_THUNDERBOLT)
            .setDescription("Light and simple staff that deals moderate electric damage with a chance of lowering core strength.")
            .build()),
    STAFF_OF_THUNDERBOLT_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, ORDER)
            .setCost(new StatMap(0, 0, 0, 110))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(325, 0, 0, 0, 1, 1, 0, 0))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, DE_CORE, true, 100, .7, true, .45, 1, 0, 0)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(AnimType.BOLT_ELECTRIC, WeaponSprite.STAFF_OF_THUNDERBOLT)
            .setDescription("Light and simple staff that deals moderate electric damage with a chance of lowering core strength." +
                    " Gold variant, deals higher damage and higher chance of strength de buff for a higher cost")
            .build()),
    SCROLL_OF_MOLTEN_RAIN(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 1, CHAOS)
            .setCost(new StatMap(0, 0, 0, 70))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(60, 0, 0, 0, 0.035, 1, 0.035, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(100), DamageLogic.Basic.GET)
            .setSelfDamage(MULTI, new StatMap(60, 0, 0, 0, 0.015, 1, 0.015, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(100), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, DE_MEDITATE, true, 10, .6, true, .035, 1, .035, 1)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(100), EffectLogic.Basic.GET)
            .setNegSelfEffects(new Effect[]{
                    new Effect(MULTI, DE_MEDITATE, true, 10, .6, true, .015, 1, .015, 1)
            })
            .setNegSelfEffectLogic(EffectCalc.BasicSelfNeg.GET(100), EffectLogic.Basic.GET)
            .setAnimation(AnimType.MOLTEN_RAIN, WeaponSprite.NONE)
            .setDescription("A tattered and forgot scroll recovered from an ancient tomb, causes a downpour of molten rain causing damage and de meditation. " +
                    "Due to its chaotic nature no one is safe.")
            .build()),
    SCROLL_OF_MOLTEN_RAIN_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 1, CHAOS)
            .setCost(new StatMap(0, 0, 0, 110))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(60, 0, 0, 0, 0.0525, 1, 0.0525, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(100), DamageLogic.Basic.GET)
            .setSelfDamage(MULTI, new StatMap(60, 0, 0, 0, 0.0225, 1, 0.0225, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(100), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, DE_MEDITATE, true, 10, .5, true, .0525, 1, .0525, 1)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(100), EffectLogic.Basic.GET)
            .setNegSelfEffects(new Effect[]{
                    new Effect(MULTI, DE_MEDITATE, true, 10, .5, true, .0225, 1, .0225, 1)
            })
            .setNegSelfEffectLogic(EffectCalc.BasicSelfNeg.GET(100), EffectLogic.Basic.GET)
            .setAnimation(AnimType.MOLTEN_RAIN, WeaponSprite.NONE)
            .setDescription("A tattered and forgot scroll recovered from an ancient tomb, causes a downpour of molten rain causing damage and de meditation. " +
                    "Due to its chaotic nature no one is safe. Gold variant, higher cost, more damage, more chaos.")
            .build());

    private final CardStats stats;
    private String uid = null;
    public static final String prefix = "WEP";

    WeaponCard(CardStats stats) {
        this.stats = stats;
    }

    public CardStats getStats() {
        return stats;
    }

    public int getLevel() {
        return stats.getLevel();
    }

    @Override
    public ActionReturn playCard(PlayerGameState player, PlayerGameState target, PawnIndex playerIdx, PawnIndex targetIdx) {
        return playCard(player, target, playerIdx, targetIdx, stats);
    }

    public String toUID() {
        return "";
    }

    public String getUid() {
        if (uid == null) { this.uid = prefix + "-" + CardUtil.getHash(this.name()); }
        return uid;
    }

    public String getName() {
        return this.name();
    }

    public ObjectNode toJson() {
        var json = JsonUtils.getMapper();
        var obj = json.createObjectNode();
        obj.put("uid", getUid());
        obj.put("is_gold", this.toString().contains("GOLD"));
        obj.put("is_player", stats.isPlayer());
        obj.put("damage_class", stats.getDamageClass() != null ? stats.getDamageClass().toString() : "N/A");
        obj.put("weapon_sprite", stats.getAnimation().split(":")[1]);
        obj.put("alignment", stats.getAlignment().toString());
        obj.put("action_type", stats.getActionType().toString());
        obj.put("self_damage_class", stats.getSelfDamageClass() != null ? stats.getSelfDamageClass().toString() : "N/A");
        obj.put("special", stats.getSpecial() != null ? stats.getSpecial().toString() : "N/A");
        obj.put("animation", stats.getAnimation() != null ? stats.getAnimation().toString() : "N/A");
        obj.put("level", stats.getLevel());
        obj.putIfAbsent("cost", stats.getCost().toJson());
        obj.putIfAbsent("damage", stats.getDamage().toJson());
        obj.put("enemy_damage_reps", stats.getDamageCalc() != null ? stats.getDamageCalc().getMulti() : 0);
        obj.put("self_damage_reps", stats.getSelfDamageCalc() != null ? stats.getSelfDamageCalc().getMulti() : 0);
        obj.put("enemy_effect_reps", stats.getTargetEffectCalc() != null ? stats.getTargetEffectCalc().getMulti() : 0);
        obj.put("self_effect_reps_neg", stats.getNegSelfEffectCalc() != null ? stats.getNegSelfEffectCalc().getMulti() : 0);
        obj.put("self_effect_reps_pos", stats.getPosSelfEffectCalc() != null ? stats.getPosSelfEffectCalc().getMulti() : 0);
        var eff = json.createObjectNode();
        if (stats.getTargetEffects() != null) {
            for (int i = 0; i < stats.getTargetEffects().length; ++i) {
                eff.putIfAbsent(("effect" + i), stats.getTargetEffects()[i].toJson());
            }
        }
        obj.putIfAbsent("effects", eff);
        var seff = json.createObjectNode();
        if (stats.getNegSelfEffects() != null) {
            for (int i = 0; i < stats.getNegSelfEffects().length; ++i) {
                seff.putIfAbsent(("effect" + i), stats.getNegSelfEffects()[i].toJson());
            }
        }
        if (stats.getPosSelfEffects() != null) {
            for (int i = 0; i < stats.getPosSelfEffects().length; ++i) {
                seff.putIfAbsent(("effect" + i), stats.getPosSelfEffects()[i].toJson());
            }
        }
        obj.putIfAbsent("self_effects", seff);
        if (stats.getSelfDamage() != null) {
            obj.putIfAbsent("self_damage", stats.getSelfDamage().toJson());
        } else {
            obj.put("self_damage", "null");
        }
        obj.put("description", stats.getDescription());
        return obj;
    }
}