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
    private final List<FieldParserExtension> extensionVisitor;

    public FieldParser(Type parent, int access, String name, String descriptor, String signature, Object value, Function<ClassParser.MemberType, Consumer<FieldParser>> finish, Set<Type> imports, List<FieldParserExtension> extensionVisitor) {
        super(Opcodes.ASM9);
        this.parent = parent;
        this.access = access;
        this.name = name;
        this.descriptor = Type.getType(descriptor);
        imports.add(this.descriptor);
        this.signature = signature;
        this.value = value;
        this.finish = finish;
        this.extensionVisitor = extensionVisitor;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return new AnnotationParser(
                extensionVisitor.stream()
                        .map((e) -> e.visitAnnotation(descriptor, visible))
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return new AnnotationParser(
                extensionVisitor.stream()
                        .map((e) -> e.visitTypeAnnotation(typeRef, typePath, descriptor, visible))
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        extensionVisitor.forEach((e) -> e.visitAttribute(attribute));
    }

    @Override
    public void visitEnd() {
        extensionVisitor.forEach(FieldParserExtension::visitEnd);
        if (extensionVisitor.stream().map(FieldParserExtension::shouldWriteField).reduce(true, Boolean::logicalAnd)) {
            if ((access & Opcodes.ACC_STATIC) == 0) {
                finish.apply(ClassParser.MemberType.INSTANCE).accept(this);
            } else {
                finish.apply(ClassParser.MemberType.STATIC).accept(this);
            }
        }
    }

    @Override
    public void write(ClassParser parser, StringBuilder builder) {
        builder.append("--- @field ").append(ClassParser.accessName(access)).append(" ").append(
                parser.hasMemberNamed(this) || ClassParser.KEYWORDS.contains(name) ? "m_" + name : name
        ).append(" ").append(ClassParser.instanceTypeDoc(descriptor)).append("\n");
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
