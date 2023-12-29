package de.wightman.minecraft.deathstats.record;

import java.util.Objects;

public record SessionRecord(int id, long start, long end, String name) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionRecord that = (SessionRecord) o;
        return id == that.id && start == that.start && end == that.end && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, start, end, name);
    }

    @Override
    public String toString() {
        return "SessionRecord{" +
                "id=" + id +
                ", start=" + start +
                ", end=" + end +
                ", name='" + name + '\'' +
                '}';
    }
}
