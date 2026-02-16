package dev.moongarden.combine.extension.api;

import org.objectweb.asm.*;

public interface ClassParserExtension {

    boolean shouldVisitField(int access, String name, String descriptor, String signature, Object value);

    boolean shouldVisitMethod(int access, String name, String descriptor, String signature, String[] exceptions);

    void writeInInstance(StringBuilder builder);

    void writeInClass(StringBuilder builder);

    void visit(
            final int version,
            final int access,
            final String name,
            final String signature,
            final String superName,
            final String[] interfaces);

    void visitSource(final String source, final String debug);

    ModuleVisitor visitModule(final String name, final int access, final String version);

    void visitNestHost(final String nestHost);

    void visitOuterClass(final String owner, final String name, final String descriptor);

    AnnotationParserExtension visitAnnotation(final String descriptor, final boolean visible);

    AnnotationParserExtension visitTypeAnnotation(
            final int typeRef, final TypePath typePath, final String descriptor, final boolean visible);

    void visitAttribute(final Attribute attribute);

    void visitNestMember(final String nestMember);

    void visitPermittedSubclass(final String permittedSubclass);

    void visitInnerClass(
            final String name, final String outerName, final String innerName, final int access);

    RecordComponentVisitor visitRecordComponent(
            final String name, final String descriptor, final String signature);

    FieldParserExtension visitField(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final Object value);

    MethodParserExtension visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions);

    void visitEnd();
}
