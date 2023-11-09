package io.mindspice.okragameserver.game.bot.state;

import io.mindspice.okragameserver.game.enums.EffectType;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.enums.PlayerAction;

import java.util.ArrayList;
import java.util.List;


public class TreeFocusState {
    public PawnIndex focusPawn;
    public EffectType effect;
    public PlayerAction playerAction;
    public List<String> decisions = new ArrayList<>();
    public String action;
}
