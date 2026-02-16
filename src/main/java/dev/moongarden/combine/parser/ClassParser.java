package dev.moongarden.combine.parser;

import dev.moongarden.combine.Combine;
import dev.moongarden.combine.extension.api.*;
import org.objectweb.asm.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClassParser extends ClassVisitor {
    public static final Set<String> KEYWORDS = Set.of(
            "and", "elseif", "end", "function", "in", "local",
            "nil", "not", "or", "repeat", "then", "until"
    );

    private final Set<Type> imports = new HashSet<>();
    private final List<NamedWritable> constructors = new ArrayList<>();
    private final List<NamedWritable> instanceMethods = new ArrayList<>();
    private final List<NamedWritable> instanceFields = new ArrayList<>();
    private final List<NamedWritable> classMethods = new ArrayList<>();
    private final List<NamedWritable> classFields = new ArrayList<>();
    private final List<ClassParserExtension> extensionVisitor = Combine.EXTENSIONS.stream()
            .map(CombineExtension::createClassParser)
            .filter(Objects::nonNull)
            .toList();

    private Type classType;
    private Type superType;
    private final List<Type> interfaces = new ArrayList<>();
    private int access;

    public ClassParser() {
        super(Opcodes.ASM9);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        extensionVisitor.forEach((e) -> e.visit(version, access, name, signature, superName, interfaces));
        this.classType = Type.getType('L' + name + ';');
        if (superName != null) {
            this.superType = Type.getType('L' + superName + ';');
            imports.add(this.superType);
        }
        for (String str : interfaces) {
            Type iface = Type.getType('L' + str + ';');
            this.interfaces.add(iface);
            imports.add(iface);
        }
        this.access = access;
    }

    @Override
    public void visitSource(String source, String debug) {
        extensionVisitor.forEach((e) -> e.visitSource(source, debug));
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
        extensionVisitor.forEach((e) -> e.visitModule(name, access, version));
        return super.visitModule(name, access, version);
    }

    @Override
    public void visitNestHost(String nestHost) {
        extensionVisitor.forEach((e) -> e.visitNestHost(nestHost));
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
    public void visitPermittedSubclass(String permittedSubclass) {
        extensionVisitor.forEach((e) -> e.visitPermittedSubclass(permittedSubclass));
    }

    @Override
    public void visitNestMember(String nestMember) {
        extensionVisitor.forEach((e) -> e.visitNestMember(nestMember));
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        extensionVisitor.forEach((e) -> e.visitOuterClass(owner, name, descriptor));
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        extensionVisitor.forEach((e) -> e.visitInnerClass(name, outerName, innerName, access));
    }

    @Override
    public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
        extensionVisitor.forEach((e) -> e.visitRecordComponent(name, descriptor, signature));
        return null;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if (((access & Opcodes.ACC_PUBLIC) != 0 || Combine.IGNORE_ACCESS) &&
                extensionVisitor.stream()
                        .map((e) -> e.shouldVisitField(access, name, descriptor, signature, value))
                        .reduce(true, Boolean::logicalAnd)
        ) {
            Function<MemberType, Consumer<FieldParser>> function = (type) -> switch (type) {
                case STATIC -> classFields::add;
                case INSTANCE -> instanceFields::add;
                case CTOR -> null;
            };
            List<FieldParserExtension> fieldVisitor = extensionVisitor.stream()
                    .map((e) -> e.visitField(access, name, descriptor, signature, value))
                    .filter(Objects::nonNull)
                    .toList();
            return new FieldParser(classType, access, name, descriptor, signature, value, function, imports, fieldVisitor);
        }
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (((access & Opcodes.ACC_PUBLIC) != 0 || Combine.IGNORE_ACCESS) && !name.equals("<clinit>") &&
                extensionVisitor.stream()
                        .map((e) -> e.shouldVisitMethod(access, name, descriptor, signature, exceptions))
                        .reduce(true, Boolean::logicalAnd)
        ) {
            Function<MemberType, Consumer<MethodParser>> function = (type) -> switch (type) {
                case STATIC -> classMethods::add;
                case INSTANCE -> instanceMethods::add;
                case CTOR -> constructors::add;
            };

            List<MethodParserExtension> methodVisitor = extensionVisitor.stream()
                    .map((e) -> e.visitMethod(access, name, descriptor, signature, exceptions))
                    .filter(Objects::nonNull)
                    .toList();
            return new MethodParser(classType, access, name, descriptor, signature, exceptions, function, imports, methodVisitor);
        }
        return null;
    }

    @Override
    public void visitEnd() {
        extensionVisitor.forEach(ClassParserExtension::visitEnd);
    }

    public void write(StringBuilder builder) {
        builder.append("--- @meta ").append(classType.getClassName()).append("\n\n");
        for (Type type : imports) {
            if (type.getSort() == Type.OBJECT) {
                builder.append("--- @module '").append(type.getClassName()).append("'\n");
            }
        }
        builder.append("--- @class ").append(instanceTypeDoc(classType)).append(": ");
        if (superType == null) {
            builder.append("InstanceUserdata");
        } else {
            builder.append(instanceTypeDoc(superType));
        }
        interfaces.forEach((i) -> builder.append(", ").append(instanceTypeDoc(i)));
        builder.append("\n").append("--- ").append(accessName(access)).append("\n");
        extensionVisitor.forEach((e) -> e.writeInInstance(builder));
        instanceFields.forEach((f) -> f.write(this, builder));
        builder.append("local ").append(instanceTypeDoc(classType)).append(" = {}\n\n");
        instanceMethods.forEach((m) -> m.write(this, builder));

        // Static documentation
        builder.append("--- @class ").append(classTypeDoc(classType)).append(": ");
        if (superType == null) {
            builder.append("ClassUserdata");
        } else {
            builder.append(classTypeDoc(superType));
        }
        builder.append("\n");
        extensionVisitor.forEach((e) -> e.writeInClass(builder));
        classFields.forEach((f) -> f.write(this, builder));
        constructors.forEach((c) -> c.write(this, builder));
        builder.append("local ").append(classTypeDoc(classType)).append(" = {}\n\n");
        classMethods.forEach((m) -> m.write(this, builder));
        builder.append("\nreturn ").append(classTypeDoc(classType)).append("\n");
    }

    public String path() {
        return classType.getClassName().replace('.', '/').replace('$', '_') + ".d.lua";
    }

    public boolean hasMemberNamed(NamedWritable named) {
        List<NamedWritable> list;
        if (named instanceof FieldParser) {
            list = named.isStatic() ? classMethods : instanceMethods;
        } else {
            list = named.isStatic() ? classFields : instanceFields;
        }
        for (NamedWritable n : list) {
            if (n.getName().equals(named.getName())) {
                return true;
            }
        }
        return false;
    }

    public static String accessName(int access) {
        if ((access & Opcodes.ACC_PUBLIC) != 0) {
            return "public";
        } else if ((access & Opcodes.ACC_PROTECTED) != 0) {
            return "protected";
        } else if ((access & Opcodes.ACC_PRIVATE) != 0) {
            return "private";
        } else {
            return "package";
        }
    }

    public static String instanceTypeDoc(Type classType) {
        String[] split = classType.getClassName().split("\\.");
        return split[split.length-1].replace('$', '_');
    }

    public static String classTypeDoc(Type classType) {
        return instanceTypeDoc(classType) + "Class";
    }

    public enum MemberType {
        STATIC,
        INSTANCE,
        CTOR;
    }
}
