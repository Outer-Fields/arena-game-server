package io.mindspice.okragameserver.util;

import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.okragameserver.game.cards.*;
import io.mindspice.okragameserver.game.enums.ActionType;
import io.mindspice.okragameserver.game.enums.Alignment;
import io.mindspice.okragameserver.game.enums.CardDomain;
import org.apache.commons.codec.digest.MurmurHash3;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


public class CardUtil {
    public static final List<String> actionCardNames;
    public static final List<String> abilityCardNames;
    public static final List<String> weaponCardNames;
    public static final List<String> powerCardsNames;
    public static final List<String> talismanCardNames;
    public static final List<String> pawnCardsNames;
    public static final List<ActionCard> actionCardList;
    public static final List<AbilityCard> abilityCardList;
    public static final List<WeaponCard> weaponCardList;
    public static final List<PowerCard> powerCardsList;
    public static final List<TalismanCard> talismanCardList;
    public static final List<PawnCard> pawnCardsList;
    public static final Map<String, String> actionCardNameTable;
    public static final Map<String, String> abilityCardNameTable;
    public static final Map<String, String> weaponCardNameTable;
    public static final Map<String, String> powerCardNameTable;
    public static final Map<String, String> talismanCardNameTable;
    public static final Map<String, String> pawnCardNameTable;


    static {
        actionCardNames = Arrays.stream(ActionCard.values()).map(Enum::name).toList();
        abilityCardNames = Arrays.stream(AbilityCard.values()).map(Enum::name).toList();
        weaponCardNames = Arrays.stream(WeaponCard.values()).map(Enum::name).toList();
        talismanCardNames = Arrays.stream(TalismanCard.values()).map(Enum::name).toList();
        powerCardsNames = Arrays.stream(PowerCard.values()).map(Enum::name).toList();
        pawnCardsNames = Arrays.stream(PawnCard.values()).map(Enum::name).toList();
        actionCardList = List.of(ActionCard.values());
        abilityCardList = List.of(AbilityCard.values());
        weaponCardList = List.of(WeaponCard.values());
        powerCardsList = List.of(PowerCard.values());
        talismanCardList = List.of(TalismanCard.values());
        pawnCardsList = List.of(PawnCard.values());

        actionCardNameTable = Arrays.stream(ActionCard.values())
                .collect(Collectors.toUnmodifiableMap(ActionCard::getUid, ActionCard::name));

        abilityCardNameTable = Arrays.stream(AbilityCard.values())
                .collect(Collectors.toUnmodifiableMap(AbilityCard::getUid, AbilityCard::name));

        weaponCardNameTable = Arrays.stream(WeaponCard.values())
                .collect(Collectors.toUnmodifiableMap(WeaponCard::getUid, WeaponCard::name));

        powerCardNameTable = Arrays.stream(PowerCard.values())
                .collect(Collectors.toUnmodifiableMap(PowerCard::getUid, PowerCard::name));

        talismanCardNameTable = Arrays.stream(TalismanCard.values())
                .collect(Collectors.toUnmodifiableMap(TalismanCard::getUid, TalismanCard::name));

        pawnCardNameTable = Arrays.stream(PawnCard.values())
                .collect(Collectors.toUnmodifiableMap(PawnCard::getUid, PawnCard::name));


    }

    private CardUtil() { }

    // This converts the uid back to card names, the final -<Char> in the uid is used only in card distribution and displaying in
    // the client. As ownedCards like -F for foil are normal/gold ownedCards but of foil type. These ownedCards don't have unique UIDs
    // but need to be considered so in the database and client. The internal UID could be returned for valid ownedCards,
    // but program was already structured to use the names to validate and uses a generic around enum to validate, so we
    // need the names for comparisons. Mapped ownedCards are returned to the player to less the lookup overhead on the client
    // as they are pre-mapped, then valid ownedCards are used internally to validate the PawnSets.

    // TODO this may later be used for foils currently there are no foils so it is kind of point less, but the mapping is used
    //  on the client, this is a small left over mess from development indecisiveness on how to represent things
    public static Pair<Map<CardDomain, List<String>>, Map<CardDomain, List<String>>> playerMappedCards(
            List<String> uidList) {
        Map<CardDomain, List<String>> playerCards = new HashMap<>(6);
        Map<CardDomain, List<String>> validCards = new HashMap<>(6);
        for (var domain : CardDomain.values()) {
            playerCards.put(domain, new ArrayList<>());
            validCards.put(domain, new ArrayList<>());
        }

        for (var uid : uidList) {
            String domainCode = uid.split("-")[0];

            switch (domainCode) {
                case ActionCard.prefix -> {
                    playerCards.get(CardDomain.ACTION).add(actionCardNameTable.get(uid));
                    validCards.get(CardDomain.ACTION).add(actionCardNameTable.get(uid));
                }
                case AbilityCard.prefix -> {
                    playerCards.get(CardDomain.ABILITY).add(abilityCardNameTable.get(uid));
                    validCards.get(CardDomain.ABILITY).add(abilityCardNameTable.get(uid));
                }
                case PowerCard.prefix -> {
                    playerCards.get(CardDomain.POWER).add(powerCardNameTable.get(uid));
                    validCards.get(CardDomain.POWER).add(powerCardNameTable.get(uid));
                }
                case WeaponCard.prefix -> {
                    playerCards.get(CardDomain.WEAPON).add(weaponCardNameTable.get(uid));
                    validCards.get(CardDomain.WEAPON).add(weaponCardNameTable.get(uid));
                }
                case TalismanCard.prefix -> {
                    playerCards.get(CardDomain.TALISMAN).add(talismanCardNameTable.get(uid));
                    validCards.get(CardDomain.TALISMAN).add(talismanCardNameTable.get(uid));

                }
                case PawnCard.prefix -> {
                    playerCards.get(CardDomain.PAWN).add(pawnCardNameTable.get(uid));
                    validCards.get(CardDomain.PAWN).add(pawnCardNameTable.get(uid));
                }
            }
        }
        playerCards.replaceAll((key, value) -> Collections.unmodifiableList(value));
        validCards.replaceAll((key, value) -> Collections.unmodifiableList(value));
        return new Pair<>(Collections.unmodifiableMap(playerCards), Collections.unmodifiableMap(validCards));
    }

