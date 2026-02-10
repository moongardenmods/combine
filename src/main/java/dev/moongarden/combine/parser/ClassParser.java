package dev.moongarden.combine.parser;

import dev.moongarden.combine.Combine;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    private Type classType;
    private Type superType;
    private final List<Type> interfaces = new ArrayList<>();
    private int access;

    public ClassParser() {
        super(Opcodes.ASM9);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
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
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitSource(String source, String debug) {
        super.visitSource(source, debug);
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
        return super.visitModule(name, access, version);
    }

    @Override
    public void visitNestHost(String nestHost) {
        super.visitNestHost(nestHost);
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
    public void visitPermittedSubclass(String permittedSubclass) {
        super.visitPermittedSubclass(permittedSubclass);
    }

    @Override
    public void visitNestMember(String nestMember) {
        super.visitNestMember(nestMember);
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        super.visitOuterClass(owner, name, descriptor);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
        return super.visitRecordComponent(name, descriptor, signature);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if ((access & Opcodes.ACC_PUBLIC) != 0 || Combine.IGNORE_ACCESS) {
            Function<MemberType, Consumer<FieldParser>> function = (type) -> switch (type) {
                case STATIC -> classFields::add;
                case INSTANCE -> instanceFields::add;
                case CTOR -> null;
            };
            return new FieldParser(classType, access, name, descriptor, signature, value, function, imports);
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (((access & Opcodes.ACC_PUBLIC) != 0 || Combine.IGNORE_ACCESS) && !name.equals("<clinit>")) {
            Function<MemberType, Consumer<MethodParser>> function = (type) -> switch (type) {
                case STATIC -> classMethods::add;
                case INSTANCE -> instanceMethods::add;
                case CTOR -> constructors::add;
            };

            return new MethodParser(classType, access, name, descriptor, signature, exceptions, function, imports);
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
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
