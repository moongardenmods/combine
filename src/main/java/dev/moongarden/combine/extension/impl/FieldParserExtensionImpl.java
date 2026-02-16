package dev.moongarden.combine.extension.impl;

import dev.moongarden.combine.extension.api.AnnotationParserExtension;
import dev.moongarden.combine.extension.api.FieldParserExtension;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.TypePath;

public class FieldParserExtensionImpl implements FieldParserExtension {

    @Override
    public boolean shouldWriteField() {
        return true;
    }

    @Override
    public String modifyFieldName(String name) {
        return name;
    }

    @Override
    public AnnotationParserExtension visitAnnotation(String descriptor, boolean visible) {
        return null;
    }

    @Override
    public AnnotationParserExtension visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return null;
    }

    @Override
    public void visitAttribute(Attribute attribute) {

    }

    @Override
    public void visitEnd() {

    }
}
