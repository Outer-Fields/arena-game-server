package io.mindspice.okragameserver.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mindspice.okragameserver.game.cards.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CardExport {

    public static void main(String[] args) throws JsonProcessingException {
        var json = new ObjectMapper();
        File userDir = new File(System.getProperty("user.dir") + File.separator + "CardJson"+ File.separator);
        userDir.mkdirs();


        /* Action Cards */
        var actionCards = json.createObjectNode();
        for (ActionCard a : ActionCard.values()) {
            actionCards.putIfAbsent(a.name(), a.toJson());
        }

        try (final FileWriter fw = new FileWriter(new File (userDir,"action_cards.json"))) {
            fw.write(json.writeValueAsString(actionCards));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /* Ability Cards */
        var abilityCards = json.createObjectNode();
        for (AbilityCard a : AbilityCard.values()) {
            abilityCards.putIfAbsent(a.name(), a.toJson());
        }
        try (final FileWriter fw = new FileWriter(new File (userDir, "ability_cards.json"))) {
            fw.write(json.writeValueAsString(abilityCards));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /* Pawn Cards */
        var pawnCards = json.createObjectNode();
        for (PawnCard p : PawnCard.values()) {
            pawnCards.putIfAbsent(p.name(), p.toJson());
        }
        try (final FileWriter fw = new FileWriter(new File (userDir,"pawn_cards.json"))) {
            fw.write(json.writeValueAsString(pawnCards));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /* Power Cards */
        var powerCards = json.createObjectNode();
        for (PowerCard p : PowerCard.values()) {
            powerCards.putIfAbsent(p.name(), p.toJson());
        }
        try (final FileWriter fw = new FileWriter(new File (userDir, "power_cards.json"))) {
            fw.write(json.writeValueAsString(powerCards));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /* Weapon Cards */
        var weaponCards = json.createObjectNode();
        for (WeaponCard w : WeaponCard.values()) {
            var obj = w.toJson();
            weaponCards.putIfAbsent(w.name(), obj);
        }
        try (final FileWriter fw = new FileWriter(new File (userDir, "weapon_cards.json"))) {
            fw.write(json.writeValueAsString(weaponCards));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /* Talisman Cards */
        var talismanCards = json.createObjectNode();
        for (TalismanCard t : TalismanCard.values()) {
            talismanCards.putIfAbsent(t.name(), t.toJson());
        }
        try (final FileWriter fw = new FileWriter(new File (userDir, "talisman_cards.json"))) {
            fw.write(json.writeValueAsString(talismanCards));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
