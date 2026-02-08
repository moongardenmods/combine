package dev.moongarden.structure;

import dev.moongarden.Combine;
import me.basiqueevangelist.enhancedreflection.api.EClass;
import me.basiqueevangelist.enhancedreflection.api.EField;
import me.basiqueevangelist.enhancedreflection.api.typeuse.ETypeUse;

import java.util.List;

public class FieldLuaElement implements LuaElement {
    private final ETypeUse returnType;
    private final String name;

    public FieldLuaElement(EField field) {
        Combine.makeVisible(field.rawFieldType());
        returnType = field.fieldTypeUse();
        name = field.name();
    }

    @Override
    public boolean addFirst() {
        return false;
    }

    @Override
    public void build(StringBuilder builder) {
        builder.append("--- @field ").append(name).append(" ").append(ClassLuaElement.getName(returnType.lowerBound().type())).append("\n");
    }
}
