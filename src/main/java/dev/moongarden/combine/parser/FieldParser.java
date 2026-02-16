package dev.moongarden.combine.parser;

import dev.moongarden.combine.extension.api.FieldParserExtension;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class FieldParser extends FieldVisitor implements NamedWritable {
    private final List<?> annotations = new ArrayList<>();
    private final Type parent;
    private final int access;
    private final String name;
    private final Type descriptor;
    private final String signature;
    private final Object value;
    private final Function<ClassParser.MemberType, Consumer<FieldParser>> finish;
    private final List<FieldParserExtension> extensionVisitors;

    public FieldParser(Type parent, int access, String name, String descriptor, String signature, Object value, Function<ClassParser.MemberType, Consumer<FieldParser>> finish, Set<Type> imports, List<FieldParserExtension> extensionVisitors) {
        super(Opcodes.ASM9);
        this.parent = parent;
        this.access = access;
        this.name = name;
        this.descriptor = Type.getType(descriptor);
        imports.add(this.descriptor);
        this.signature = signature;
        this.value = value;
        this.finish = finish;
        this.extensionVisitors = extensionVisitors;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return new AnnotationParser(
                extensionVisitors.stream()
                        .map((e) -> e.visitAnnotation(descriptor, visible))
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return new AnnotationParser(
                extensionVisitors.stream()
                        .map((e) -> e.visitTypeAnnotation(typeRef, typePath, descriptor, visible))
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        extensionVisitors.forEach((e) -> e.visitAttribute(attribute));
    }

    @Override
    public void visitEnd() {
        extensionVisitors.forEach(FieldParserExtension::visitEnd);
        if (extensionVisitors.stream().map(FieldParserExtension::shouldWriteField).reduce(true, Boolean::logicalAnd)) {
            if ((access & Opcodes.ACC_STATIC) == 0) {
                finish.apply(ClassParser.MemberType.INSTANCE).accept(this);
            } else {
                finish.apply(ClassParser.MemberType.STATIC).accept(this);
            }
        }
    }

    @Override
    public void write(ClassParser parser, StringBuilder builder) {
        builder.append("--- @field ").append(ClassParser.accessName(access)).append(" ");
        String identity = parser.hasMemberNamed(this) || ClassParser.KEYWORDS.contains(name) ? "f_" + name : name;
        for (FieldParserExtension extensionVisitor : extensionVisitors) {
            identity = extensionVisitor.modifyFieldName(identity);
        }
        builder.append(identity).append(" ").append(ClassParser.instanceTypeDoc(descriptor)).append("\n");
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
