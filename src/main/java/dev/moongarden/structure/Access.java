package dev.moongarden.structure;

import me.basiqueevangelist.enhancedreflection.api.ModifierHolder;

import java.util.Locale;

public enum Access {
    PRIVATE,
    PACKAGE,
    PROTECTED,
    PUBLIC;

    private final String type;
    Access() {
        this.type = name().toLowerCase(Locale.ROOT);
    }

    public String type() {
        return type;
    }

    public static Access getAccess(ModifierHolder modifier) {
        if (modifier.isPrivate()) return PRIVATE;
        if (modifier.isProtected()) return PROTECTED;
        if (modifier.isPublic()) return PUBLIC;
        return PACKAGE;
    }
}
