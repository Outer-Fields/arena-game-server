package io.mindspice.okragameserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.enums.CardDomain;
import io.mindspice.okragameserver.schema.PawnSet;
import io.mindspice.okragameserver.util.CardUtil;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;


public class CardMapping {



    List<String> cards = List.of(
            "ACT-275670846430183", "ACT-28700681008090", "ABL-183721805757410", "ACT-139932657369375", "ACT-32372890799686",
            "ABL-227116670596555", "ABL-10401708053617", "ABL-37080939889100", "PAW-156156220038764", "ABL-208085012070330",
            "POW-190031102977975", "WEP-49610250362158", "ACT-45253279202786", "ACT-6987037181225", "ABL-131564875880620",
            "POW-250976682642965", "ABL-227250694798862", "WEP-64217806514507", "ACT-1658559249646", "POW-128505338223781",
            "ABL-9892535662219", "ACT-167940368574057", "TAL-160825585306683", "ACT-250274965707442", "ACT-214993741124056",
            "ACT-128252634017031", "PAW-126068739711401", "ABL-246646482234622", "ABL-185414460738295", "PAW-126068739711401",
            "POW-12723791230524", "ABL-189756490761286", "ABL-111427808278427", "ACT-32372890799686", "POW-250976682642965",
            "ABL-189756490761286", "TAL-160825585306683", "ACT-218805150922745", "POW-53872258785316", "ABL-111427808278427",
            "POW-53872258785316", "ACT-10503915256970", "WEP-240460624230808", "WEP-28293457107181", "ACT-8817470192490",
            "ABL-10401708053617", "WEP-49610250362158", "ABL-10401708053617", "WEP-64217806514507", "ABL-227116670596555",
            "ACT-219704736912549", "ABL-131564875880620", "POW-53872258785316", "TAL-94761991510048", "POW-76580047894849",
            "ACT-128252634017031", "ACT-185299037019467");


    String pawnset = """
            {"set_num":0,"set_name":"PawnSet 1","pawn_loadouts":[{"pawn_card":"IMPERIAL_RANGER","talisman_card":"STABILITY_STONE","weapon_card_1":"LONGBOW_OF_EXCELLENCE","weapon_card_2":"LONGBOW_OF_EXCELLENCE","action_deck":["SLUMBEROUS_SHOT_GOLD","POISON_SHOT","SWIFT_SHOT","POISON_SHOT","STRANGE_BREW","ENFEEBLING_SHOT"],"ability_deck":["POISON_REMEDY","REJUVENATING_LIGHT_GOLD","RELEGATION_OF_DESTINY","ARMS_TRADE","REJUVENATING_LIGHT_GOLD","SACRIFICIAL_DEMISE"],"power_deck":["MIRROR_SKIN","STONE_WALL","THE_UNSEEN"],"valid":true},{"pawn_card":"IMPERIAL_MAGI","talisman_card":"RELIC_OF_THE_ARCANE","weapon_card_1":"GLOVES_OF_INFERNO","weapon_card_2":"STAFF_OF_FORTILITY","action_deck":["SLEEP_RELEASE","CLOUD_OF_ILL_FATE","SPIT_FIRE","FICKLE_FATE_GOLD","VITALITY_DRAIN","PROTON_ZAP"],"ability_deck":["SACRIFICIAL_DEMISE","POWER_PLUNDER","WARRIORS_TURMOIL","SACRIFICIAL_DEMISE","WARRIORS_TURMOIL","CORE_VIGOR_GOLD"],"power_deck":["STONE_WALL","RANGED_MASTER","STONE_WALL"],"valid":true},{"pawn_card":"IMPERIAL_MAGI","talisman_card":"RELIC_OF_THE_ARCANE","weapon_card_1":"STAFF_OF_HEALING","weapon_card_2":"STAFF_OF_FORTILITY","action_deck":["AURA_OF_DESTRUCTION","VITALITY_DRAIN","DEFENSE_SAP","SLEEP_RELEASE_GOLD","ARC_LIGHTNING","LIFE_SAP"],"ability_deck":["RANGERS_TURMOIL","FORTUNES_FAVOR","ORACLE_OF_HAND","MAGES_TURMOIL","POWER_PLUNDER","RELEGATION_OF_DESTINY"],"power_deck":["CELESTIAL_SHIELD","THE_UNSEEN","IRON_JAW"],"valid":true}],"set_level":116}
            """;


    @Test
    void testMap() throws JsonProcessingException {
//        var mapped = CardUtil.playerMappedCards(cards);
//        // System.out.println(mapped);
//        var map = Map.of(1, pawnset);
//
//        Map<CardDomain, List<String>> muteMap = mapped.second().entrySet().stream()
//                .collect(Collectors.toMap(
//                        Map.Entry::getKey,
//                        entry -> new ArrayList<>(entry.getValue())
//                ));
//
//        PawnSet pset = map.entrySet().stream().map(PawnSet::fromJsonEntry).toList().get(0).get();
//        assert (pset.validate(muteMap).first());
    }

}