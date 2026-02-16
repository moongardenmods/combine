package dev.moongarden.combine.extension.api;

public interface AnnotationParserExtension {

    void visit(final String name, final Object value);

    void visitEnum(final String name, final String descriptor, final String value);

    AnnotationParserExtension visitAnnotation(final String name, final String descriptor);

    AnnotationParserExtension visitArray(final String name);

    void visitEnd();
}
