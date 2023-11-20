package io.mindspice.okragameserver.game.cards;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.gameroom.action.ActionReturn;
import io.mindspice.okragameserver.game.gameroom.action.StatMap;
import io.mindspice.okragameserver.game.gameroom.action.logic.*;
import io.mindspice.okragameserver.game.enums.*;
import io.mindspice.okragameserver.game.gameroom.effect.Effect;
import io.mindspice.okragameserver.game.gameroom.state.PlayerGameState;
import io.mindspice.okragameserver.util.CardUtil;

import static io.mindspice.okragameserver.game.enums.ActionClass.MULTI;
import static io.mindspice.okragameserver.game.enums.ActionClass.SINGLE;
import static io.mindspice.okragameserver.game.enums.ActionType.*;
import static io.mindspice.okragameserver.game.enums.Alignment.*;
import static io.mindspice.okragameserver.game.enums.EffectType.*;
import static io.mindspice.okragameserver.game.enums.SpecialAction.IGNORE_DP;
import static io.mindspice.okragameserver.game.enums.SpecialAction.IGNORE_MP;


public enum ActionCard implements Card {
    ///////////
    /* MELEE */
    ///////////
    TRIPLE_STRIKE(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 3, ORDER)
            .setCost(new StatMap(0, 0, 120, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(200, 0, 0, 0, 1, 1, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.MELEE_SINGLE, AnimTime.MEDIUM,AnimSpeed.NORMAL))
            .setDescription("A strong attack highly successful against all of your opponents pawns. Low on damage," +
                    " but a highly effective attack against multiple pawns, for a low cost.")
            .build()),
    TRIPLE_STRIKE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 4, ORDER)
            .setCost(new StatMap(0, 0, 150, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(225, 0, 0, 0, 1, 1, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSpecial(IGNORE_DP)
            .setAnimation(Animation.build(AnimType.MELEE_SINGLE, AnimTime.MEDIUM,AnimSpeed.NORMAL))
            .setDescription("A focused devastating strike against all your opponents standing pawns. Delivers a highly trained " +
                    "and targeted blows that bypasses m+_ost defensive protections. Gold variety, higher cost, more devastation.")
            .build()),
    SHATTER(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 2, ORDER)
            .setCost(new StatMap(0, 0, 90, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(0, 80, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.SHATTER, AnimTime.LONG,AnimSpeed.NORMAL))
            .setDescription("A strong blunt blow, rendering those on the receiving ends armour less affective.")
            .build()),
    SHATTER_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 3, ORDER)
            .setCost(new StatMap(0, 0, 140, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(0, 120, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.SHATTER, AnimTime.LONG,AnimSpeed.NORMAL))
            .setDescription("A strong blunt blow, rendering those on the receiving ends armour less affective. " +
                    "Gold variety with a higher cost and more damage.")
            .build()),
    MIGHT_DRAIN(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 2, ORDER)
            .setCost(new StatMap(0, 0, 75, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(0, 0, 80, 0, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.MELEE_DRAIN, AnimTime.LONG,AnimSpeed.NORMAL))
            .setDescription("A target blow to an enemies vital pressure point, rendering their strength lower.")
            .build()),
    MIGHT_DRAIN_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 3, ORDER)
            .setCost(new StatMap(0, 0, 90, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(0, 0, 110, 0, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.MELEE_DRAIN, AnimTime.LONG,AnimSpeed.NORMAL))
            .setDescription("A target blow to an enemies vital pressure point, rendering their strength lower. " +
                    "Gold variety higher cost for slightly higher damage")
            .build()),
    STOMP(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 3, ORDER)
            .setCost(new StatMap(0, 0, 140, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(0, 80, 0, 0, .65, 1, .65, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.STOMP, AnimTime.LONG,AnimSpeed.NORMAL))
            .setDescription("A Violent stomp sending a shockwave through the ground, causing devastating reverberations that " +
                    "lower all opposing pawns defenses.")
            .build()),
    STOMP_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 3, ORDER)
            .setCost(new StatMap(0, 0, 150, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(0, 80, 0, 0, .75, 1, .75, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.STOMP, AnimTime.LONG,AnimSpeed.NORMAL))
            .setDescription("A Violent stomp sending a shockwave through the ground, causing devastating reverberations that " +
                    "lower all opposing pawns defenses. Gold variety with slightly higher chance and higher cost.")
            .build()),
    WARRIORS_STOMP(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 3, ORDER)
            .setCost(new StatMap(0, 0, 160, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(0, 40, 60, 0, .65, 1, .65, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.STOMP, AnimTime.LONG,AnimSpeed.NORMAL))
            .setDescription("A Violent stomp channeling the might of all warriors of the past, sending a monumental shockwave" +
                    " through the ground, causing intense reverberations that lowers all opposing pawns defenses.")
            .build()),
    WARRIORS_STOMP_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 4, ORDER)
            .setCost(new StatMap(0, 0, 185, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(0, 50, 70, 0, .75, 1, .75, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.STOMP, AnimTime.LONG,AnimSpeed.NORMAL))
            .setDescription("A Violent stomp channeling the might of all warriors of the past, sending a monumental shockwave" +
                    " through the ground, causing intense reverberations that lowers all opposing pawns defenses. " +
                    "Gold variety with a lower cost and higher chance.")
            .build()),
    MULTI_STRIKE(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 2, ORDER)
            .setCost(new StatMap(0, 0, 80, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(200, 0, 0, 0, 1, 1, .5, .7))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.MELEE_SINGLE, AnimTime.MEDIUM,AnimSpeed.NORMAL))
            .setDescription("A strong offensive attack dealing average damage, attacking multiple pawns if the opportunity " +
                    "presents itself.")
            .build()),
    MULTI_STRIKE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 3, ORDER)
            .setCost(new StatMap(0, 0, 125, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(210, 0, 0, 0, 1, 1, .5, .7))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSpecial(IGNORE_DP)
            .setAnimation(Animation.build(AnimType.MELEE_SINGLE, AnimTime.MEDIUM, AnimSpeed.NORMAL))
            .setDescription("A strong offensive attack dealing average damage while avoiding most defenses, attacking" +
                    " multiple pawns if the opportunity  presents itself. Gold variety higher cost and immune to most defense.")
            .build()),
    SWIFT_STRIKE(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 1, ORDER)
            .setCost(new StatMap(0, 0, 60, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(220, 0, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.MELEE_SINGLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("A Swift, simple, direct strike, dealing average damage for a low cost.")
            .build()),
    SWIFT_STRIKE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 2, ORDER)
            .setCost(new StatMap(0, 0, 70, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(270, 0, 0, 0, 1.0, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.MELEE_SINGLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("A Swift, simple, direct strike, dealing average damage for a low cost. Gold variety" +
                    " with higher damage for a higher cost.")
            .build()),
    FOCUSED_STRIKE(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 3, ORDER)
            .setCost(new StatMap(0, 0, 90, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(300, 0, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSpecial(IGNORE_DP)
            .setAnimation(Animation.build(AnimType.MELEE_SINGLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("A direct and skillfully focused strike, it's precise nature bypasses most defenses.")
            .build()),
    FOCUSED_STRIKE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 4, ORDER)
            .setCost(new StatMap(0, 0, 120, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(410, 0, 0, 0, 1.0, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSpecial(IGNORE_DP)
            .setAnimation(Animation.build(AnimType.MELEE_SINGLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("A direct and skillfully focused strike, it's precise nature bypasses most defenses. " +
                    "Gold variety with a damage and cost.")
            .build()),
    FLURRY_STRIKE(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 1, ORDER)
            .setCost(new StatMap(0, 0, 60, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(75, 0, 0, 0, .5, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(6), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.MELEE_DUAL, AnimTime.NORMAL, AnimSpeed.EXTRA_FAST))
            .setDescription("While not the most accurate, you release a flurry of 6 strikes. Damage inflicted depends on your luck.")
            .build()),
    FLURRY_STRIKE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 2, ORDER)
            .setCost(new StatMap(0, 0, 80, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(75, 0, 0, 0, .35, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(12), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.MELEE_DUAL, AnimTime.NORMAL, AnimSpeed.EXTRA_FAST))
            .setDescription("While not the most accurate, you release a flurry of 12 strikes. Damage inflicted depends on your luck." +
                    "Gold variety, larger flurry with lower chance. May luck be willing.")
            .build()),
    TACTICAL_STRIKE(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 3, ORDER)
            .setCost(new StatMap(0, 0, 100, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(200, 45, 45, 0, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.MELEE_SINGLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("A strong attack on multiple fronts, dealing a fair amount of damage to health while also inflicting" +
                    " loss to the opponents strength and defense.")
            .build()),
    TACTICAL_STRIKE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 4, ORDER)
            .setCost(new StatMap(0, 0, 130, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(250, 65, 65, 0, 1.0, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.MELEE_SINGLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("A strong attack on multiple fronts, dealing a fair amount of damage to health while also inflicting" +
                    " loss to the opponents strength and defense. Gold variety higher cost with more damage.")
            .build()),
    ///////////
    // CHAOS //
    ///////////
    BLIND_FLURRY(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 3, CHAOS)
            .setCost(new StatMap(0, 0, 190, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(125, 0, 0, 0, .7, 1, .7, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(4), DamageLogic.Basic.GET)
            .setSelfDamage(MULTI, new StatMap(125, 0, 0, 0, 0, 1, .35, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(4), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.MELEE_CHAOS_DUAL, AnimTime.NORMAL, AnimSpeed.EXTRA_FAST))
            .setDescription("The spirit of chaos is channeled, leading to a devastating 4 strike flurry attack that decimates all in its path." +
                    " High risk of damage to both the opponent and ones own.")
            .build()),
    BLIND_FLURRY_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 4, CHAOS)
            .setCost(new StatMap(0, 0, 220, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(140, 0, 0, 0, .77, 1, .70, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(4), DamageLogic.Basic.GET)
            .setSelfDamage(MULTI, new StatMap(110, 0, 0, 0, 0, 0, .35, .9))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(4), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.MELEE_CHAOS_DUAL, AnimTime.NORMAL, AnimSpeed.EXTRA_FAST))
            .setDescription("The spirit of chaos is channeled, leading to a devastating 4 strike flurry attack that decimates all in its path." +
                    " High risk of damage to both the opponent and ones own. Gold variety with higher cost, and a " +
                    "slightly lower risk of team damage.")
            .build()),
    HARBINGER_OF_DEATH(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 4, CHAOS)
            .setCost(new StatMap(0, 0, 200, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(600, 0, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSelfDamage(SINGLE, new StatMap(400, 0, 0, 0, 1, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(1), DamageLogic.Basic.GET)
            .setSpecial(IGNORE_DP)
            .setAnimation(Animation.build(AnimType.MELEE_CHAOS_SINGLE, AnimTime.LONG, AnimSpeed.SLOW))
            .setDescription("A direct wrathful blow, damaging the opponent at all costs, even ones own health. Bypasses most defenses. ")
            .build()),
    HARBINGER_OF_DEATH_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 4, CHAOS)
            .setCost(new StatMap(0, 0, 220, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(600, 0, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSelfDamage(SINGLE, new StatMap(400, 0, 0, 0, .95, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(1), DamageLogic.Basic.GET)
            .setSpecial(IGNORE_DP)
            .setAnimation(Animation.build(AnimType.MELEE_CHAOS_SINGLE, AnimTime.LONG, AnimSpeed.SLOW))
            .setDescription("A direct wrathful blow, damaging the opponent at all costs, even ones own health. Bypasses most defenses." +
                    " Gold variety with a slightly higher cost, but less self risk.")
            .build()),
    DISINTEGRATE(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 3, CHAOS)
            .setCost(new StatMap(0, 0, 190, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(0, 190, 0, 0, .75, 1, 1, .75))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSelfDamage(MULTI, new StatMap(0, 135, 0, 0, .7, 1, .9, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.DISINTEGRATE, AnimTime.LONG, AnimSpeed.NORMAL))
            .setDescription("Channeling the voice of the chaos realm, your voice reverberates across the land, disintegrating " +
                    "the armour of all those around.")
            .build()),
    DISINTEGRATE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 4, CHAOS)
            .setCost(new StatMap(0, 0, 220, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(0, 200, 0, 0, .8, 1, 1, .8))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSelfDamage(MULTI, new StatMap(0, 150, 0, 0, .6, 1, .8, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.DISINTEGRATE, AnimTime.LONG, AnimSpeed.NORMAL))
            .setDescription("Channeling the voice of the chaos realm, your voice reverberates across the land, disintegrating " +
                    "the armour of all those around. Gold variety higher cost, more damage, and slightly less risk.")
            .build()),
    SACRIFICIAL_SLICE(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 1, CHAOS)
            .setCost(new StatMap(0, 0, 70, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(300, 50, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSelfDamage(SINGLE, new StatMap(100, 50, 0, 0, .75, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.MELEE_CHAOS_SINGLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("A strong direct attack dealing a sizable amount of health damage and a noticeable impact to defense. " +
                    "Risk of self damage from chaotic posture.")
            .build()),
    SACRIFICIAL_SLICE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 2, CHAOS)
            .setCost(new StatMap(0, 0, 90, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(340, 60, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSelfDamage(SINGLE, new StatMap(100, 50, 0, 0, .65, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.MELEE_CHAOS_SINGLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("A strong direct attack dealing a sizable amount of health damage and a noticeable impact to defense. " +
                    "Risk of self damage from chaotic posture. Gold variety with slightly lower self risk.")
            .build()),
    RED_EYE_BLIND(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 2, CHAOS)
            .setCost(new StatMap(0, 0, 90, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(140, 0, 0, 0, 1, 1, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSelfDamage(MULTI, new StatMap(100, 0, 0, 0, 0, 1, .6, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(1), DamageLogic.Basic.GET)
            .setSpecial(IGNORE_DP)
            .setAnimation(Animation.build(AnimType.MELEE_CHAOS_SINGLE, AnimTime.NORMAL, AnimSpeed.FAST))
            .setDescription("A sizable attack upon multiple opponents with high success, chaotic energy also means mild " +
                    "success upon one's own as well.")
            .build()),
    RED_EYE_BLIND_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 3, CHAOS)
            .setCost(new StatMap(0, 0, 100, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(150, 0, 0, 0, 1, 1, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSelfDamage(MULTI, new StatMap(80, 0, 0, 0, 0, 1, 0.5, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(1), DamageLogic.Basic.GET)
            .setSpecial(IGNORE_DP)
            .setAnimation(Animation.build(AnimType.MELEE_CHAOS_SINGLE, AnimTime.MEDIUM, AnimSpeed.NORMAL))
            .setDescription("A sizable attack upon multiple opponents with high success, chaotic energy also means mild" +
                    " success upon one's own as well. Gold variety with higher enemy damage, lower self damage and higher cost.")
            .build()),
    BARBARIC_BASH(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 2, CHAOS)
            .setCost(new StatMap(0, 0, 60, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(100, 50, 50, 0, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSelfDamage(SINGLE, new StatMap(50, 25, 25, 0, 1, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(1), DamageLogic.Basic.GET)
            .setSpecial(IGNORE_DP)
            .setAnimation(Animation.build(AnimType.MELEE_SHIELD, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("A strong bashing blow of your shield, deals a small amount of health damage that bypasses most defenses while also " +
                    "lower the opponents strength and defense. Performed with a disregard to ones safety, resulting in " +
                    "risk of self damage to health, strength, and defense, though the opponent suffers more.")
            .build()),
    BARBARIC_BASH_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 3, CHAOS)
            .setCost(new StatMap(0, 0, 75, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(100, 75, 75, 0, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSelfDamage(SINGLE, new StatMap(50, 37, 37, 0, .85, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(1), DamageLogic.Basic.GET)
            .setSpecial(IGNORE_DP)
            .setAnimation(Animation.build(AnimType.MELEE_SHIELD, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("A strong bashing blow of your shield, deals a small amount of health damage that bypasses most defenses while also " +
                    "lower the opponents strength and defense. Performed with a disregard to ones safety, resulting in " +
                    "risk of self damage to health, strength, and defense, though the opponent suffers more. " +
                    "Gold variety deals slight more damage to both self and opponent, with less chance of self harm.")
            .build()),
    OVERPOWERED_STRIKE(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 2, CHAOS)
            .setCost(new StatMap(0, 0, 90, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(425, 25, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSelfDamage(SINGLE, new StatMap(0, 12, 75, 0, .8, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.MELEE_POWER, AnimTime.NORMAL, AnimSpeed.NORMAL).setSprite(Sprite.AXE))
            .setDescription("A strong and deadly strike, backed by lots of force, risk of strength loss from over exertion.")
            .build()),
    OVERPOWERED_STRIKE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 3, CHAOS)
            .setCost(new StatMap(0, 0, 100, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(450, 50, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSelfDamage(SINGLE, new StatMap(0, 25, 75, 0, .8, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.MELEE_POWER, AnimTime.NORMAL, AnimSpeed.NORMAL).setSprite(Sprite.AXE))
            .setDescription("A strong and deadly strike, backed by lots of force, guaranteed strength loss from over exertion." +
                    " Gold variety, slightly higher damage to opponents health and defense from over exertion.")
            .build()),
    ROULETTE(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 1, CHAOS)
            .setCost(new StatMap(0, 0, 30, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(125, 0, 0, 0, .5, 1, .5, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(4), DamageLogic.Basic.GET)
            .setSelfDamage(MULTI, new StatMap(125, 0, 0, 0, 0, 1, .4, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(4), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.MELEE_CHAOS_SINGLE, AnimTime.MEDIUM, AnimSpeed.FAST))
            .setDescription("A non-discriminatory attack, a chaotic offensive, risk of attacking anyone on the field.")
            .build()),
    ROULETTE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 2, CHAOS)
            .setCost(new StatMap(0, 0, 40, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(175, 0, 0, 0, .5, 1, .5, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(4), DamageLogic.Basic.GET)
            .setSelfDamage(MULTI, new StatMap(175, 0, 0, 0, 0, 1, .4, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(4), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.MELEE_CHAOS_SINGLE, AnimTime.MEDIUM, AnimSpeed.FAST))
            .setDescription("A non-discriminatory attack, a chaotic offensive, risk of attacking anyone on the field. " +
                    "Gold variety with higher damage and cost.")
            .build()),
    RESONANT_BLAST(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 3, CHAOS)
            .setCost(new StatMap(0, 0, 110, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(0, 0, 0, 100, 1, 1, .5, .75))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSelfDamage(MULTI, new StatMap(0, 0, 0, 100, .75, 1, .4, .75))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.RESONANT, AnimTime.LONG, AnimSpeed.NORMAL))
            .setDescription("A loud resonant sound is omitted from you armament directed towards your opponent side, as " +
                    "practiced by ancient warriors. Lowers the mana of those targeted. This ancient tradition is no " +
                    "longer practiced, high risk of mis-direction and un-predictable results.")
            .build()),
    RESONANT_BLAST_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 3, CHAOS)
            .setCost(new StatMap(0, 0, 110, 50))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(0, 0, 0, 100, 1, 1, .5, .75))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSelfDamage(MULTI, new StatMap(0, 0, 0, 100, .65, 1, .3, .75))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.RESONANT, AnimTime.LONG, AnimSpeed.NORMAL))
            .setDescription("A loud resonant sound is omitted from you armament directed towards your opponent side, as " +
                    "practiced by ancient warriors. Lowers the mana of those targeted. This ancient tradition is no " +
                    "longer practiced, but you are fairly adept at it, though still unpredictable. Gold variant uses " +
                    "mana in your attack to channel more accuracy.")
            .build()),
    MANA_STRIKE(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 2, CHAOS)
            .setCost(new StatMap(0, 0, 0, 125))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(300, 0, 0, 0, 1.2, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSpecial(IGNORE_DP)
            .setAnimation(Animation.build(AnimType.MANA_STRIKE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("Channel your mana into strength for a strong direct strike, highly successful, bypassing most defenses.")
            .build()),
    MANA_STRIKE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MELEE, 3, CHAOS)
            .setCost(new StatMap(0, 0, 0, 150))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(300, 0, 0, 0, 1.2, 1, .5, .65))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSpecial(IGNORE_DP)
            .setAnimation(Animation.build(AnimType.MANA_STRIKE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("Channel your mana into strength for a strong direct strike, highly successful, bypassing most defenses. " +
                    "Gold variant, uses slightly more mana for a chance to damage multiple pawns from the energy released.")
            .build()),
    //////////
    /*RANGED*/
    //////////
    // TODO maybe add flag for vital hits?
    VITAL_SHOT(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 3, ORDER)
            .setCost(new StatMap(0, 0, 80, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(300, 0, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.5, 1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_SINGLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("A well aimed shot to a vital region between the enemies armor to bypass armour defense, 50% chance of vital damage" +
                    " bypassing most defenses.")
            .build()),
    VITAL_SHOT_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 3, ORDER)
            .setCost(new StatMap(0, 0, 90, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(350, 0, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.65, 1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_SINGLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("A well aimed shot to a vital region between the enemies armor to bypass armour defense, 65% chance of vital damage " +
                    "bypassing most defenses. Gold variant higher vital chance for high cost.")
            .build()),
    VITAL_FLURRY(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 1, ORDER)
            .setCost(new StatMap(0, 0, 80, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(75, 0, 0, 0, .75, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.25, 6), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_SINGLE, AnimTime.NORMAL, AnimSpeed.FAST))
            .setDescription("A flurry of 6 well placed shots, accuracy traded for volume, 25% chance of a vital hit bypassing most defenses.")
            .build()),
    VITAL_FLURRY_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 2, ORDER)
            .setCost(new StatMap(0, 0, 110, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(75, 0, 0, 0, .75, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.25, 12), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_SINGLE, AnimTime.NORMAL, AnimSpeed.EXTRA_FAST))
            .setDescription("A flurry of 12 well placed shots, accuracy traded for volume, 25% chance of a vital hit bypassing most defenses. " +
                    "Gold variant with a larger flurry and higher cost.")
            .build()),
    KNEE_SHOT(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 2, ORDER)
            .setCost(new StatMap(0, 0, 80, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(100, 0, 75, 0, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_SINGLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("A well placed shot to an opponents knee, dealing a lage loss of strength. They used to be an adventurer....")
            .build()),
    KNEE_SHOT_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 3, ORDER)
            .setCost(new StatMap(0, 0, 100, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(100, 0, 100, 0, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_SINGLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("A well placed shot to an opponents knee, dealing a lage loss of strength. Gold variant, high damage for a higher cost." +
                    "...Until they took an arrow to the knee.")
            .build()),
    ARCHERS_SALVO(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 2, ORDER)
            .setCost(new StatMap(0, 0, 100, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(100, 0, 0, 0, .45, 1, .45, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.2, 4), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_SINGLE, AnimTime.NORMAL, AnimSpeed.EXTRA_FAST))
            .setDescription("Fire off 4 rapid shots to each enemy on your opponents line. Each with a 20% chance of a " +
                    "vital hit bypassing most defenses.")
            .build()),
    ARCHERS_SALVO_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 3, ORDER)
            .setCost(new StatMap(0, 0, 120, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(100, 0, 0, 0, .6, 1, .6, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.35, 4), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_SINGLE, AnimTime.NORMAL, AnimSpeed.EXTRA_FAST))
            .setDescription("Fire off 4 rapid shots to each enemy on your opponents line. Each with a 25% chance of a vital hit bypassing most defenses." +
                    "Gold variant slightly high cost for a small increase in vital hit chance. ")
            .build()),
    SWIFT_SHOT(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 1, ORDER)
            .setCost(new StatMap(0, 0, 40, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(200, 0, 0, 0, 1.2, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.5, 1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_SINGLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("A swift simple shot, low cost, light on damage, 20% chance of a vital hit bypassing defenses.")
            .build()),
    SWIFT_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 2, ORDER)
            .setCost(new StatMap(0, 0, 60, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(275, 0, 0, 0, 1.2, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.9, 1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_SINGLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("A swift simple shot, low cost, light on damage, 25% chance of a vital hit bypassing defenses." +
                    "Gold variant, slight cost increase for high chance of vital hit.")
            .build()),
    DEADLY_SHURIKEN(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 3, ORDER)
            .setCost(new StatMap(0, 0, 150, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(400, 50, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSpecial(IGNORE_DP)
            .setAnimation(Animation.build(AnimType.SHURIKEN, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("You pull from your pocket a weighted and deadly shuriken, dealing a large amount of damage " +
                    "to health and moderate damage to the enemies armor.")
            .build()),
    DEADLY_SHURIKEN_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 4, ORDER)
            .setCost(new StatMap(0, 0, 160, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(350, 50, 0, 0, .6, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(2), DamageLogic.Basic.GET)
            .setSpecial(IGNORE_DP)
            .setAnimation(Animation.build(AnimType.SHURIKEN, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("You pull from your pocket a weighted and deadly shuriken, dealing a large amount of damage to health and " +
                    "moderate damage to the enemies armor. Gold variant, throws two in rapid succession with " +
                    "each having a moderate chance of hitting")
            .build()),
    POISON_SHOT(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 3, ORDER)
            .setCost(new StatMap(0, 0, 90, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(150, 0, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.75, 1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, POISON, true, 80, .25, true, .75, 1)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_POISON_SINGLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("Dipping an arrow in poison, you take aim for the most vital exposed region, if the arrow hits its mark" +
                    " you deal substantial poison damage.")
            .build()),
    POISON_SHOT_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 4, ORDER)
            .setCost(new StatMap(0, 0, 120, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(175, 0, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.9, 1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, POISON, true, 95, .25, true, 1.1, 1)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_POISON_SINGLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("Dipping an arrow in poison, you take aim for the most vital exposed region, using a special arrow" +
                    "crafted for penetration you deal substantial poison damage. Gold variant higher cost, and high chance of successful poisoning . ")
            .build()),
    SLUMBEROUS_SHOT(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 2, ORDER)
            .setCost(new StatMap(0, 0, 120, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(120, 0, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.5, 1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, SLEEP, true, 150, .5, true, .65, 1)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_ENCHANTED_SINGLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("Using the oils from a foreign ivy know for it sedative effects, you dip and fire a well aimed shot towards" +
                    "your opponents vital regions, if you hit they a quickly in a slumber.")
            .build()),
    SLUMBEROUS_SHOT_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 3, ORDER)
            .setCost(new StatMap(0, 0, 160, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(150, 0, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.5, 1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, SLEEP, true, 150, .35, true, .75, 1)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_ENCHANTED_SINGLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("Using the oils from a foreign ivy know for it sedative effects, you dip and fire a well aimed shot towards" +
                    "your opponents vital regions, if you hit they a quickly in a slumber. Gold variant with a slightly " +
                    "lower roll off chance, higher slumber chance, and slightly higher cost.")
            .build()),
    TRIPLE_SHOT(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 2, ORDER)
            .setCost(new StatMap(0, 0, 100, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(200, 0, 0, 0, .8, 1, .8, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.5, 1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_SINGLE, AnimTime.MEDIUM, AnimSpeed.FAST))
            .setDescription("Fire off a precise well meditated shot to each of your opponents pawns, moderate chance of vital damage.")
            .build()),
    TRIPLE_SHOT_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 3, ORDER)
            .setCost(new StatMap(0, 0, 100, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(200, 0, 0, 0, .8, 1, .8, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(0.75, 1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_SINGLE, AnimTime.MEDIUM, AnimSpeed.FAST))
            .setDescription("Fire off a precise well meditated shot to each of your opponents pawns, Gold variant with higher chance of vital damage.")
            .build()),
    QUICK_TRIPLE(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 1, ORDER)
            .setCost(new StatMap(0, 0, 70, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(150, 0, 0, 0, .7, 1, .7, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.35, 1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_SINGLE, AnimTime.MEDIUM, AnimSpeed.FAST))
            .setDescription("Fire of a quick arrow towards all of your opponents pawns, lacks in precision but low in cost. Good against less armored opponents.")
            .build()),
    QUICK_TRIPLE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 1, ORDER)
            .setCost(new StatMap(0, 0, 80, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(150, 0, 0, 0, .8, 1, .8, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.45, 1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_SINGLE, AnimTime.MEDIUM, AnimSpeed.FAST))
            .setDescription("Fire of a quick arrow towards all of your opponents pawns, lacks in precision but low in cost. Good against less armored opponents." +
                    "Gold Variant slightly higher chance, for higher cost.")
            .build()),
    ///////////
    // CHAOS //
    ///////////
    OVER_DRAW(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 3, CHAOS)
            .setCost(new StatMap(0, 0, 100, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(340, 60, 0, 0, .87, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(1.1, 1), DamageLogic.Basic.GET)
            .setSelfDamage(SINGLE, new StatMap(175, 0, 0, 0, .8, 1))
            .setSelfDamageLogic(DamageCalc.VitalChanceSelf.GET(1.1, 1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_STRONG, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("Drawing the bow back with all your might, you fire off a single, powerful armor piercing shot. " +
                    "It almost as if your ancestors are pushing it through them. Damages both health and defense. Risk of self damage from over exertion.")
            .build()),
    OVER_DRAW_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 4, CHAOS)
            .setCost(new StatMap(0, 0, 120, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(450, 75, 0, 0, .87, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(1.1, 1), DamageLogic.Basic.GET)
            .setSelfDamage(SINGLE, new StatMap(160, 0, 0, 0, .7, 1))
            .setSelfDamageLogic(DamageCalc.VitalChanceSelf.GET(1.1, 1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_STRONG, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("Drawing the bow back with all your might, you fire off a single, powerful armor piercing shot. " +
                    "It almost as if your ancestors are pushing it through them. Damages both health and defense. Risk of self damage from over exertion." +
                    " Gold variant with higher damage for a higher cost and lower risk of self damage.")
            .build()),
    CHAOS_RAIN(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 2, CHAOS)
            .setCost(new StatMap(0, 0, 130, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(75, 0, 0, 0, .3, 1, .3, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.75, 10), DamageLogic.Basic.GET)
            .setSelfDamage(MULTI, new StatMap(75, 0, 0, 0, .15, 1, .15, 1))
            .setSelfDamageLogic(DamageCalc.VitalChanceSelf.GET(.5, 10), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_CHAOS_RAIN, AnimTime.NORMAL, AnimSpeed.EXTRA_FAST))
            .setDescription("Fire a barrage of arrows high into the sky, raining down on your opponents line...and yours. " +
                    "High chaos and risk of team damage, may luck be on your side. High chance of vital hits.")
            .build()),
    CHAOS_RAIN_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 3, CHAOS)
            .setCost(new StatMap(0, 0, 150, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(90, 0, 0, 0, .3, 1, .3, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.75, 10), DamageLogic.Basic.GET)
            .setSelfDamage(MULTI, new StatMap(90, 0, 0, 0, .15, 1, .15, 1))
            .setSelfDamageLogic(DamageCalc.VitalChanceSelf.GET(.4, 10), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_CHAOS_RAIN, AnimTime.NORMAL, AnimSpeed.EXTRA_FAST))
            .setDescription("Fire a barrage of arrows high into the sky, raining down on your opponents line...and yours. " +
                    "High chaos and risk of team damage, may luck be on your side. High chance of vital hits. " +
                    "Gold variant with higher damage for a higher cost.")
            .build()),
    SKY_FALL(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 1, CHAOS)
            .setCost(new StatMap(0, 0, 90, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(40, 0, 0, 0, .3, 1, .3, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.75, 10), DamageLogic.Basic.GET)
            .setSelfDamage(MULTI, new StatMap(40, 0, 0, 0, 0, 1, .15, 1))
            .setSelfDamageLogic(DamageCalc.VitalChanceSelf.GET(.75, 10), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_RAIN, AnimTime.NORMAL, AnimSpeed.EXTRA_FAST))
            .setDescription("Fire a focused but weakly drawn barrage of arrows into the sky. Light on damage, but less riskier to team" +
                    "than Chaos rain. High chance of vital hits.")
            .build()),
    SKY_FALL_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 2, CHAOS)
            .setCost(new StatMap(0, 0, 110, 0))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(50, 0, 0, 0, .35, 1, .35, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.75, 10), DamageLogic.Basic.GET)
            .setSelfDamage(MULTI, new StatMap(50, 0, 0, 0, 0, 1, .15, 1))
            .setSelfDamageLogic(DamageCalc.VitalChanceSelf.GET(.75, 10), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_RAIN, AnimTime.NORMAL, AnimSpeed.EXTRA_FAST))
            .setDescription("Fire a focused but weakly drawn barrage of arrows into the sky. Light on damage, but less riskier to team" +
                    "than Chaos rain. High chance of vital hits. Gold variant, slight higher accuracy and damage.")
            .build()),
    STRANGE_BREW(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 2, CHAOS)
            .setCost(new StatMap(0, 0, 70, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(150, 0, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.35, 1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, POISON, true, 80, .35, true, 0.25, 1),
                    new Effect(SINGLE, CONFUSION, true, .5, .35, true, 0.25, 1),
                    new Effect(SINGLE, DE_INVIGORATE, true, 200, .35, true, 0.25, 1),
                    new Effect(SINGLE, DE_CORE, true, 100, .35, true, 0.25, 1),
                    new Effect(SINGLE, DE_FORTIFY, true, 100, .35, true, 0.25, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setNegSelfEffects(new Effect[]{
                    new Effect(SINGLE, POISON, true, 80, .45, true, 0.2, 1),
                    new Effect(SINGLE, CONFUSION, true, .5, .45, true, 0.2, 1),
                    new Effect(SINGLE, DE_INVIGORATE, true, 200, .45, true, 0.2, 1),
                    new Effect(SINGLE, DE_CORE, true, 100, .45, true, 0.2, 1),
                    new Effect(SINGLE, DE_FORTIFY, true, 100, .45, true, 0.2, 1),
            })
            .setNegSelfEffectLogic(EffectCalc.BasicSelfNeg.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_STRANGE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("The local alchemist had given you a poison prepared from the remaining ingredients from their preparations " +
                    "for battle. You quickly dip an arrow in it, slinging some on you in your haste, you notice an noxious odor," +
                    "and start to feel unwell firing your shot.....You hope this doesnt last long.")
            .build()),
    STRANGE_BREW_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 3, CHAOS)
            .setCost(new StatMap(0, 0, 80, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(175, 0, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.35, 1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, POISON, true, 80, .35, true, 0.35, 1),
                    new Effect(SINGLE, CONFUSION, true, .5, .35, true, 0.35, 1),
                    new Effect(SINGLE, DE_INVIGORATE, true, 200, .35, true, 0.35, 1),
                    new Effect(SINGLE, DE_CORE, true, 100, .35, true, 0.35, 1),
                    new Effect(SINGLE, DE_FORTIFY, true, 100, .35, true, 0.35, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setNegSelfEffects(new Effect[]{
                    new Effect(SINGLE, POISON, true, 80, .45, true, 0.25, 1),
                    new Effect(SINGLE, CONFUSION, true, .5, .45, true, 0.25, 1),
                    new Effect(SINGLE, DE_INVIGORATE, true, 200, .45, true, 0.25, 1),
                    new Effect(SINGLE, DE_CORE, true, 100, .45, true, 0.25, 1),
                    new Effect(SINGLE, DE_FORTIFY, true, 100, .45, true, 0.25, 1),
            })
            .setNegSelfEffectLogic(EffectCalc.BasicSelfNeg.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_STRANGE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("The local alchemist had given you a poison prepared from the remaining ingredients from their preparations" +
                    " for battle. This one seem different than the last time. You quickly dip an arrow in it, slinging some " +
                    "on you in your haste, you notice an noxious odor, and start to feel unwell firing your shot....." +
                    "You hope this doesnt last long. Gold variant with higher chance of...something.")
            .build()),
    ENFEEBLING_SHOT(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 1, CHAOS)
            .setCost(new StatMap(0, 0, 60, 50))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(120, 0, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.35, 1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, FEEBLENESS, true, 1, .35, true, 1, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_CHAOS_ENCHANTED_SINGLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("Using your mana you channel a chaotic energy of feebleness into your arrows shaft, lowering you targets willpower " +
                    "Highly successful, but not the most effective.")
            .build()),
    ENFEEBLING_SHOT_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 2, CHAOS)
            .setCost(new StatMap(0, 0, 60, 70))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(120, 0, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.35, 1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, FEEBLENESS, true, 2, .35, true, 1, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_CHAOS_ENCHANTED_SINGLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("Using your mana you channel a chaotic energy of feebleness into your arrows shaft, lowering you targets willpower. " +
                    "Gold variant with stronger enchantment.")
            .build()),
    ARROW_OF_MISFORTUNE(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 2, CHAOS)
            .setCost(new StatMap(0, 0, 50, 50))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(120, 0, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.4, 1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, MISFORTUNE, true, 2, .25, true, 1, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_CHAOS_ENCHANTED_SINGLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("Using your mana you channel chaotic energy of misfortune into your arrows shaft, lowering your " +
                    "targets luck. Highly Successful. ")
            .build()),
    ARROW_OF_MISFORTUNE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 3, CHAOS)
            .setCost(new StatMap(0, 0, 50, 80))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(150, 0, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.4, 1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, MISFORTUNE, true, 4, .2, true, 1, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_CHAOS_ENCHANTED_SINGLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("Using your mana you channel chaotic energy of misfortune into your arrows shaft, lowering your " +
                    "targets luck. Highly Successful. Gold variant with stronger enchantment.")
            .build()),
    REIGN_OF_CHAOS(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 2, CHAOS)
            .setCost(new StatMap(0, 0, 70, 70))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(100, 0, 0, 0, .7, 1,.7,1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.35, 1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, MISFORTUNE, true, 2, .3, true, .15, 1, .15, 1),
                    new Effect(MULTI, FEEBLENESS, true, 2, .3, true, .15, 1, .15, 1),
                    new Effect(MULTI, DE_CORE, true, 100, .3, true, .15, 1, .15, 1),
                    new Effect(MULTI, DE_MEDITATE, true, 100, .3, true, .15, 1, .15, 1),
                    new Effect(MULTI, DE_INVIGORATE, true, 100, .3, true, .15, 1, .15, 1),
                    new Effect(MULTI, DE_FORTIFY, true, 100, .3, true, .15, 1, .15, 1),
                    new Effect(MULTI, CONFUSION, true, .35, .3, true, .15, 1, .15, 1)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setNegSelfEffects(new Effect[]{
//                    new Effect(MULTI, MISFORTUNE, true, 2, .3, true, .0, 1, .0, 1),
//                    new Effect(MULTI, FEEBLENESS, true, 2, .3, true, .0, 1, .0, 1),
//                    new Effect(MULTI, DE_CORE, true, 100, .3, true, .0, 1, .0, 1),
//                    new Effect(MULTI, DE_MEDITATE, true, 100, .3, true, .0, 1, .0, 1),
//                    new Effect(MULTI, DE_INVIGORATE, true, 100, .3, true, .0, 1, .0, 1),
//                    new Effect(MULTI, DE_FORTIFY, true, 100, .3, true, .0, 1, .0, 1),
//                    new Effect(MULTI, CONFUSION, true, .35, .3, true, .0, 1, .0, 1)
                    new Effect(MULTI, MISFORTUNE, true, 2, .3, true, .1, 1, .1, 1),
                    new Effect(MULTI, FEEBLENESS, true, 2, .3, true, .1, 1, .1, 1),
                    new Effect(MULTI, DE_CORE, true, 100, .3, true, .1, 1, .1, 1),
                    new Effect(MULTI, DE_MEDITATE, true, 100, .3, true, .1, 1, .1, 1),
                    new Effect(MULTI, DE_INVIGORATE, true, 100, .3, true, .1, 1, .1, 1),
                    new Effect(MULTI, DE_FORTIFY, true, 100, .3, true, .1, 1, .1, 1),
                    new Effect(MULTI, CONFUSION, true, .35, .3, true, .1, 1, .1, 1)
            })
            .setNegSelfEffectLogic(EffectCalc.BasicSelfNeg.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_ENCHANTED_RAIN, AnimTime.EXTRA_LONG, AnimSpeed.EXTRA_FAST))
            .setDescription("Not the most adept at magic, you draw on all your knowledge and put your fate into the hands" +
                    "of the ancestral plane enchanting pure chaos into your arrows shaft. May the luck of their spirits be on your side.")
            .build()),
    REIGN_OF_CHAOS_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 4, CHAOS)
            .setCost(new StatMap(0, 0, 90, 160))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(150, 0, 0, 0, .75, 1,.75,1))
            .setDamageLogic(DamageCalc.VitalChance.GET(.35, 1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, MISFORTUNE, true, 2, .33, true, .25, 1, .25, 1),
                    new Effect(MULTI, FEEBLENESS, true, 2, .33, true, .25, 1, .25, 1),
                    new Effect(MULTI, DE_CORE, true, 100, .33, true, .25, 1, .25, 1),
                    new Effect(MULTI, DE_MEDITATE, true, 100, .33, true, .25, 1, .25, 1),
                    new Effect(MULTI, DE_INVIGORATE, true, 100, .33, true, .25, 1, .25, 1),
                    new Effect(MULTI, DE_FORTIFY, true, 100, .33, true, .25, 1, .25, 1),
                    new Effect(MULTI, CONFUSION, true, .35, .33, true, .25, 1, .25, 1)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setNegSelfEffects(new Effect[]{
                    new Effect(MULTI, MISFORTUNE, true, 2, .33, true, .1, 1, .1, 1),
                    new Effect(MULTI, FEEBLENESS, true, 2, .33, true, .1, 1, .1, 1),
                    new Effect(MULTI, DE_CORE, true, 100, .33, true, .1, 1, .1, 1),
                    new Effect(MULTI, DE_MEDITATE, true, 100, .33, true, .1, 1, .1, 1),
                    new Effect(MULTI, DE_FORTIFY, true, 100, .33, true, .1, 1, .1, 1),
                    new Effect(MULTI, DE_INVIGORATE, true, 100, .33, true, .1, 1, .1, 1),
                    new Effect(MULTI, CONFUSION, true, .35, .33, true, .1, 1, .1, 1)
            })
            .setNegSelfEffectLogic(EffectCalc.BasicSelfNeg.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ARROW_ENCHANTED_RAIN, AnimTime.EXTRA_LONG, AnimSpeed.EXTRA_FAST))
            .setDescription("Not the most adept at magic, you draw on all your knowledge and put your fate into the hands" +
                    "of the ancestral plane enchanting pure chaos into your arrows shaft. May the luck of their spirits be on your side." +
                    " Gold variant stronger enchantment with higher chances and stronger effects to all, for more mana")
            .build()),
    SHADOW_STRIKE(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 2, CHAOS)
            .setCost(new StatMap(0, 0, 50, 70))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(700, 0, 0, 0, .5, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(1.1, 1), DamageLogic.Basic.GET)
            .setSelfDamage(SINGLE, new StatMap(700, 0, 0, 0, .6, 1))
            .setSelfDamageLogic(DamageCalc.VitalChanceSelf.GET(1.1, 1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.SHADOW_STRIKE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("Channeling mana to cloak your self, you attempt to sneak behind the opponents line to attack them from behind " +
                    "Dealing high damage if successful, with a high risk of damage from being caught.")
            .build()),
    SHADOW_STRIKE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 3, CHAOS)
            .setCost(new StatMap(0, 0, 50, 100))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(700, 0, 0, 0, .5, 1))
            .setDamageLogic(DamageCalc.VitalChance.GET(1.1, 1), DamageLogic.Basic.GET)
            .setSelfDamage(SINGLE, new StatMap(700, 0, 0, 0, .45, 1))
            .setSelfDamageLogic(DamageCalc.VitalChanceSelf.GET(1.1, 1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.SHADOW_STRIKE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("Channeling mana to cloak your self, you attempt to sneak behind the opponents line to attack them from behind " +
                    "Dealing high damage if successful, with a high risk of damage from being caught. Gold variant, uses " +
                    "more mana to cloak, lowering risk of being caught and damaged.")
            .build()),
    SKY_PIERCER(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 3, CHAOS)
            .setCost(new StatMap(0, 0, 100, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(450, 100, 0, 0, .60, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSpecial(IGNORE_DP)
            .setAnimation(Animation.build(AnimType.JAVELIN, AnimTime.NORMAL, AnimSpeed.NORMAL)).
            setDescription("Reaching on to your back you grab a sacred javelin, one of the most devastating weapons known. Not the most " +
                    "accurate, but carries a high amount of health and defense damage if it hits its mark.")
            .build()),
    SKY_PIERCER_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 4, CHAOS)
            .setCost(new StatMap(0, 0, 110, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(450, 100, 0, 0, .75, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSpecial(IGNORE_DP)
            .setAnimation(Animation.build(AnimType.JAVELIN, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("Reaching on to your back you grab a sacred javelin, one of the most devastating weapons known. Not the most " +
                    "accurate, but carries a high amount of health and defense damage if it hits its mark. " +
                    "Gold variant with higher chance.")
            .build()),
    SLING_SHOT(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 1, CHAOS)
            .setCost(new StatMap(0, 0, 70, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(120, 0, 0, 0, .9, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, CONFUSION, true, .35, .35, true, .35, 1),
                    new Effect(SINGLE, SLEEP, true, 150, .5, true, .25, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setSpecial(IGNORE_DP)
            .setAnimation(Animation.build(AnimType.SLING_SHOT, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("Taking a slingshot carried for times of limited ammo and loading it with a large rock, you pull and release your attack." +
                    " You are well trained and with accuracy and luck you can deal a blow to their head rendering them unconscious and/or confused.")
            .build()),
    SLING_SHOT_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, RANGED, 2, CHAOS)
            .setCost(new StatMap(0, 0, 85, 0))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(150, 0, 0, 0, .9, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, CONFUSION, true, .35, .3, true, .4, 1),
                    new Effect(SINGLE, SLEEP, true, 150, .5, true, .3, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setSpecial(IGNORE_DP)
            .setAnimation(Animation.build(AnimType.SLING_SHOT, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("Taking a sling carried for times of limited ammo and loading it with a large rock, you swing and release your attack." +
                    "You are well trained and with accuracy and luck you can deal a blow to their head rendering them unconscious and/or confused." +
                    " Gold variant, using some extra strength you deal a more powerful blow.")
            .build()),
    ///////////
    // MAGIC //
    ///////////
    LIFE_SAP(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, ORDER)
            .setCost(new StatMap(0, 0, 0, 80))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, SIPHON_HEALTH, true, 200, 0, true, 1, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.SIPHON_HEALTH, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Infuse yourself with the health of your opponent.")
            .build()),
    LIFE_SAP_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, ORDER)
            .setCost(new StatMap(0, 0, 0, 140))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, SIPHON_HEALTH, true, 350, 0, true, 1, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.SIPHON_HEALTH, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Infuse yourself with the health of your opponent. Gold variant, siphons more health for a higher cost.")
            .build()),
    DEFENSE_SAP(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, ORDER)
            .setCost(new StatMap(0, 0, 0, 80))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, SIPHON_DEFENSE, true, 60, 0, true, 1, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.SIPHON_DEFENSE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Your opponent feels their armour start to heat up, as you channel it's damage resistance into your own their flesh begins to burn.")
            .build()),
    DEFENSE_SAP_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 4, ORDER)
            .setCost(new StatMap(0, 0, 0, 150))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(200, 0, 0, 0))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, SIPHON_DEFENSE, true, 140, 0, true, 1, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setSpecial(IGNORE_MP)
            .setAnimation(Animation.build(AnimType.SIPHON_DEFENSE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Your opponent feels their armour start to heat up as you channel it's damage resistance into your own their flesh begins to burn." +
                    "Gold variant higher costs, siphons more defense and causes health damage.")
            .build()),
    ENFEEBLED_MIND(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 1, ORDER)
            .setCost(new StatMap(0, 0, 0, 70))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, FEEBLENESS, true, 1, .4, true, 1, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.CURSE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Feeble the mind of your opponent, lowering their willpower and regeneration.")
            .build()),
    ENFEEBLED_MIND_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, ORDER)
            .setCost(new StatMap(0, 0, 0, 120))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, FEEBLENESS, true, 2, .25, true, 1, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.CURSE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Feeble the mind of your opponent, lowering their willpower and regeneration. " +
                    "Gold variant, higher cost for higher effect and duration.")
            .build()),
    SLEEP_RELEASE(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, ORDER)
            .setCost(new StatMap(0, 0, 0, 100))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, SLEEP, true, 150, .5, true, 1, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.CURSE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("A focused and Strong arcane mist, causing an opponent to slip into the peace of sleep, while still in the turmoils of battle.")
            .build()),
    SLEEP_RELEASE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 4, ORDER)
            .setCost(new StatMap(0, 0, 0, 160))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, SLEEP, true, 150, .65, true, .5, 1, .5, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.CURSE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("A focused arcane mist, causing your opponents to slip into the peace of sleep, while still in the turmoils of battle." +
                    " Gold variant, wider mist but less concentrated, rolls off sooner and dependent on chance.")
            .build()),
    CLOUD_OF_ILL_FATE(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, ORDER)
            .setCost(new StatMap(0, 0, 0, 100))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, MISFORTUNE, true, 4, .35, true, 1, 1, 0, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.CURSE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("The clouds darken overhead as an eerie light shines though, leaving an unsettling feeling of fate among your opponents.")
            .build()),
    CLOUD_OF_ILL_FATE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, ORDER)
            .setCost(new StatMap(0, 0, 0, 140))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, MISFORTUNE, true, 4, .35, true, .5, 1, .5, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.CURSE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("The clouds darken overhead as an eerie light shines though, leaving an unsettling feeling of fate among your opponents." +
                    "Gold variant affects multiple enemies for a higher cost.")
            .build()),
    CRIPPLING_BOLT(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, ORDER)
            .setCost(new StatMap(0, 0, 0, 100))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(175, 50, 50, 0, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.BOLT_PURPLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("Fire off a bolt of energy towards the enemy, damaging health, defense, and strength on impact.")
            .build()),
    CRIPPLING_BOLT_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 4, ORDER)
            .setCost(new StatMap(0, 0, 0, 160))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(275, 80, 80, 0, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.BOLT_PURPLE, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("Meditating on your attack you fire off a bolt of immense energy towards the enemy, damaging health, defense, and strength on impact." +
                    "Gold variant, higher damage to stats for a higher cost.")
            .build()),
    AURA_OF_DESTRUCTION(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, ORDER)
            .setCost(new StatMap(0, 0, 0, 120))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(200, 0, 0, 0, 1, 1, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.AURA_DESTRUCTION, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Cast a damaging aura around your opponents line causing moderate damage to them all.")
            .build()),
    AURA_OF_DESTRUCTION_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 4, ORDER)
            .setCost(new StatMap(0, 0, 0, 160))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(300, 0, 0, 0, 1, 1, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.AURA_DESTRUCTION, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Cast a damaging aura around your opponents, they are overwhelmed with pain as you deal heavy damage to them all.")
            .build()),
    SPIT_FIRE(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 1, ORDER)
            .setCost(new StatMap(0, 0, 0, 50))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(50, 0, 0, 0, .3, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(10), DamageLogic.Basic.GET)
            .setSpecial(IGNORE_MP)
            .setAnimation(Animation.build(AnimType.FIREBALL, AnimTime.LONG, AnimSpeed.FAST))
            .setDescription("You release a volley of ten fireballs directly toward and opponent, some do not make it the full " +
                    "distance, but the ones that do can not be resisted, may luck be on your side.")
            .build()),
    SPIT_FIRE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, ORDER)
            .setCost(new StatMap(0, 0, 0, 80))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(75, 0, 0, 0, .35, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(10), DamageLogic.Basic.GET)
            .setSpecial(IGNORE_MP)
            .setAnimation(Animation.build(AnimType.FIREBALL, AnimTime.LONG, AnimSpeed.FAST))
            .setDescription("You release a volley of ten fireballs directly toward and opponent, some do not make it the full " +
                    "distance, but the ones that do can not be resisted, may luck be on your side. Gold variant with higher cost, " +
                    "but more damage and chance.")
            .build()),
    CATATUMBO_LIGHTNING(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, ORDER)
            .setCost(new StatMap(0, 0, 0, 80))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(50, 0, 0, 0, .15, 1, .15, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(25), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.CATATUMBO_LIGHTNING, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("The sky darkens, as lighting start flashing rapidly, dancing around the sky. Suddenly it descends, " +
                    "wrecking havoc to your opponents line.")
            .build()),
    CATATUMBO_LIGHTNING_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, ORDER)
            .setCost(new StatMap(0, 0, 0, 120))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(65, 0, 0, 0, .2, 1, .2, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(25), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.CATATUMBO_LIGHTNING, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("The sky darkens, as lighting start flashing rapidly, dancing around the sky. Suddenly it descends, " +
                    "wrecking havoc to your opponents line. Gold variant with slight higher damage and chance.")
            .build()),
    VITALITY_DRAIN(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, ORDER)
            .setCost(new StatMap(0, 0, 0, 70))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, DE_INVIGORATE, true, 250, .33, true, 1, 1, 0, 0),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ENERGY_BALL_DRAIN, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("Hitting your enemy with a ball of energy they find themselves feeling suddenly unwell. Lowers both health and max health for duration.")
            .build()),
    VITALITY_DRAIN_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, ORDER)
            .setCost(new StatMap(0, 0, 0, 110))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, DE_INVIGORATE, true, 325, .33, true, 1, 1, 0, 0),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.ENERGY_BALL_DRAIN, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("Hitting your enemy with a ball of energy they find themselves feeling suddenly unwell. Lowers both health and max health for duration. " +
                    "Gold variant, deals higher health loss for a higher cost.")
            .build()),
    ///////////
    // CHAOS //
    ///////////
    MAGES_IMPASSE(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, CHAOS)
            .setCost(new StatMap(0, 0, 0, 300))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, SIPHON_MANA, true, 150, 0, true, 1, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.IMPASSE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("A spiteful attack that leaves both you and your opponent with less manna than you started with," +
                    " no one wins today.....or do they?")
            .build()),
    MAGES_IMPASSE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 4, CHAOS)
            .setCost(new StatMap(0, 0, 0, 400))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, SIPHON_MANA, true, 200, 0, true, 1, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.IMPASSE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("A spiteful attack that leaves both you and your opponent with less manna than you started with," +
                    " no one wins today.....or do they? Gold variant with higher mana siphoning and cost.")
            .build()),
    ARC_LIGHTNING(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, CHAOS)
            .setCost(new StatMap(0, 0, 0, 80))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(200, 0, 0, 0, 1, 1, .7, .8))
            .setSelfDamage(MULTI, new StatMap(200, 0, 0, 0, .5, .6, .4, .5))
            //Self damage is also handled by this logic but still needs set by .setSelfDamage
            .setDamageLogic(DamageCalc.SeqTargetAndSelf.GET, DamageLogic.Basic.GET)
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(1), DamageLogic.Basic.GET) // Ignored needed to validate
            .setSpecial(IGNORE_DP)
            .setAnimation(Animation.build(AnimType.ARC_LIGHTENING, AnimTime.SHORT, AnimSpeed.NORMAL))
            .setDescription("You release a high voltage bolt of lightening with a high probability of arcing to other players....and" +
                    " possibly your own. Will jump to other pawns that are near, most effective when cast towards the ends of your opponents " +
                    "line. Un-faltered by armor.")
            .build()) {
        @Override
        public ActionReturn playCard(PlayerGameState player, PlayerGameState target, PawnIndex playerIdx, PawnIndex targetIdx) {
            ActionReturn actionReturn = getSequentialActionReturn(player, playerIdx, target, targetIdx, getStats().getAnimation());
            // Do Cost, aborts if player can't afford;
               if (!getStats().getCostLogic().doCost(player.getPawn(playerIdx), getStats().getCost().asMap(), actionReturn)) {
                return ActionReturn.getInvalid(player.getPawn(playerIdx), InvalidMsg._INVALID_COST, getName());
            }
            getStats().getDamageCalc().doDamage(getStats().getDamageLogic(), actionReturn.playerPawnStates,
                                                actionReturn.targetPawnStates, this, getStats().getSpecial());

            getStats().getSelfDamageCalc().doDamage(getStats().getDamageLogic(), actionReturn.playerPawnStates,
                    actionReturn.targetPawnStates, this, getStats().getSpecial());

            return actionReturn;
        }
    },
    ARC_LIGHTNING_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, CHAOS)
            .setCost(new StatMap(0, 0, 0, 100))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(250, 0, 0, 0, 1, 1, .7, .8))
            .setSelfDamage(MULTI, new StatMap(250, 0, 0, 0, .5, .6, .4, .5))
            //Self damage is also handled by this logic but still needs set by .setSelfDamage
            .setDamageLogic(DamageCalc.SeqTargetAndSelf.GET, DamageLogic.Basic.GET)
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(1), DamageLogic.Basic.GET) // Ignored needed to validate
            .setSpecial(IGNORE_DP)
            .setAnimation(Animation.build(AnimType.ARC_LIGHTENING, AnimTime.SHORT, AnimSpeed.NORMAL))
            .setDescription("You release a high voltage bolt of lightening with a high probability of arcing to other players...." +
                    "and possibly your own. Will jump to other pawns that are near, most effective when cast towards the ends of your " +
                    "opponents line. Un-faltered by armor. Gold variant more damage for a higher cost.")
            .build()) {
        @Override
        public ActionReturn playCard(PlayerGameState player, PlayerGameState target, PawnIndex playerIdx, PawnIndex targetIdx) {
            ActionReturn actionReturn = getSequentialActionReturn(player, playerIdx, target, targetIdx, getStats().getAnimation());
            // Do Cost, aborts if player can't afford;
               if (!getStats().getCostLogic().doCost(player.getPawn(playerIdx), getStats().getCost().asMap(), actionReturn)) {
                return ActionReturn.getInvalid(player.getPawn(playerIdx), InvalidMsg._INVALID_COST, getName());
            }

            getStats().getDamageCalc().doDamage(getStats().getDamageLogic(), actionReturn.playerPawnStates,
                                                actionReturn.targetPawnStates, this, getStats().getSpecial());

            getStats().getSelfDamageCalc().doDamage(getStats().getDamageLogic(), actionReturn.playerPawnStates,
                                                    actionReturn.targetPawnStates, this, getStats().getSpecial());
            return actionReturn;
        }
    },
    NOXIOUS_SHOWER(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, CHAOS)
            .setCost(new StatMap(0, 0, 0, 80))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, POISON, true, 10, .6, true, .15, 1, .15, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(100), EffectLogic.Basic.GET)
            .setNegSelfEffects(new Effect[]{
                    new Effect(MULTI, POISON, true, 10, .6, true, .1, 1, .1, 1),
            })
            .setNegSelfEffectLogic(EffectCalc.BasicSelfNeg.GET(40), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.NOXIOUS_RAIN, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("The clouds darken overhead as an eerie light shines though, leaving an unsettling feeling of fate among your " +
                    "opponents as they start to notice a noxious odour in the wind. Due to the chaotic nature of whether manipulation, your team endures some showers as well. ")
            .build()),
    NOXIOUS_SHOWER_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, CHAOS)
            .setCost(new StatMap(0, 0, 0, 100))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, POISON, true, 12, .6, true, .20, 1, .2, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(100), EffectLogic.Basic.GET)
            .setNegSelfEffects(new Effect[]{
                    new Effect(MULTI, POISON, true, 12, .6, true, .1, 1, .1, 1),
            })
            .setNegSelfEffectLogic(EffectCalc.BasicSelfNeg.GET(40), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.NOXIOUS_RAIN, AnimTime.NORMAL, AnimSpeed.NORMAL))
            .setDescription("The clouds darken overhead as an eerie light shines though, leaving an unsettling feeling of fate among your " +
                    "opponents as they start to notice a noxious odour in the wind. Due to the chaotic nature of whether manipulation, your team endures some showers as well.  " +
                    "Gold variant with slightly higher poison damage and chance.")
            .build()),
    FICKLE_FATE(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, CHAOS)
            .setCost(new StatMap(0, 0, 0, 100))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, MISFORTUNE, true, 8, .35, true, 1, 1, 1, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setNegSelfEffects(new Effect[]{
                    new Effect(MULTI, MISFORTUNE, true, 4, .35, true, .9, 1, 1, 1),
            })
            .setNegSelfEffectLogic(EffectCalc.BasicSelfNeg.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.CLOUDS_FATE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("A gloom is cast upon the land, everyone feels uneasy as if fates is knocking on the door. Luck is heavily lowered for all upon the field, though your fortune is better.")
            .build()),
    FICKLE_FATE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 4, CHAOS)
            .setCost(new StatMap(0, 0, 0, 120))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, MISFORTUNE, true, 10, .35, true, 1, 1, 1, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setNegSelfEffects(new Effect[]{
                    new Effect(MULTI, MISFORTUNE, true, 6, .35, true, .9, 1, 1, 1),
            })
            .setNegSelfEffectLogic(EffectCalc.BasicSelfNeg.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.CLOUDS_FATE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("A gloom is cast upon the land, everyone feels uneasy as if fates is knocking on the door. " +
                    "Luck is heavily lowered for all upon the field, may this some how be and means to an end. ")
            .build()),
    TECTONIC_TORMENT(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, CHAOS)
            .setCost(new StatMap(0, 0, 0, 100))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, DE_CORE, true, 12, .35, true, .5, 1, 0.5, 1),
                    new Effect(MULTI, DE_FORTIFY, true, 12, .35, true, .5, 1, .5, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(15), EffectLogic.Basic.GET)
            .setNegSelfEffects(new Effect[]{
                    new Effect(MULTI, DE_CORE, true, 8, .35, true, .5, 1, 0.5, 1),
                    new Effect(MULTI, DE_FORTIFY, true, 8, .35, true, .5, 1, .5, 1),
            })
            .setNegSelfEffectLogic(EffectCalc.BasicSelfNeg.GET(15), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.TECTONIC, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("The ground begins to shake, and armor begins to rattle. Your opponents notice a loss in their strength and new cracks in their armor." +
                    " Your teams feels some of the tremors as well.")
            .build()),
    TECTONIC_TORMENT_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, CHAOS)
            .setCost(new StatMap(0, 0, 0, 120))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, DE_CORE, true, 12, .35, true, .5, 1, 0.5, 1),
                    new Effect(MULTI, DE_FORTIFY, true, 12, .35, true, .5, 1, .5, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(20), EffectLogic.Basic.GET)
            .setNegSelfEffects(new Effect[]{
                    new Effect(MULTI, DE_CORE, true, 8, .35, true, .4, 1, 0.5, 1),
                    new Effect(MULTI, DE_FORTIFY, true, 8, .35, true, .4, 1, .5, 1),
            })
            .setNegSelfEffectLogic(EffectCalc.BasicSelfNeg.GET(20), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.TECTONIC, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("The ground begins to shake, and armor begins to rattle. It seems as if the quake will never stop. " +
                    "Your opponents notice a heavy loss in their strength and new cracks in their armor." +
                    "Your teams feels some of the tremors as well.")
            .build()),
    FUNERAL_PYRE(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 4, CHAOS)
            .setCost(new StatMap(0, 0, 0, 140))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(900, 0, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSelfDamage(SINGLE, new StatMap(450, 0, 0, 0, 1, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.PYRE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Casting a scorching blast of fire to your opponent you feel a strong heat upon their brow, " +
                    "leaving ashes and embers swirling in the air. Causes devastation to your enemy at a large cost and chance of self damage.")
            .build()),
    FUNERAL_PYRE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 4, CHAOS)
            .setCost(new StatMap(0, 0, 0, 160))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(900, 0, 0, 0, 1, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSelfDamage(SINGLE, new StatMap(375, 0, 0, 0, .9, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(1), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.PYRE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Casting a scorching blast of fire to your opponent you feel a strong heat upon their brow, " +
                    "leaving ashes and embers swirling in the air. Causes devastation to your enemy at a large cost and " +
                    "chance of self damage. Gold variant with higher cost and slightly less risk to self.")
            .build()),
    WINDS_OF_CONFUSION(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 1, CHAOS)
            .setCost(new StatMap(0, 0, 0, 100))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, CONFUSION, true, .2, .35, true, 1, 1, 1, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setNegSelfEffects(new Effect[]{
                    new Effect(MULTI, CONFUSION, true, .1, .35, true, .9, 1, 1, 1),
            })
            .setNegSelfEffectLogic(EffectCalc.BasicSelfNeg.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.WINDS_CONFUSION, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Strong winds begin to stir, as debris fills the air confusion begins to set in on the field." +
                    " Causes moderate confusion to enemies, as well as some to your own team albeit less as they were mentally prepared.")
            .build()),
    WINDS_OF_CONFUSION_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, CHAOS)
            .setCost(new StatMap(0, 0, 0, 140))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, CONFUSION, true, .5, .35, true, 1, 1, 1, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setNegSelfEffects(new Effect[]{
                    new Effect(MULTI, CONFUSION, true, .25, .35, true, .9, 1, 1, 1),
            })
            .setNegSelfEffectLogic(EffectCalc.BasicSelfNeg.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.WINDS_CONFUSION, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Gale force wins take over the land, as debris fills the air confusion begins to set in on the field." +
                    " Causes heavy confusion to enemies, as well as moderate confusion to your own team albeit less as they were mentally prepared." +
                    " Gold variant causes more confusion for a higher cost, with your own team less affected.")
            .build()),
    PURE_BEFUDDLEMENT(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, CHAOS)
            .setCost(new StatMap(0, 0, 0, 100))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, CONFUSION, true, 0.65, .5, true, 1, 1, 1, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.FOG_BEFUDDLE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("A fog of confusion settles around your opponent leaving them extremely disoriented.")
            .build()),
    PURE_BEFUDDLEMENT_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, CHAOS)
            .setCost(new StatMap(0, 0, 0, 160))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, CONFUSION, true, 1, .35, true, 1, 1, 1, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.FOG_BEFUDDLE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("A fog of confusion settles around your opponent leaving them extremely disoriented, leaving" +
                    " not even a single thought to be reckoned with. Gold variant deals complete confusion for a high cost.")
            .build()),
    VOLTAIC_BLAST(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, CHAOS)
            .setCost(new StatMap(0, 0, 0, 90))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(200, 0, 0, 0, 1, 1, 0.5,1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSelfDamage(MULTI, new StatMap(100, 0, 0, 0, .5, 1, .5, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, PARALYSIS, true, .35, .45, true, .3, 1, .3, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setNegSelfEffects(new Effect[]{
                    new Effect(MULTI, PARALYSIS, true, .35, .45, true, .15, 1, .15, 1),
            })
            .setNegSelfEffectLogic(EffectCalc.BasicSelfNeg.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.BLAST_ELECTRIC, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("A large ball of electric hovers over your opponents, in a large blinding flash it releases " +
                    "it's energy causing damage and possible paralysis. Due to the chaotic nature of electricity, you team is at risk as well. ")
            .build()),
    VOLTAIC_BLAST_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, CHAOS)
            .setCost(new StatMap(0, 0, 0, 120))
            .setCostLogic(Cost.GET)
            .setDamage(MULTI, new StatMap(250, 0, 0, 0, 1, 1, 0.5, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(1), DamageLogic.Basic.GET)
            .setSelfDamage(MULTI, new StatMap(120, 0, 0, 0, .5, 1, .5, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(1), DamageLogic.Basic.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, PARALYSIS, true, .5, .3, true, .4, 1, .4, 1),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setNegSelfEffects(new Effect[]{
                    new Effect(MULTI, PARALYSIS, true, .4, .3, true, .2, 1, .2, 1),
            })
            .setNegSelfEffectLogic(EffectCalc.BasicSelfNeg.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.BLAST_ELECTRIC, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("A large ball of electric hovers over your opponents, in a large blinding flash it releases " +
                    "it's energy causing damage and possible paralysis. Due to the chaotic nature of electricity, you team is at risk as well." +
                    " Gold variant with a higher risk of damage and paralysis to all parties involved.")
            .build()),
    PROTON_ZAP(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 1, ORDER)
            .setCost(new StatMap(0, 0, 0, 60))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(50, 0, 0, 0, .25, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(40), DamageLogic.Basic.GET)
            .setSelfDamage(SINGLE, new StatMap(50, 0, 0, 0, .1, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(40), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.BOLT_ELECTRIC, AnimTime.SHORT, AnimSpeed.EXTRA_FAST))
            .setDescription("You unleash a series of electric rays towards you enemy, getting shocked yourself in the process.")
            .build()),
    PROTON_ZAP_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, ORDER)
            .setCost(new StatMap(0, 0, 0, 80))
            .setCostLogic(Cost.GET)
            .setDamage(SINGLE, new StatMap(75, 0, 0, 0, .25, 1))
            .setDamageLogic(DamageCalc.BasicTarget.GET(40), DamageLogic.Basic.GET)
            .setSelfDamage(SINGLE, new StatMap(75, 0, 0, 0, .05, 1))
            .setSelfDamageLogic(DamageCalc.BasicSelf.GET(40), DamageLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.BOLT_ELECTRIC, AnimTime.SHORT, AnimSpeed.EXTRA_FAST))
            .setDescription("You unleash a series of electric rays towards you enemy, getting shocked yourself in the process." +
                    " Gold Variant with higher damage and less self change for more cost")
            .build());


    private final CardStats stats;

    public static final String prefix = "ACT";
    private  String uid = null;

    ActionCard(CardStats stats) {
        this.stats = stats;
    }

    @Override
    public ActionReturn playCard(PlayerGameState player, PlayerGameState target, PawnIndex playerIdx, PawnIndex
            targetIdx) {
        // Call the default implementation with included stats
        return playCard(player, target, playerIdx, targetIdx, stats);
    }

    public CardStats getStats(){
        return stats;
    }

    public int getLevel() {
        return stats.getLevel();
    }

    public String getUid() {
        if (uid == null) { uid = prefix  + "-" + CardUtil.getHash(name()); }
        return uid;
    }

    public String getName() {
        return this.name();
    }


//    public String toStringLog() {
//        return this + "{" +
//                "damageClass=" + stats.damageClass +
//                ", actionType=" + stats.actionType +
//                ", selfDamageClass=" + stats.selfDamageClass +
//                ", animation=" + stats.animation +
//                ", level=" + stats.level +
//                ", cost=" + stats.cost +
//                ", damage=" + stats.damage +
//                ", selfDamage=" + stats.selfDamage +
//                '}';
//    }


    public ObjectNode toJson() {
        var json = JsonUtils.getMapper();
        var obj = json.createObjectNode();
        obj.put("uid", getUid());
        obj.put("is_gold", this.toString().contains("GOLD"));
        obj.put("damage_class",stats.getDamageClass() != null ? stats.getDamageClass().toString() : "N/A");
        obj.put("is_player", stats.isPlayer());
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

