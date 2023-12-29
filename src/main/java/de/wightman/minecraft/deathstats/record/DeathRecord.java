package de.wightman.minecraft.deathstats.record;

import javax.annotation.Nullable;
import java.util.Objects;

public record DeathRecord(int id,
                          String world,
                          String dimension,
                          String deathMessage,
                          @Nullable String killedByKey,
                          @Nullable String killedByStr,
                          int argb,
                          long ts)  {

    /**
     * Used for records which have yet to be written to the database.
     */
    public static int NOT_SET = -1;

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

    @Override
    public String toString() {
        return "DeathRecord{" +
                "id=" + id +
                ", world='" + world + '\'' +
                ", dimension='" + dimension + '\'' +
                ", deathMessage='" + deathMessage + '\'' +
                ", killedByKey='" + killedByKey + '\'' +
                ", killedByStr='" + killedByStr + '\'' +
                ", argb=" + argb +
                ", ts=" + ts +
                '}';
    }
}
