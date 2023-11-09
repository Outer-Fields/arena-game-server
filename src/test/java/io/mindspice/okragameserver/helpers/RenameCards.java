package io.mindspice.okragameserver.helpers;

import io.mindspice.okragameserver.game.cards.Card;
import io.mindspice.okragameserver.util.CardUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class RenameCards {

    String dirPath = "/mnt/nas-personal/okra_assets/cards/jpgs";

    private Map<String, String> getTable(String card) {
        switch (card.substring(0, 3)) {
            case "ACT" -> { return CardUtil.actionCardNameTable; }
            case "ABL" -> { return CardUtil.abilityCardNameTable; }
            case "PAW" -> { return CardUtil.pawnCardNameTable; }
            case "TAL" -> { return CardUtil.talismanCardNameTable; }
            case "WEP" -> { return CardUtil.weaponCardNameTable; }
            case "POW" -> { return CardUtil.powerCardNameTable; }
            default -> { throw new IllegalStateException("No found"); }
        }
    }

    private static String removeFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }

    @Test
    void rename() {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();

        for (var file : files) {
            var name = removeFileExtension(file.getName());
            var newName = getTable(name).get(name);
            var newFile = new File(dirPath + File.separator + newName + ".jpg");
            file.renameTo(newFile);
        }


    }
}
