package io.mindspice.okragameserver.util;

import io.mindspice.databaseservice.client.schema.Reward;
import io.mindspice.mindlib.data.tuples.Pair;

import java.util.concurrent.ThreadLocalRandom;


public class RewardCalc {
    public static Pair<Reward, Integer> getReward(int currWins) {
        var rand = ThreadLocalRandom.current().nextDouble(1);

        switch (currWins) {
            case 1 -> { return new Pair<>(Reward.OKRA, 500); }
            case 2 -> { return new Pair<>(Reward.OUTR, 200); }
            case 3 -> { return new Pair<>(Reward.NFT, 2); }
            case 4 -> { return new Pair<>(Reward.OKRA, 500); }
            case 5 -> { return new Pair<>(Reward.OUTR, 100); }
            case 6 -> { return new Pair<>(Reward.NFT, 1); }
            case 7 -> { return new Pair<>(Reward.OKRA, 500); }
            case 8 -> { return new Pair<>(Reward.OUTR, 100); }
            case 9 -> { return new Pair<>(Reward.NFT, 1); }
            case 10 -> { return new Pair<>(Reward.OKRA, 500); }
            case 11 -> { return new Pair<>(Reward.OUTR, 100); }
            case 12 -> { return new Pair<>(Reward.NFT, 1); }
            case 13 -> { return new Pair<>(Reward.OKRA, 500); }
            case 14 -> { return new Pair<>(Reward.OUTR, 100); }
            default -> {
                if (rand <= 0.3333) {
                    return new Pair<>(Reward.NFT, 1);
                } else if (rand <= 0.6666) {
                    return new Pair<>(Reward.OKRA, 200);
                } else {
                    return new Pair<>(Reward.OUTR, 50);
                }
            }
        }
    }

    public static Pair<Reward, Integer> getBasicReward(int currWins) {
        var rand = ThreadLocalRandom.current().nextDouble(1);
        switch (currWins) {
            case 1 -> { return new Pair<>(Reward.OKRA, 250); }
            case 2 -> { return new Pair<>(Reward.OUTR, 100); }
            case 3 -> { return new Pair<>(Reward.NFT, 1); }
            case 4 -> { return new Pair<>(Reward.OKRA, 250); }
            case 5 -> { return new Pair<>(Reward.OUTR, 50); }
            case 6 -> { return new Pair<>(Reward.NFT, 1); }
            case 7 -> { return new Pair<>(Reward.OKRA, 250); }
            case 8 -> { return new Pair<>(Reward.OUTR, 50); }
            case 9 -> { return new Pair<>(Reward.NFT, 1); }
            case 10 -> { return new Pair<>(Reward.OKRA, 250); }
            case 11 -> { return new Pair<>(Reward.OUTR, 50); }
            case 12 -> { return new Pair<>(Reward.NFT, 1); }
            case 13 -> { return new Pair<>(Reward.OKRA, 250); }
            case 14 -> { return new Pair<>(Reward.OUTR, 50); }
            default -> {
                if (rand <= 0.3333) {
                    return new Pair<>(Reward.NFT, 1);
                } else if (rand <= 0.6666) {
                    return new Pair<>(Reward.OKRA, 100);
                } else {
                    return new Pair<>(Reward.OUTR, 25);
                }
            }
        }

    }
}
