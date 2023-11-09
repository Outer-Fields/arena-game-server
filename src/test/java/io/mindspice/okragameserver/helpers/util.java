package io.mindspice.okragameserver.helpers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.cards.*;
import io.mindspice.okragameserver.game.enums.EffectType;
import io.mindspice.okragameserver.game.enums.PowerEnums;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class util {

    @Test
    public void cardCount() {
        int count = ActionCard.values().length + AbilityCard.values().length + PawnCard.values().length
                + TalismanCard.values().length + PowerCard.values().length + WeaponCard.values().length;

        System.out.println("Card Count:" + count);
    }

    @Test
    public void addUid() throws IOException {
        var table = new Hashtable<String, String>(240);

        table.putAll(Arrays.stream(ActionCard.values()).collect(Collectors.toMap(ActionCard::name, ActionCard::getUid)));
        table.putAll(Arrays.stream(AbilityCard.values()).collect(Collectors.toMap(AbilityCard::name, AbilityCard::getUid)));
        table.putAll(Arrays.stream(WeaponCard.values()).collect(Collectors.toMap(WeaponCard::name, WeaponCard::getUid)));
        table.putAll(Arrays.stream(TalismanCard.values()).collect(Collectors.toMap(TalismanCard::name, TalismanCard::getUid)));
        table.putAll(Arrays.stream(PawnCard.values()).collect(Collectors.toMap(PawnCard::name, PawnCard::getUid)));
        table.putAll(Arrays.stream(PowerCard.values()).collect(Collectors.toMap(PowerCard::name, PowerCard::getUid)));
        System.out.println(table.size());

//        File json = new File("/home/mindspice/code/Python/PycharmProjects/card_image_work/cards.json");
//        var arr = (ArrayNode) JsonUtils.getMapper().readTree(json);
//
//        arr.forEach(c -> {
//                    var uid = table.get(c.get("name").asText());
//                    if (uid == null) { throw new IllegalStateException("this shouldnt happen: " + c.get("name")); }
//                    ((ObjectNode) c).put("uid", uid);
//                    System.out.println(c);
//                }
//        );

        //JsonUtils.getMapper().writeValue(json,arr);

    }

    @Test
    public void scratch() {
        Arrays.stream(PowerEnums.PowerType.values()).forEach(e -> {
            System.out.println(" - **" + e  + "**");
        });
    }
}
