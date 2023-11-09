package io.mindspice.okragameserver.game.enums;

public class GameConst {
    public static final int ACTION_MIN = 18;
    public static final int ACTION_MAX = 24;
    public static final int ABILITY_MIN = 9;
    public static final int ABILITY_MAX = 24;
    public static final int POWER_MIN = 9;
    public static final int POWER_MAX = 12;
    public static final double DECK_LIMIT = 3;
    public static final int RELATIVE_STAT_SCALE_ALT = 4;
    public static final int RELATIVE_STAT_SCALE_LP = 100;
    public static final int RELATIVE_STAT_SCALE_WP = 100;
    public static final int EXTRA_LIGHT_FX = 25;
    public static final int LIGHT_FX = 50;
    public static final int MODERATE_FX = 150;
    public static final int HEAVY_FX = 300;

    public static int[] getDomainMinMax(CardDomain domain) {
        switch (domain) {
            case ACTION -> {
                return new int[]{ACTION_MIN, ACTION_MAX};
            }
            case ABILITY -> {
                return new int[]{ABILITY_MIN, ABILITY_MAX};
            }
            case POWER -> {
                return new int[]{POWER_MIN, POWER_MAX};
            }
        }
        throw new IllegalArgumentException("No Values For Domain");
    }
}
