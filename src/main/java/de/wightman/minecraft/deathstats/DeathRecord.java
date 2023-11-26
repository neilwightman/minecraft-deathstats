package de.wightman.minecraft.deathstats;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

import javax.annotation.Nullable;
import java.util.Objects;

public class DeathRecord  {
    private static final Gson gson = new Gson();

    @Expose
    public String world;

    @Expose
    public String dimension;

    @Expose
    public String deathMessage;

    @Expose
    @Nullable
    public String killedByKey;

    @Expose
    @Nullable
    public String killedByStr;

    @Expose
    public int argb;

    // TODO add timestamp

    public DeathRecord(String world, String dimension, String deathMessage, @Nullable String killedByKey, @Nullable String killedByStr, int argb) {
        Objects.requireNonNull(deathMessage, "deathMessage cannot be null");

        this.world = world;
        this.dimension = dimension;
        this.deathMessage = deathMessage;
        this.killedByKey = killedByKey;
        this.killedByStr = killedByStr;
        this.argb = argb;
    }

    public static String toJsonString(DeathRecord dr) {
        return gson.toJson(dr);
    }

    public static DeathRecord fromString(String str) {
        return gson.fromJson(str, DeathRecord.class);
    }

    public String toJsonString () {
        return toJsonString(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeathRecord that = (DeathRecord) o;
        return argb == that.argb && Objects.equals(deathMessage, that.deathMessage) && Objects.equals(killedByKey, that.killedByKey) && Objects.equals(killedByStr, that.killedByStr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deathMessage, killedByKey, killedByStr, argb);
    }
}
