package io.mindspice.okragameserver.game.enums;

public enum InvalidMsg {
    COST("Cost Too High"),
    NULL("Card Not Held"),
    HP_CONSTRAINT("HP Constraint Not Met"),
    LVL_CONSTRAINT("Level Constraint Not Met"),
    TYPE_CONSTRAINT("Swap Must Be same Action Type"),
    CONSTRAINT("No Suitable Cards"),
    P_EMPTY("Player Slot Empty"),
    E_EMPTY("Enemy Slot Empty"),
    ;
    public final String msg;

    InvalidMsg(String msg) { this.msg = msg; }
}
