package io.mindspice.okragameserver.cards;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.cards.WeaponCard;
import io.mindspice.okragameserver.game.enums.StatType;
import org.junit.jupiter.api.Test;

import javax.swing.text.html.parser.Entity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class CardBalance {
    HashMap<Integer, String> info = new HashMap<>();

 //   @Test
//    void weaponCards() throws JsonProcessingException {
//        for (var card : WeaponCard.values()) {
//            var cost = card.getStats().getCost().getSum();
//            var damage = card.getStats().getHpDamage();
//            var selfDamageDamage = card.getStats().getSelfDamage().asMap().get(StatType.HP);
//
//            var json = new JsonUtils.ObjectBuilder()
//                    .put("Card", card)
//                    .put("Level", card.getLevel())
//                    .put("Cost", cost)
//                    .put("damage_per_cost", (damage - selfDamageDamage) / cost)
//                    .put("damage_per_level", (damage - selfDamageDamage) / card.getLevel())
//                    .put("total_damage", (damage - selfDamageDamage) * card.getStats().getDamage().chance + card.getStats().getDamageCalc())
//                    .buildNode();
//
//            info.put((damage - selfDamageDamage), JsonUtils.writePretty(json));
//        }
//        List<Map.Entry<Integer, String>> sortedEntries = info.entrySet().stream()
//                .sorted(Map.Entry.comparingByKey())
//                .toList();
//
//        sortedEntries.forEach(e -> System.out.println(e.getValue()));
//    }


}
