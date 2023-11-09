package io.mindspice.okragameserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.cards.*;

import io.mindspice.okragameserver.game.enums.StatType;
import io.mindspice.okragameserver.game.gameroom.GameRoom;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import io.mindspice.okragameserver.testutil.States;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static io.mindspice.okragameserver.game.enums.StatType.HP;


public class ScratchTests {
    GameRoom game = States.getReadiedGameRoom();

    @Test
    void testHighLow() {
        game.getPlayer1().getPawns().forEach(
                p -> System.out.println(p.getIndex() + " AP: " + p.getActionPotential() + " HP:" + p.getStat(StatType.HP))
        );
        game.getPlayer1().getPawns().stream()
                .min(Comparator.comparingDouble(Pawn::getActionPotential).reversed()
                             .thenComparing(p -> p.getStat(HP)))
                .map(pawn -> {
                    System.out.println(pawn.getIndex());
                    return true;
                })
                .orElse(false);
    }

    @Test
    void statJson() throws IOException {
        Map<String, String> infos = new HashMap<>();
        JsonUtils.getMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        JsonUtils.getMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        for (var card : PawnCard.values()) { infos.put(card.name(), JsonUtils.writePretty(card.stats)); }
        for (var card : WeaponCard.values()) { infos.put(card.name(), JsonUtils.writePretty(card.getStats())); }
        for (var card : ActionCard.values()) { infos.put(card.name(), JsonUtils.writePretty(card.getStats())); }
        for (var card : AbilityCard.values()) { infos.put(card.name(), JsonUtils.writePretty(card.getStats())); }
        for (var card : PowerCard.values()) { infos.put(card.name(), JsonUtils.writePretty(card.powers)); }
        for (var card : TalismanCard.values()) { infos.put(card.name(), JsonUtils.writePretty(Collections.singletonMap(card.statChange, card.maxChange))); }


        writeMapToJsonFile(infos, "card_info.json");
    }

    public static void writeMapToJsonFile(Map<String, String> map, String fileName) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("{\n");
            boolean isFirst = true;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (!isFirst) {
                    writer.write(",\n");
                }
                writer.write("\"" + entry.getKey() + "\": " + entry.getValue());
                isFirst = false;
            }
            writer.write("\n}");
        }
    }

    public static void main(String[] args) throws IOException {
        Map<String, String> infos = new HashMap<>();
        // Populate your map here
        writeMapToJsonFile(infos, "output.json");
    }


    @Test
    void cloneTest(){
        var map1 = new EnumMap<StatType, Integer>(StatType.class);
        map1.put(HP, 1000);
        var map2 = map1.clone();

//        for (var entry : map1.entrySet()) {
//            map2.put(entry.getKey(), entry.getValue());
//        }
        var oldvalue = map1.get(HP);
        oldvalue += 100;
        map2.put(HP, map1.get(HP) * 2);

        System.out.println(map1);
        System.out.println(map2);
    }




}


