package io.mindspice.okragameserver.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.core.Settings;
import io.mindspice.okragameserver.game.cards.*;
import io.mindspice.okragameserver.game.enums.ActionType;
import io.mindspice.okragameserver.game.enums.Alignment;
import io.mindspice.okragameserver.game.enums.CardDomain;
import io.mindspice.okragameserver.util.CardUtil;
import io.mindspice.okragameserver.util.Log;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static io.mindspice.okragameserver.game.enums.ActionType.RANGED;


public record PawnSet(
        @JsonProperty("set_num") int setNum,
        @JsonProperty("set_name") String setName,
        @JsonProperty("pawn_loadouts") PawnLoadOut[] pawnLoadouts,
        @JsonProperty("set_level") int setLevel
) {
    public PawnSet(@JsonProperty("set_num") int setNum, @JsonProperty("set_name") String name,
            @JsonProperty("pawn_loadouts") PawnLoadOut[] pawns) {
        this(setNum, name, pawns, getSetLevel(pawns));
    }

    public PawnSet {
        if (setName == null) { setName = "nully"; }
        if (setName.length() > 15) { setName = setName.substring(0, 15); }
        setLevel = getSetLevel(pawnLoadouts);
    }

    public List<String> toList() {
        var cardList = new ArrayList<String>();
        for (PawnLoadOut p : pawnLoadouts) {
            var pawnCards = new ArrayList<String>();
            pawnCards.add(p.pawnCard().name());
            pawnCards.add(p.weaponCard1().name());
            pawnCards.add(p.weaponCard2().name());
            pawnCards.add(p.talismanCard().name());
            pawnCards.addAll(p.actionDeck().stream().map(Enum::name).toList());
            pawnCards.addAll(p.abilityDeck().stream().map(Enum::name).toList());
            pawnCards.addAll(p.powerDeck().stream().map(Enum::name).toList());
            cardList.addAll(pawnCards);
        }
        return cardList;
    }

    private static int getSetLevel(PawnLoadOut[] pawns) {
        if (pawns == null || pawns.length != 3) { return -1; }
        return Arrays.stream(pawns)
                .mapToInt(PawnLoadOut::sumLevel)
                .sum();
    }

    //    public static Optional<PawnSet> fromJsonString(String json) {
//        try {
//            JsonNode setData = JsonUtils.readTree(json);
//            String name = setData.get("set_name").asText();
//            PawnLoadOut[] pawnLoadOuts = JsonUtils.readJson(setData.get("pawn_loadouts"), PawnLoadOut[].class);
//            return Optional.of(new PawnSet())
//        } catch (IOException e) {
//            Log.SERVER.error(PawnSet.class, "Error deserializing json", e);
//            return Optional.empty();
//        }
//    }
//
    public static PawnSet fromJsonEntry(int index, String json) throws IOException {
        JsonNode setData = JsonUtils.readTree(json);
        String name = setData.get("set_name").asText();
        PawnLoadOut[] pawnLoadOuts = JsonUtils.readJson(setData.get("pawn_loadouts"), PawnLoadOut[].class);
        return new PawnSet(index, name, pawnLoadOuts);
    }

    public String toJsonString() throws JsonProcessingException {
        return JsonUtils.writeString(this);
    }

    // Ugly ass code incoming....
    public Pair<Boolean, String> validate(Map<CardDomain, List<String>> playerValidCards) {
        for (var pawn : pawnLoadouts) {
            if (pawn.weaponCard1() == null || pawn.weaponCard2() == null
                    || pawn.actionDeck() == null || pawn.abilityDeck() == null
                    || pawn.talismanCard() == null || pawn.pawnCard() == null
                    || pawn.powerDeck() == null) {
                return new Pair<>(false, "Null deck value found");
            }
            if (boundsError(Settings.GET().actionDeckBounds, pawn.actionDeck().size())
                    || boundsError(Settings.GET().abilityDeckBounds, pawn.abilityDeck().size())
                    || boundsError(Settings.GET().powerDeckBounds, pawn.powerDeck().size())) {
                Log.SERVER.debug(this.getClass(), "Deck size Out of Bounds");
                return new Pair<>(false, "Deck size Out of Bounds");
            }
            if (pawn.actionDeck().stream().mapToInt(Card::getLevel).sum() / pawn.actionDeck().size() > Settings.GET().maxActionDeckLevel
                    || pawn.abilityDeck().stream().mapToInt(Card::getLevel).sum() / pawn.abilityDeck().size() > Settings.GET().maxAbilityDeckLevel
                    || pawn.powerDeck().stream().mapToInt(Card::getLevel).sum() / pawn.powerDeck().size() > Settings.GET().maxPowerDeckLevel) {
                return new Pair<>(false, "Deck level to high");
            }

            if (!playerValidCards.get(CardDomain.WEAPON).remove(pawn.weaponCard1().name())
                    || !playerValidCards.get(CardDomain.WEAPON).remove(pawn.weaponCard2().name())
                    || !playerValidCards.get(CardDomain.PAWN).remove(pawn.pawnCard().name())
                    || !playerValidCards.get(CardDomain.TALISMAN).remove(pawn.talismanCard().name())) {
//                System.out.println(pawn.weaponCard1());
//                System.out.println(pawn.weaponCard2());
//                System.out.println(pawn.pawnCard());
//                System.out.println(pawn.talismanCard());
//                System.out.println(playerValidCards.get(CardDomain.WEAPON));
//                System.out.println(playerValidCards.get(CardDomain.PAWN));
//                System.out.println(playerValidCards.get(CardDomain.TALISMAN));
                return new Pair<>(false, "Un-owned card encountered in set: Pawn, WeaponCard(s) or Talisman");
            }

            var actionValid = hasUnOwnedCards(
                    playerValidCards.get(CardDomain.ACTION),
                    pawn.actionDeck().stream().map(ActionCard::name).toList()
            );
            if (!actionValid.first()) {
                return new Pair<>(false, "Un-owned card encountered in set: " + actionValid.second());
            }

            var abilityValid = hasUnOwnedCards(
                    playerValidCards.get(CardDomain.ABILITY),
                    pawn.abilityDeck().stream().map(AbilityCard::name).toList()
            );
            if (!abilityValid.first()) {
                return new Pair<>(false, "Un-owned card encountered in set: " + abilityValid.second());
            }

            var powerValid = hasUnOwnedCards(
                    playerValidCards.get(CardDomain.POWER),
                    pawn.powerDeck().stream().map(PowerCard::name).toList()
            );
            if (!powerValid.first()) {
                return new Pair<>(false, "Un-owned card encountered in set: " + powerValid.second());
            }
        }
        return new Pair<>(true, "");
    }

    private boolean boundsError(int[] bounds, int deckSize) {
        return deckSize < bounds[0] || deckSize > bounds[1];
    }

    private Pair<Boolean, String> hasUnOwnedCards(List<String> ownedCards, List<String> setCardUids) {
        for (String card : setCardUids) {
            if (!ownedCards.remove(card)) {
                return new Pair<>(false, card);
            }
        }
        return new Pair<>(true, "");
    }

    public static int getRandomLevel() {
        int rnd = ThreadLocalRandom.current().nextInt(130, 160);
        return (int) ThreadLocalRandom.current().nextDouble(0.93, 1.07) * rnd;
    }

    // CarUtil random card functions could return ownedCards that do not meet the arguments, this is done as a fallback
    // to avoid having to return null if nothing matching the arguments are found. Given the card and the current
    // implementation this will not happen, but documenting anyway.
    // FIXME this needs to use floats, was implemented think there would be a max level int cap, not an
    //  average float
    public static PawnSet getRandomPawnSet(int levelCap) {
        var start = System.currentTimeMillis();
        var rng = ThreadLocalRandom.current();
        int[] loudOutLevels = getLoadoutLevels(levelCap);
        PawnLoadOut[] pawnLoadOuts = new PawnLoadOut[3];
        List<PawnCard> pawnCards = IntStream.range(0, 3).mapToObj(i -> CardUtil.getRandomPawn()).toList();
        List<TalismanCard> talismanCards = IntStream.range(0, 3).mapToObj(i -> CardUtil.getRandomTalisman()).toList();

        for (int i = 0; i < 3; ++i) {
            int loadOutLevel = loudOutLevels[i];
            Alignment alignment = talismanCards.get(i).alignment;
            ActionType actionType = pawnCards.get(i).actionType;
            double alignSkew = alignment == Alignment.NEUTRAL ? 0.5 : rng.nextDouble(0.5, 0.7);
            // all the random CarUtil functions can return null
            WeaponCard weapon1 = CardUtil.getRandomWeapon(actionType, alignment);
            WeaponCard weapon2 = CardUtil.getRandomWeapon(actionType, skewAlign(alignSkew, alignment));
            loadOutLevel -= weapon1.getLevel();
            loadOutLevel -= weapon2.getLevel();

            int powerCount = rng.nextInt(Settings.GET().powerDeckBounds[0], Settings.GET().powerDeckBounds[1] + 1);
            int actionCount = rng.nextInt(Settings.GET().actionDeckBounds[0], Settings.GET().actionDeckBounds[1] + 1);
            int abilityCount = rng.nextInt(Settings.GET().abilityDeckBounds[0], Settings.GET().abilityDeckBounds[1] + 1);
            int powerLevel = (int) Math.round(Math.min(loadOutLevel * 0.145, Settings.GET().maxPowerDeckLevel) * powerCount);
            loadOutLevel -= powerLevel;
            int actionLevel = Math.min(loadOutLevel / 2, Settings.GET().maxActionDeckLevel * actionCount);
            int abilityLevel = Math.min(loadOutLevel / 2, Settings.GET().maxAbilityDeckLevel * abilityCount);

            List<PowerCard> powerCards = getLevelSet(powerLevel, powerCount)
                    .stream()
                    .map(CardUtil::getRandomPowerCard).toList();

            List<ActionCard> actionCards = getLevelSet(actionLevel, actionCount).stream()
                    .map(lvl -> CardUtil.getRandomActionCard(actionType, skewAlign(alignSkew, alignment), lvl)).toList();

            List<AbilityCard> abilityCards = getLevelSet(abilityLevel, abilityCount).stream()
                    .map(lvl -> CardUtil.getRandomAbilityCard(skewAlign(alignSkew, alignment), lvl)).toList();

            PawnLoadOut pawnLoadOut = new PawnLoadOut(
                    pawnCards.get(i),
                    talismanCards.get(i),
                    weapon1,
                    weapon2,
                    actionCards,
                    abilityCards,
                    powerCards
            );
            pawnLoadOuts[i] = pawnLoadOut;
        }
        return new PawnSet(-1, "BotSet", pawnLoadOuts, levelCap);
    }

    public static PawnSet getRandomPawnSet2() {
        List<PawnCard> pawnCards = IntStream.range(0, 3).mapToObj(i -> CardUtil.getRandomPawn()).toList();
        List<TalismanCard> talismanCards = new ArrayList<>(3);
        List<WeaponCard[]> weaponCards = new ArrayList<>(3);
        for (var pawn : pawnCards) {
            switch (pawn.actionType) {
                case MELEE -> {
                    switch (ThreadLocalRandom.current().nextInt(0, 4)) {
                        case 0 -> talismanCards.add(TalismanCard.WARRIORS_ORNAMENT);
                        case 1 -> talismanCards.add(TalismanCard.JEWEL_OF_CONQUEST);
                        case 2 -> talismanCards.add(TalismanCard.OKRITHRIAL_PENDANT_GOLD);
                        case 3 -> talismanCards.add((TalismanCard.OKRITHRIAL_PENDANT));
                    }
                }
                case MAGIC -> {
                    switch (ThreadLocalRandom.current().nextInt(0, 5)) {
                        case 0 -> talismanCards.add(TalismanCard.AMULET_OF_ARCANE_CHAOS);
                        case 1 -> talismanCards.add(TalismanCard.PENDANT_OF_DEFENSE_GOLD);
                        case 2 -> talismanCards.add(TalismanCard.PENDANT_OF_DEFENSE);
                        case 3 -> talismanCards.add((TalismanCard.RELIC_OF_THE_ARCANE));
                        case 4 -> talismanCards.add(TalismanCard.OKRITHRIAL_PENDANT_GOLD);
                    }
                }
                case RANGED -> {
                    switch (ThreadLocalRandom.current().nextInt(0, 5)) {
                        case 0 -> talismanCards.add(TalismanCard.PENDANT_OF_DEFENSE);
                        case 1 -> talismanCards.add(TalismanCard.JEWEL_OF_CONQUEST);
                        case 2 -> talismanCards.add(TalismanCard.FRAGMENT_OF_THE_LUCKMEISTER);
                        case 3 -> talismanCards.add((TalismanCard.OKRITHRIAL_PENDANT_GOLD));
                        case 4 -> talismanCards.add(TalismanCard.BALANCE_BEAD);
                    }
                }
            }
        }
        for (int i = 0; i < 3; ++i) {
            switch (pawnCards.get(i).actionType) {
                case MELEE -> {
                    switch (talismanCards.get(i).alignment) {
                        case Alignment.CHAOS -> {
                            if (ThreadLocalRandom.current().nextBoolean()) {
                                weaponCards.add(new WeaponCard[]{WeaponCard.OKRITHRIAL_GLADIUS, WeaponCard.DUMBBRINGERS_CUDGEL});
                            } else {
                                weaponCards.add(new WeaponCard[]{WeaponCard.KNUCKLEDUSTER, WeaponCard.MJOLNIR_HAMMER});
                            }
                        }
                        case ORDER -> {
                            if (ThreadLocalRandom.current().nextBoolean()) {
                                weaponCards.add(new WeaponCard[]{WeaponCard.OKRITHRIAL_GLADIUS_GOLD, WeaponCard.CUTLASS_OF_FURY_GOLD});
                            } else {
                                weaponCards.add(new WeaponCard[]{WeaponCard.ZWEIHANDER_OF_EXCELLENCE_GOLD, WeaponCard.CUTLASS_OF_FURY});
                            }
                        }
                        case NEUTRAL -> {
                            switch (ThreadLocalRandom.current().nextInt(0, 4)) {
                                case 0 -> weaponCards.add(new WeaponCard[]{WeaponCard.OKRITHRIAL_GLADIUS, WeaponCard.DUMBBRINGERS_CUDGEL});
                                case 1 -> weaponCards.add(new WeaponCard[]{WeaponCard.KNUCKLEDUSTER, WeaponCard.MJOLNIR_HAMMER});
                                case 2 ->
                                        weaponCards.add(new WeaponCard[]{WeaponCard.OKRITHRIAL_GLADIUS_GOLD, WeaponCard.CUTLASS_OF_FURY_GOLD});
                                case 3 ->
                                        weaponCards.add(new WeaponCard[]{WeaponCard.ZWEIHANDER_OF_EXCELLENCE_GOLD, WeaponCard.CUTLASS_OF_FURY});
                            }
                        }
                    }
                }

                case MAGIC -> {
                    switch (talismanCards.get(i).alignment) {
                        case Alignment.CHAOS -> {
                            if (ThreadLocalRandom.current().nextBoolean()) {
                                weaponCards.add(new WeaponCard[]{WeaponCard.STAFF_OF_ROUGE_SIPHON, WeaponCard.SCROLL_OF_MOLTEN_RAIN});
                            } else {
                                weaponCards.add(new WeaponCard[]{WeaponCard.SCROLL_OF_MOLTEN_RAIN_GOLD, WeaponCard.GLOVES_OF_INFERNO});
                            }
                        }
                        case ORDER -> {
                            if (ThreadLocalRandom.current().nextBoolean()) {
                                weaponCards.add(new WeaponCard[]{WeaponCard.GLOVES_OF_INFERNO, WeaponCard.STAFF_OF_THUNDERBOLT_GOLD});
                            } else {
                                weaponCards.add(new WeaponCard[]{WeaponCard.STAFF_OF_THUNDERBOLT, WeaponCard.GLOVES_OF_INFERNO});
                            }
                        }
                        case NEUTRAL -> {
                            switch (ThreadLocalRandom.current().nextInt(0, 4)) {
                                case 0 ->
                                        weaponCards.add(new WeaponCard[]{WeaponCard.STAFF_OF_ROUGE_SIPHON, WeaponCard.SCROLL_OF_MOLTEN_RAIN});
                                case 1 ->
                                        weaponCards.add(new WeaponCard[]{WeaponCard.SCROLL_OF_MOLTEN_RAIN_GOLD, WeaponCard.GLOVES_OF_INFERNO});
                                case 2 ->
                                        weaponCards.add(new WeaponCard[]{WeaponCard.GLOVES_OF_INFERNO, WeaponCard.STAFF_OF_THUNDERBOLT_GOLD});
                                case 3 -> weaponCards.add(new WeaponCard[]{WeaponCard.STAFF_OF_THUNDERBOLT, WeaponCard.GLOVES_OF_INFERNO});
                            }
                        }
                    }

                }
                case RANGED -> {
                    switch (talismanCards.get(i).alignment) {
                        case Alignment.CHAOS -> {
                            if (ThreadLocalRandom.current().nextBoolean()) {
                                weaponCards.add(new WeaponCard[]{WeaponCard.BOW_OF_POISONING, WeaponCard.JAVELIN_OF_MIGHT_GOLD});
                            } else {
                                weaponCards.add(new WeaponCard[]{WeaponCard.BOW_OF_SWIFTNESS, WeaponCard.SHADOW_DAGGER});
                            }
                        }
                        case ORDER -> {
                            if (ThreadLocalRandom.current().nextBoolean()) {
                                weaponCards.add(new WeaponCard[]{WeaponCard.JAVELIN_OF_MIGHT, WeaponCard.IMPERIAL_CROSSBOW});
                            } else {
                                weaponCards.add(new WeaponCard[]{WeaponCard.LONGBOW_OF_EXCELLENCE, WeaponCard.IMPERIAL_CROSSBOW});
                            }
                        }
                        case NEUTRAL -> {
                            switch (ThreadLocalRandom.current().nextInt(0, 4)) {
                                case 0 -> weaponCards.add(new WeaponCard[]{WeaponCard.BOW_OF_POISONING, WeaponCard.JAVELIN_OF_MIGHT_GOLD});
                                case 1 -> weaponCards.add(new WeaponCard[]{WeaponCard.BOW_OF_SWIFTNESS, WeaponCard.SHADOW_DAGGER});
                                case 2 -> weaponCards.add(new WeaponCard[]{WeaponCard.JAVELIN_OF_MIGHT, WeaponCard.IMPERIAL_CROSSBOW});
                                case 3 -> weaponCards.add(new WeaponCard[]{WeaponCard.LONGBOW_OF_EXCELLENCE, WeaponCard.IMPERIAL_CROSSBOW});
                            }
                        }
                    }
                }
            }
        }
        List<List<PowerCard>> powerCards = new ArrayList<>(3);
        for (int i = 0; i < 3; ++i) {
            switch (pawnCards.get(i).actionType) {
                case MELEE ->
                        powerCards.add(List.of(PowerCard.DOUBLER, PowerCard.MARTIAL_MASTER, PowerCard.MARTIAL_MASTER_GOLD, PowerCard.CRAFT_MASTER));
                case MAGIC ->
                        powerCards.add(List.of(PowerCard.ARCANE_MASTER_GOLD, PowerCard.LUCKY_CLOVER, PowerCard.MIRROR_SKIN, PowerCard.WARRIORS_BLESSING));
                case RANGED ->
                        powerCards.add(List.of(PowerCard.WARRIORS_BLESSING, PowerCard.RANGED_MASTER_GOLD, PowerCard.IMPRENETRATION, PowerCard.DOUBLER));
            }
        }
        List<List<ActionCard>> actionCards = new ArrayList<>(3);
        List<List<AbilityCard>> abilityCards = new ArrayList<>(3);
        for (int i = 0; i < 3; ++i) {
            var actionType = pawnCards.get(i).actionType;
            var alignment = talismanCards.get(i).alignment;
            List<ActionCard> aCards = new ArrayList<>(8);
            List<AbilityCard> abCards = new ArrayList<>(8);
            for (int j = 0; j < 4; ++j) {
                List<ActionCard> cardOpt = new ArrayList<>();
                while (cardOpt.isEmpty()) {
                    cardOpt = Arrays.stream(ActionCard.values())
                            .filter(c -> c.getStats().getActionType() == actionType)
                            .filter(c -> alignment == Alignment.NEUTRAL || c.getStats().getAlignment() == alignment)
                            .filter(c -> ThreadLocalRandom.current().nextInt(3) < 2 ? c.getStats().getLevel() > 2 : c.getStats().getLevel() < 4)
                            .toList();
                }
                aCards.add(cardOpt.get(ThreadLocalRandom.current().nextInt(cardOpt.size())));
            }
            for (int j = 0; j < 4; ++j) {
                List<ActionCard> cardOpt = new ArrayList<>();
                while (cardOpt.isEmpty()) {
                    cardOpt = Arrays.stream(ActionCard.values())
                            .filter(c -> c.getStats().getActionType() == actionType)
                            .filter(c -> ThreadLocalRandom.current().nextInt(3) < 2 ? c.getStats().getLevel() > 2 : c.getStats().getLevel() < 4)
                            .toList();
                    aCards.add(cardOpt.get(ThreadLocalRandom.current().nextInt(cardOpt.size())));
                }
                actionCards.add(aCards);
            }
            for (int j = 0; j < 8; ++j) {
                List<AbilityCard> cardOpt = new ArrayList<>();
                while (cardOpt.isEmpty()) {
                    cardOpt = Arrays.stream(AbilityCard.values())
                            .filter(c -> ThreadLocalRandom.current().nextInt(3) < 2 ? c.getStats().getLevel() < 3 : c.getStats().getLevel() > 2)
                            .toList();
                }
                abCards.add(cardOpt.get(ThreadLocalRandom.current().nextInt(cardOpt.size())));
            }
            abilityCards.add(abCards);
        }
        PawnLoadOut[] loadouts = new PawnLoadOut[3];

        for (int i = 0; i < 3; ++i) {
            loadouts[i] = new PawnLoadOut(
                    pawnCards.get(i),
                    talismanCards.get(i),
                    weaponCards.get(i)[0],
                    weaponCards.get(i)[1],
                    actionCards.get(i).stream().map(c -> (ActionCard) c).toList(),
                    abilityCards.get(i),
                    powerCards.get(i)
            );
        }

        return new PawnSet(
                -1,
                "botSet",
                loadouts
        );
    }

    private static int[] getLoadoutLevels(int levelCap) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int[] levelCaps = new int[3];
        int maxLevel = levelCap;
        levelCaps[0] = (int) Math.round((maxLevel / 3.0) * rnd.nextDouble(0.9, 1.1));
        maxLevel -= levelCaps[0];
        levelCaps[1] = (int) Math.round((maxLevel / 2.0) * rnd.nextDouble(0.9, 1.1));
        levelCaps[2] = maxLevel - levelCaps[1];
        return levelCaps;
    }

    private static Alignment skewAlign(double skew, Alignment alignment) {
        return ThreadLocalRandom.current().nextDouble(0, 1) < skew
                ? alignment
                : (alignment == Alignment.CHAOS ? Alignment.ORDER : Alignment.CHAOS);
    }

    public static List<Integer> getLevelSet(float sumNeeded, int n) throws IllegalStateException {
        Random rnd = new Random();

        for (int i = 0; i < 1000; ++i) {
            List<Integer> lvlList = new ArrayList<>(n);
            int sum = 0;
            for (int j = 0; j < n; ++j) {
                int nextInt = rnd.nextInt(1, 5);
                lvlList.add(nextInt);
                sum += nextInt;
                // if (sum > sumNeeded) { break; }
                if (Math.abs(sumNeeded - sum) > sumNeeded * 0.1) {
                    if (lvlList.size() >= n) {
                        return lvlList;
                    }
                }
            }
        }
        throw new IllegalStateException("Failed to generate a set this shouldn't happen");
    }
}
