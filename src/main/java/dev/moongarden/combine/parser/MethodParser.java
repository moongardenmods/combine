package dev.moongarden.combine.parser;

import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class MethodParser extends MethodVisitor implements NamedWritable {
    // TODO: Create annotation reader
    private final List<?> annotations = new ArrayList<>();
    private final List<String> parameterNames = new ArrayList<>();

    private final Type parent;
    private final int access;
    private final String name;
    private final Type[] parameterTypes;
    private final Type returnType;
    private final String signature;
    private final String[] exceptions;
    private final Function<ClassParser.MemberType, Consumer<MethodParser>> finish;


    public MethodParser(Type parent, int access, String name, String descriptor, String signature, String[] exceptions, Function<ClassParser.MemberType, Consumer<MethodParser>> finish, Set<Type> imports) {
        super(Opcodes.ASM9);
        this.parent = parent;
        this.access = access;
        this.name = name;
        Type desc = Type.getType(descriptor);
        this.parameterTypes = desc.getArgumentTypes();
        imports.addAll(Arrays.asList(this.parameterTypes));
        this.returnType = desc.getReturnType();
        imports.add(this.returnType);
        this.signature = signature;
        this.exceptions = exceptions;
        this.finish = finish;
    }

    @Override
    public void visitParameter(String name, int access) {
        parameterNames.add(name);
        super.visitParameter(name, access);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        super.visitAnnotableParameterCount(parameterCount, visible);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        return super.visitParameterAnnotation(parameter, descriptor, visible);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        super.visitAttribute(attribute);
    }

    @Override
    public void visitEnd() {
        if (name.equals("<init>")) {
            finish.apply(ClassParser.MemberType.CTOR).accept(this);
        } else {
            if ((access & Opcodes.ACC_STATIC) == 0) {
                finish.apply(ClassParser.MemberType.INSTANCE).accept(this);
            } else {
                finish.apply(ClassParser.MemberType.STATIC).accept(this);
            }
        }
        super.visitEnd();
    }

    @Override
    public void write(ClassParser parser, StringBuilder builder) {
        if (name.equals("<init>")) {
            builder.append("--- @overload fun(");
            for (int i = 0; i < parameterTypes.length; i++) {
                builder.append(parameterNames.size() > i ? parameterNames.get(i) : "arg"+i).append(": ").append(ClassParser.instanceTypeDoc(parameterTypes[i]));
                if (i != parameterTypes.length-1) builder.append(", ");
            }
            builder.append("): ").append(ClassParser.instanceTypeDoc(parent)).append(" ").append(ClassParser.accessName(access)).append("\n");
        } else {
            builder.append("--- @").append(ClassParser.accessName(access)).append("\n");
            for (int i = 0; i < parameterTypes.length; i++) {
                builder.append("--- @param ").append(parameterNames.size() > i ? parameterNames.get(i) : "arg"+i).append(" ")
                        .append(ClassParser.instanceTypeDoc(parameterTypes[i]));
                // TODO: Parameter Details
                builder.append("\n");
            }
            builder.append("--- @return ").append(ClassParser.instanceTypeDoc(returnType));
            // TODO: Return Details
            builder.append("\n");
            if (name.contains("$")) {
                builder.append(ClassParser.instanceTypeDoc(parent));
                if ((access & Opcodes.ACC_STATIC) != 0) {
                    builder.append("Class");
                }
                builder.append("[\"").append(name).append("\"] = function");
            } else {
                builder.append("function ").append(ClassParser.instanceTypeDoc(parent));
                if ((access & Opcodes.ACC_STATIC) != 0) {
                    builder.append("Class.");
                } else {
                    builder.append(":");
                }
                builder.append(ClassParser.KEYWORDS.contains(name) ? "m_" + name : name);
            }
            builder.append("(");
            for (int i = 0; i < parameterTypes.length; i++) {
                String pName = parameterNames.size() > i ? parameterNames.get(i) : "arg"+i;
                builder.append(pName);
                if (i != parameterTypes.length-1) builder.append(", ");
            }
            builder.append(") end\n\n");
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isStatic() {
        return (access & Opcodes.ACC_STATIC) != 0;
    }

}
