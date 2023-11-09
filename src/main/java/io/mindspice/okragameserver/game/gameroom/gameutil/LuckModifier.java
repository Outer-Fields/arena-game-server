package io.mindspice.okragameserver.game.gameroom.gameutil;

import java.util.concurrent.ThreadLocalRandom;

public class LuckModifier {

    public static double luckMod(int luck) {
        double[] values = luckModCalc(luck);
        double floor = values[0];
        double ceiling = values[1] ;

        return (1 + ThreadLocalRandom.current().nextDouble(floor, ceiling + .01));
    }

    public static double inverseLuckMod(int luck) {
        double[] values = luckModCalc(luck);
        double floor = values[0];
        double ceiling = values[1] ;

        return (1 - ThreadLocalRandom.current().nextDouble(floor, ceiling + .01));
    }

    public static boolean chanceCalc(double chance, int luck) {
        double luckChance = chance * luckMod(luck);
        double rng = ThreadLocalRandom.current().nextDouble(1);
        return luckChance >= rng;
    }

    public static boolean inverseChanceCalc(double chance, int luck) {
        double luckChance = chance * inverseLuckMod(luck);
        double rng = ThreadLocalRandom.current().nextDouble(1);
        return luckChance >= rng;
    }


    //TODO maybe add check for bad values here
    //returns {floor,ceiling}
    public static double[] luckModCalc(int luck) {
        int checkedLuck; // check if too high or low from being scaled

        if (luck > 20){
            checkedLuck = 20;
        } else if (luck < 0) {
            checkedLuck = 0;
        } else {
            checkedLuck = luck;
        }
        double[] values = new double[2];

        switch (checkedLuck) {
            case 0 -> {
                values[0] = -0.3;
                values[1] = -0.1;
                return values;
            }
            case 1,2 -> {
                values[0] = -0.3;
                values[1] = 0;
                return values;
            }
            case 3,4 -> {
                values[0] = -0.25;
                values[1] = 0;
                return values;
            }
            case 5,6 -> {
                values[0] = -0.2;
                values[1] = 0.5;
                return values;
            }
            case 7,8 -> {
                values[0] = -0.15;
                values[1] = 0.1;
                return values;
            }
            case 9,10 -> {
                values[0] = -0.1;
                values[1] = 0.1;
                return values;
            }
            case 11,12 -> {
                values[0] = -0.05;
                values[1] = 0.15;
                return values;
            }
            case 13,14 -> {
                values[0] = -0.05;
                values[1] = 0.2;
                return values;
            }
            case 15,16 -> {
                values[0] = 0.0;
                values[1] = 0.25;
                return values;
            }
            case 17,18 -> {
                values[0] = 0.05;
                values[1] = 0.3;
                return values;
            }
            case 19,20 -> {
                values[0] = 0.1;
                values[1] = 0.3;
                return values;
            }
        }


        return values;
    }
}
