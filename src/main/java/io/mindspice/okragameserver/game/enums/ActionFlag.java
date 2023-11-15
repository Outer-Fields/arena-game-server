package io.mindspice.okragameserver.game.enums;

import java.util.List;
import java.util.stream.Collectors;


public enum ActionFlag {
    REFLECTED(1),
    RESISTED(1),
    _2X(1),
    DAMAGED(1),
    EFFECTED(1),
    DE_BUFF(1),
    DRAIN(1),
    SIPHON(1),
    SUCCESS(1), //returns for effects
    HEAL(1),
    UNSUCCESSFUL(0),
    CONFUSED(0),
    PARALYZED(0),
    SHIELDED(0),
    CURE(1),
    BUFF(1),
    CURSED(1);

    public final int value;

    ActionFlag(int value) { this.value = value; }

    public int getValue() { return value; }

    public static int getMultiSum(List<ActionFlag> flags) {
        return flags.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.summingInt(ActionFlag::getValue)))
                .values()
                .stream()
                .max(Integer::compareTo)
                .orElse(0);
    }
}

