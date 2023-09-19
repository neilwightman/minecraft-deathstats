package de.wightman.minecraft.deathstats;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

import java.util.Objects;

public class DeathRecord  {
    private static final Gson gson = new Gson();

    @Expose
    public String deathMessage;
    @Expose
    public String killedByKey;
    @Expose
    public String killedByStr;

    public DeathRecord(String deathMessage, String killedByKey, String killedByStr) {
        Objects.requireNonNull(deathMessage, "deathMessage cannot be null");

        this.deathMessage = deathMessage;
        this.killedByKey = killedByKey;
        this.killedByStr = killedByStr;
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
}
