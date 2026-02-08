package dev.moongarden.structure;

import dev.moongarden.Combine;
import me.basiqueevangelist.enhancedreflection.api.EClass;
import me.basiqueevangelist.enhancedreflection.api.EMethod;
import me.basiqueevangelist.enhancedreflection.api.typeuse.ETypeUse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MethodLuaElement extends AbstractExecutableLuaElement {
    private static final Set<String> keywords = HashSet.newHashSet(14);

    private final ETypeUse returnType;
    private final String name;
    private final boolean isStatic;

    public MethodLuaElement(EMethod method) {
        super(method);
        Combine.makeVisible(method.rawReturnType());
        returnType = method.returnTypeUse();
        name = keywords.contains(method.name()) ? "m_" + method.name() : method.name();
        isStatic = method.isStatic();
    }

    @Override
    public boolean addFirst() {
        return false;
    }

    @Override
    public void build(StringBuilder builder) {
        builder.append("--- @").append(access.type()).append("\n");
        for (ParameterLuaElement parameter : parameters) {
            parameter.inMethodDoc(builder);
        }
        EClass<?> returnClass = returnType.lowerBound().type();
        ParameterLuaElement.ValueDetails type = ParameterLuaElement.valueName(returnClass);
        builder.append("--- @return ").append(type.type());
        if (type.details() != null) {
            builder.append(" ").append(type.type()).append(" ").append(type.details());
        }
        builder.append("\n");
        if (name.contains("$")) {
            builder.append(parent.simpleName());
            if (isStatic) {
                builder.append("Class");
            }
            builder.append("[\"").append(name).append("\"] = function");
        } else {
            builder.append("function ").append(parent.simpleName());
            if (isStatic) {
                builder.append("Class.");
            } else {
                builder.append(":");
            }
            builder.append(name);
        }
        builder.append("(");
        if (!parameters.isEmpty()) {
            ParameterLuaElement last = parameters.getLast();
            for (ParameterLuaElement parameter : parameters) {
                parameter.inMethodDeclaration(builder);
                if (!last.equals(parameter)) builder.append(", ");
            }
        }
        builder.append(") end\n\n");
    }

    static {
        keywords.add("and"); keywords.add("do"); keywords.add("elseif"); keywords.add("end");
        keywords.add("false"); keywords.add("function"); keywords.add("in"); keywords.add("local");
        keywords.add("nil"); keywords.add("not"); keywords.add("or"); keywords.add("repeat");
        keywords.add("then"); keywords.add("until");
    }
}
