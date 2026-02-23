package dev.moongarden.combine.extension.impl;

import dev.moongarden.combine.extension.api.AnnotationParserExtension;
import dev.moongarden.combine.extension.api.MethodParserExtension;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.*;

public class MethodParserExtensionImpl implements MethodParserExtension {

    @Override
    public boolean shouldWriteMethod() {
        return true;
    }

    @Override
    public boolean shouldWriteParameter(int parameter) { return true; }

    @Override
    public String modifyParameterType(String type) {
        return type;
    }

    @Override
    public String modifyReturnType(String type) {
        return type;
    }

    @Override
    public String modifyMethodName(String name) {
        return name;
    }

    @Override
    public void visitParameter(String name, int access) {

    }

    @Override
    public AnnotationParserExtension visitAnnotationDefault() {
        return null;
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
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {

    }

    @Override
    public AnnotationParserExtension visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        return null;
    }

    @Override
    public void visitAttribute(Attribute attribute) {

    }

    @Override
    public void visitCode() {

    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {

    }

    @Override
    public void visitInsn(int opcode) {

    }

    @Override
    public void visitIntInsn(int opcode, int operand) {

    }

    @Override
    public void visitVarInsn(int opcode, int varIndex) {

    }

    @Override
    public void visitTypeInsn(int opcode, String type) {

    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {

    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {

    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {

    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {

    }

    @Override
    public void visitLabel(Label label) {

    }

    @Override
    public void visitLdcInsn(Object value) {

    }

    @Override
    public void visitIincInsn(int varIndex, int increment) {

    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {

    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {

    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {

    }

    @Override
    public AnnotationParserExtension visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return null;
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {

    }

    @Override
    public AnnotationParserExtension visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return null;
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {

    }

    @Override
    public AnnotationParserExtension visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        return null;
    }

    @Override
    public void visitLineNumber(int line, Label start) {

    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {

    }

    @Override
    public void visitEnd() {

    }
}
