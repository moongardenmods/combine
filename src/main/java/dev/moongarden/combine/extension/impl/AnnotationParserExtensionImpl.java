package dev.moongarden.combine.extension.impl;

import dev.moongarden.combine.extension.api.AnnotationParserExtension;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

public class AnnotationParserExtensionImpl implements AnnotationParserExtension {
    @Override
    public void visit(String name, Object value) {

    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {

    }

    @Override
    public AnnotationParserExtension visitAnnotation(String name, String descriptor) {
        return null;
    }

    @Override
    public AnnotationParserExtension visitArray(String name) {
        return null;
    }

    @Override
    public void visitEnd() {

    }
}
