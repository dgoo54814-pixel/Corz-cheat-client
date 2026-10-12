package dev.corzstyle.client;

import java.util.EnumSet;

public final class Feature {
    public enum Group { COMBAT, DONUTSMP, VISUALS, MOVEMENT, MISC }
    public enum Badge { NONE, POPULAR, NEW, ADVANCED }

    public final String id, name, description;
    public final EnumSet<Group> groups;
    public final Badge badge;
    public final boolean implemented;
    public boolean enabled;

    public Feature(String id, String name, String description, Badge badge, boolean implemented, Group... groups) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.badge = badge;
        this.implemented = implemented;
        this.groups = EnumSet.noneOf(Group.class);
        for (Group group : groups) this.groups.add(group);
    }
}
