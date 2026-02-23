package dev.moongarden.combine.parser;

import dev.moongarden.combine.extension.api.MethodParserExtension;
import org.objectweb.asm.*;

import java.util.*;
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
    private final List<MethodParserExtension> extensionVisitors;


    public MethodParser(Type parent, int access, String name, String descriptor, String signature, String[] exceptions, Function<ClassParser.MemberType, Consumer<MethodParser>> finish, Set<Type> imports, List<MethodParserExtension> extensionVisitor) {
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
        this.extensionVisitors = extensionVisitor;
    }

    @Override
    public void visitParameter(String name, int access) {
        extensionVisitors.forEach((e) -> e.visitParameter(name, access));
        parameterNames.add(name);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return new AnnotationParser(extensionVisitors.stream()
                .map((e) -> e.visitAnnotation(descriptor, visible))
                .filter(Objects::nonNull)
                .toList()
        );
    }

    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        extensionVisitors.forEach((e) -> e.visitAnnotableParameterCount(parameterCount, visible));
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        return new AnnotationParser(
                extensionVisitors.stream()
                        .map((e) -> e.visitParameterAnnotation(parameter, descriptor, visible))
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

    @Override
    public void visitAttribute(Attribute attribute) {

    }

    @Override
    public void visitEnd() {
        extensionVisitors.forEach(MethodParserExtension::visitEnd);
        if (extensionVisitors.stream().map(MethodParserExtension::shouldWriteMethod).reduce(true, Boolean::logicalAnd)) {
            if (name.equals("<init>")) {
                finish.apply(ClassParser.MemberType.CTOR).accept(this);
            } else {
                if ((access & Opcodes.ACC_STATIC) == 0) {
                    finish.apply(ClassParser.MemberType.INSTANCE).accept(this);
                } else {
                    finish.apply(ClassParser.MemberType.STATIC).accept(this);
                }
            }
        }
    }

    @Override
    public void write(ClassParser parser, StringBuilder builder) {
        if (name.equals("<init>")) {
            builder.append("--- @overload fun(");
            final int size = parameterNames.size()-1;
            for (int i = 0; i < parameterTypes.length; i++) {
                builder.append(size > i ? parameterNames.get(i) : "arg"+i).append(": ").append(ClassParser.instanceTypeDoc(parameterTypes[i]));
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
                builder.append(ClassParser.shortTypeDoc(parent));
                if ((access & Opcodes.ACC_STATIC) != 0) {
                    builder.append("Class");
                }
                builder.append("[\"").append(name).append("\"] = function");
            } else {
                builder.append("function ").append(ClassParser.shortTypeDoc(parent));
                if ((access & Opcodes.ACC_STATIC) != 0) {
                    builder.append("Class.");
                } else {
                    builder.append(":");
                }
                String identity = ClassParser.KEYWORDS.contains(name) ? "m_" + name : name;
                for (MethodParserExtension extensionVisitor : extensionVisitors) {
                    identity = extensionVisitor.modifyMethodName(identity);
                }
                builder.append(identity);
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
