package io.mindspice.okragameserver.logic;

import io.mindspice.okragameserver.game.gameroom.gameutil.LuckModifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class LuckTests {

    @Test
    public void testLuckMod() {
        int iterations = 100000; // Number of iterations for each luck value
        for (int i = 0; i <= 20; i++) {
            int hits = 0;
            double[] expected = LuckModifier.luckModCalc(i);
            double lowerBound = 0.5 * (1 + expected[0]);
            double upperBound = 0.5 * (1 + expected[1] + 0.01);

            for (int j = 0; j < iterations; j++) {
                if (LuckModifier.chanceCalc(0.5, i)) {
                    hits++;
                }
            }

            double hitRate = hits / (double) iterations;
            System.out.println(lowerBound + " | " + hitRate + " | " + upperBound);
            assertTrue(hitRate >= lowerBound && hitRate <= upperBound);
        }
    }


    @Test
    public void testInverseLuckMod() {
        for (int i = 0; i <= 20; i++) {
            double sum = 0.0;
            double[] expected = LuckModifier.luckModCalc(i);
            double lowerBound = 1 - (expected[1] + 0.01);
            double upperBound = 1 - expected[0];

            for (int j = 0; j < 100000; j++) {
                double result = LuckModifier.inverseLuckMod(i);
                sum += result;
            }

            double average = sum / 100000;
            System.out.println(lowerBound + "|" +  average +  "|" + upperBound);
            assertTrue(average >= lowerBound && average <= upperBound);
        }
    }




    @Test
    public void testChanceCalc() {
        for (int i = 0; i <= 20; i++) {
            int hits = 0;
            for (int j = 0; j < 100000; j++) {
                if (LuckModifier.chanceCalc(0.5, i)) {
                    hits++;
                }
            }
            double hitRate = hits / 100000.0;
            double[] luckValues = LuckModifier.luckModCalc(i);
            double minExpectedRate = 0.5 * (1 + luckValues[0]);
            double maxExpectedRate = 0.5 * (1 + luckValues[1]);
            System.out.println(hitRate);
            assertTrue(hitRate >= minExpectedRate && hitRate <= maxExpectedRate);
        }
    }


    @Test
    public void testInverseChanceCalc() {
        for (int i = 0; i <= 20; i++) {
            int hits = 0;
            for (int j = 0; j < 100000; j++) {
                if (LuckModifier.inverseChanceCalc(0.5, i)) {
                    hits++;
                }
            }
            double hitRate = hits / 100000.0;
            System.out.println(hitRate);
            double[] luckValues = LuckModifier.luckModCalc(i);
            double minExpectedRate = 0.5 * (1 - luckValues[1]);
            double maxExpectedRate = 0.5 * (1 - luckValues[0]);
            assertTrue(hitRate >= minExpectedRate && hitRate <= maxExpectedRate);
        }
    }

    @Test
    public void inverseTest() {
        int h = 0;
        for (int i = 0; i < 100; ++i) {
            if (LuckModifier.chanceCalc(0.95, 20)) {
                h++;
            }
        }

        System.out.println(h);
    }

    @Test
    public void debugInverseTest() {
        int hits = 0;
        for (int i = 0; i < 1000; ++i) {
            if (LuckModifier.inverseChanceCalc(1.5, 20)) {
                hits++;
            }
        }
        System.out.println(hits);
    }

}