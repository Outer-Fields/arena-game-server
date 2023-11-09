package io.mindspice.okragameserver.game.gameroom.gameutil;

import io.mindspice.okragameserver.game.enums.StatType;

import java.util.EnumMap;

public interface Regenerate {
        EnumMap<StatType, Integer> regeneration(EnumMap<StatType, Integer> stats);
}
