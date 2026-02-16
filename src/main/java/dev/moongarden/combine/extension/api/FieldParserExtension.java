package dev.moongarden.combine.extension.api;


import org.objectweb.asm.Attribute;
import org.objectweb.asm.TypePath;

public interface FieldParserExtension {

    boolean shouldWriteField();

    String modifyFieldName(String name);

    AnnotationParserExtension visitAnnotation(final String descriptor, final boolean visible);

    AnnotationParserExtension visitTypeAnnotation(
            final int typeRef, final TypePath typePath, final String descriptor, final boolean visible);

    void visitAttribute(final Attribute attribute);

    void visitEnd();
}
