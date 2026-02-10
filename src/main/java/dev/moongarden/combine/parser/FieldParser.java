package dev.moongarden.combine.parser;

import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.List;
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

    public FieldParser(Type parent, int access, String name, String descriptor, String signature, Object value, Function<ClassParser.MemberType, Consumer<FieldParser>> finish, Set<Type> imports) {
        super(Opcodes.ASM9);
        this.parent = parent;
        this.access = access;
        this.name = name;
        this.descriptor = Type.getType(descriptor);
        imports.add(this.descriptor);
        this.signature = signature;
        this.value = value;
        this.finish = finish;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        super.visitAttribute(attribute);
    }

    @Override
    public void visitEnd() {
        if ((access & Opcodes.ACC_STATIC) == 0) {
            finish.apply(ClassParser.MemberType.INSTANCE).accept(this);
        } else {
            finish.apply(ClassParser.MemberType.STATIC).accept(this);
        }
        super.visitEnd();
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
