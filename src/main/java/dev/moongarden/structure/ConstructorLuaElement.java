package dev.moongarden.structure;

import dev.moongarden.Combine;
import me.basiqueevangelist.enhancedreflection.api.EClass;
import me.basiqueevangelist.enhancedreflection.api.EConstructor;

import java.util.List;

public class ConstructorLuaElement extends AbstractExecutableLuaElement {
    private final EClass<?> parent;
    public ConstructorLuaElement(EConstructor<?> constructor) {
        super(constructor);
        this.parent = constructor.declaringClass();
    }

    @Override
    public boolean addFirst() {
        return true;
    }

    @Override
    public void build(StringBuilder builder) {
        if (!Combine.IGNORE_ACCESS && access != Access.PUBLIC) return;
        builder.append("--- @overload fun(");
        if (!parameters.isEmpty()) {
            ParameterLuaElement last = parameters.getLast();
            for (ParameterLuaElement parameter : parameters) {
                parameter.inConstructor(builder);
                if (!last.equals(parameter)) builder.append(", ");
            }
        }
        builder.append("): ").append(ClassLuaElement.getName(parent)).append(" ").append(access.type()).append("\n");
    }
}