    public static long getHash(String input) {
        // Generate a 128-bit hash using MurmurHash3
        long[] hash = MurmurHash3.hash128x64(input.getBytes());

        // Combine the first two integers into a 64-bit long
        long longHash = ((long) hash[0] << 32) | (hash[1] & 0xFFFFFFFFL);
        //long[] hash = MurmurHash3.hash128x64(input.getBytes(StandardCharsets.UTF_8));
        // Truncate to 48 bits by bitwise AND with a mask
        //String hexHash = String.format("%012x", longHash & 0xFFFFFFFFFFFFL);
        return longHash & 0xFFFFFFFFFFFFL;
    }

    public static PawnCard getRandomPawn() {
        return pawnCardsList.get(ThreadLocalRandom.current().nextInt(pawnCardsList.size()));
    }

    public static WeaponCard getRandomWeapon(ActionType actionType, int level) {
        var weaponList = weaponCardList.stream()
                .filter(c -> c.getStats().getActionType() == actionType)
                .filter(c -> c.getLevel() == level)
                .toList();
        if (weaponList.isEmpty()) { return null; }
        return weaponList.get(ThreadLocalRandom.current().nextInt(weaponList.size()));
    }

    public static WeaponCard getRandomWeapon(ActionType actionType) {
        var weaponList = weaponCardList.stream()
                .filter(c -> c.getStats().getActionType() == actionType)
                .toList();
        if (weaponList.isEmpty()) { return null; }
        return weaponList.get(ThreadLocalRandom.current().nextInt(weaponList.size()));
    }

    // all the .isEmpty methods should never trigger, but are there for null safety

    public static WeaponCard getRandomWeapon(ActionType actionType, Alignment alignment) {
        var weaponList = weaponCardList.stream()
                .filter(c -> c.getStats().getActionType() == actionType)
                .filter(c -> c.getStats().getAlignment() == alignment)
                .toList();
        if (weaponList.isEmpty()) {
            weaponList = weaponCardList.stream()
                    .filter(c -> c.getStats().getActionType() == actionType)
                    .toList();
        }
        return weaponList.get(ThreadLocalRandom.current().nextInt(weaponList.size()));
    }

    public static TalismanCard getRandomTalisman() {
        return talismanCardList.get(ThreadLocalRandom.current().nextInt(talismanCardList.size()));
    }

    public static PowerCard getRandomPowerCard(int level) {
        var powerList = powerCardsList.stream().filter(c -> c.getLevel() == level).toList();
        if (powerList.isEmpty()) {
            powerList = powerCardsList;
        }
        return powerList.get(ThreadLocalRandom.current().nextInt(powerList.size()));
    }

    public static ActionCard getRandomActionCard(ActionType actionType, Alignment alignment, int level) {
        var actionList = actionCardList.stream()
                .filter(c -> c.getLevel() == level)
                .filter(c -> c.getStats().getActionType() == actionType)
                .filter(c -> c.getStats().getAlignment() == alignment)
                .toList();

        if (actionList.isEmpty()) {
            actionList = actionCardList.stream().filter(c -> c.getStats().getActionType() == actionType).toList();
        }
        return actionList.get(ThreadLocalRandom.current().nextInt(actionList.size()));
    }

    public static ActionCard getRandomActionCard(ActionType actionType, int level) {
        var actionList = actionCardList.stream()
                .filter(c -> c.getLevel() == level)
                .filter(c -> c.getStats().getActionType() == actionType)
                .toList();
        if (actionList.isEmpty()) {
            actionList = actionCardList.stream().filter(c -> c.getStats().getActionType() == actionType).toList();
        }
        return actionList.get(ThreadLocalRandom.current().nextInt(actionList.size()));
    }

    public static AbilityCard getRandomAbilityCard(Alignment alignment, int level) {
        var abilityList = abilityCardList.stream()
                .filter(c -> c.getLevel() == level)
                .filter(c -> c.getStats().getAlignment() == alignment)
                .toList();

        if (abilityList.isEmpty()) {
            abilityList = abilityCardList;
        }
        return abilityList.get(ThreadLocalRandom.current().nextInt(abilityList.size()));
    }

    public static AbilityCard getRandomAbilityCard(int level) {
        var abilityList = abilityCardList.stream()
                .filter(c -> c.getLevel() == level)
                .toList();

        if (abilityList.isEmpty()) {
            abilityList = abilityCardList;
        }
        return abilityList.get(ThreadLocalRandom.current().nextInt(abilityList.size()));
    }
}
