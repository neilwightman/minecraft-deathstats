package de.wightman.minecraft.deathstats;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

import javax.annotation.Nullable;
import java.util.Objects;

public class DeathRecord  {
    private static final Gson gson = new Gson();

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

    public DeathRecord(String deathMessage, String killedByKey, String killedByStr, int argb) {
        Objects.requireNonNull(deathMessage, "deathMessage cannot be null");

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
}
