package dev.moongarden.structure;

import me.basiqueevangelist.enhancedreflection.api.EClass;
import me.basiqueevangelist.enhancedreflection.api.EExecutable;

import java.util.List;

public abstract class AbstractExecutableLuaElement implements LuaElement {
    protected final List<ParameterLuaElement> parameters;
    protected final EClass<?> parent;

    public AbstractExecutableLuaElement(EExecutable executable) {
        parameters = executable.parameters().stream().map(ParameterLuaElement::new).toList();
        parent = executable.declaringClass();
    }
}
