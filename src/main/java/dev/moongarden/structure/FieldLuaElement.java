package dev.moongarden.structure;

import dev.moongarden.Combine;
import me.basiqueevangelist.enhancedreflection.api.EField;
import me.basiqueevangelist.enhancedreflection.api.EType;
import me.basiqueevangelist.enhancedreflection.api.typeuse.ETypeUse;

public class FieldLuaElement implements LuaElement {
    private final EType type;
    private final Access access;
    private final String name;

    public FieldLuaElement(EField field) {
        Combine.makeVisible(field.rawFieldType());
        type = field.fieldType();
        access = Access.getAccess(field);
        if (field.name().contains("$")) {
            name = "[\"" + field.name() + "\"]";
        } else {
            name = field.name();
        }

        field.fieldType().upperBound().typeVariableValues().forEach((t) -> Combine.makeVisible(t.upperBound()));
    }

    @Override
    public boolean addFirst() {
        return false;
    }

    @Override
    public void build(StringBuilder builder) {
        ParameterLuaElement.ValueDetails value = ParameterLuaElement.valueName(type.upperBound());
        builder.append("--- @field ").append(access.type()).append(" ").append(name).append(" ").append(value.type());
        if (value.details() != null) {
            builder.append(" ").append(value.details());
        }
        builder.append("\n");
    }
}
