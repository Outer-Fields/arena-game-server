package io.mindspice.okragameserver.game.enums;

public class Animation {
    private volatile String value;
    private volatile String sprite;
    private volatile String impact;
    private volatile  boolean isFinal = false;

    public static Animation build(AnimType type, AnimTime time, AnimSpeed speed) {
        return new Animation(type.name() + ":" + time.name() + ":" + speed.name());
    }

    public Animation setSprite(Sprite sprite) {
        this.sprite = sprite.name();
        return this;
    }

    public Animation setImpactAudio(ImpactAudio audio) {
        this.impact = audio.name();
        return this;
    }

    private Animation(String value) {
        this.value = value;
    }

    // FIXME this should be made into a builder builder
    public String getValue() {
        if (!isFinal) {
            if (sprite != null) { value += ":" + sprite; }
            if (impact != null) { value += ":" + impact; }
            isFinal = true;
        }
        return value;
    }

}