package dev.moongarden.combine.extension.impl;

import dev.moongarden.combine.extension.api.AnnotationParserExtension;
import dev.moongarden.combine.extension.api.ClassParserExtension;
import dev.moongarden.combine.extension.api.FieldParserExtension;
import dev.moongarden.combine.extension.api.MethodParserExtension;
import org.objectweb.asm.*;

public class ClassParserExtensionImpl implements ClassParserExtension {

    @Override
    public boolean shouldVisitField(int access, String name, String descriptor, String signature, Object value) {
        return true;
    }

    @Override
    public boolean shouldVisitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return true;
    }

    @Override
    public void writeInInstance(StringBuilder builder) {

    }

    @Override
    public void writeInClass(StringBuilder builder) {

    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

    }

    @Override
    public void visitSource(String source, String debug) {

    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
        return null;
    }

    @Override
    public void visitNestHost(String nestHost) {

    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {

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
    public void visitNestMember(String nestMember) {

    }

    @Override
    public void visitPermittedSubclass(String permittedSubclass) {

    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {

    }

    @Override
    public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
        return null;
    }

    @Override
    public FieldParserExtension visitField(int access, String name, String descriptor, String signature, Object value) {
        return null;
    }

    @Override
    public MethodParserExtension visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return null;
    }

    @Override
    public void visitEnd() {

    }


}
