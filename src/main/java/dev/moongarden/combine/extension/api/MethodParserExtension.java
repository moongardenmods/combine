package dev.moongarden.combine.extension.api;

import org.jspecify.annotations.Nullable;
import org.objectweb.asm.*;

public interface MethodParserExtension {

    boolean shouldWriteMethod();

    String modifyMethodName(String name);

    boolean shouldWriteParameter(int parameter);

    String modifyParameterType(String type);

    String modifyReturnType(String type);

    void visitParameter(final String name, final int access);

    AnnotationParserExtension visitAnnotationDefault();

    AnnotationParserExtension visitAnnotation(final String descriptor, final boolean visible);

    AnnotationParserExtension visitTypeAnnotation(
            final int typeRef, final TypePath typePath, final String descriptor, final boolean visible);

    void visitAnnotableParameterCount(final int parameterCount, final boolean visible);

    AnnotationParserExtension visitParameterAnnotation(
            final int parameter, final String descriptor, final boolean visible);

    void visitAttribute(final Attribute attribute);

    void visitCode();

    void visitFrame(
            final int type,
            final int numLocal,
            final Object[] local,
            final int numStack,
            final Object[] stack);

    void visitInsn(final int opcode);

    void visitIntInsn(final int opcode, final int operand);

    void visitVarInsn(final int opcode, final int varIndex);

    void visitTypeInsn(final int opcode, final String type);

    void visitFieldInsn(
            final int opcode, final String owner, final String name, final String descriptor);

    void visitMethodInsn(
            final int opcode,
            final String owner,
            final String name,
            final String descriptor,
            final boolean isInterface);

    void visitInvokeDynamicInsn(
            final String name,
            final String descriptor,
            final Handle bootstrapMethodHandle,
            final Object... bootstrapMethodArguments);

    void visitJumpInsn(final int opcode, final Label label);

    void visitLabel(final Label label);

    void visitLdcInsn(final Object value);

    void visitIincInsn(final int varIndex, final int increment);

    void visitTableSwitchInsn(
            final int min, final int max, final Label dflt, final Label... labels);

    void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels);

    void visitMultiANewArrayInsn(final String descriptor, final int numDimensions);

    AnnotationParserExtension visitInsnAnnotation(
            final int typeRef, final TypePath typePath, final String descriptor, final boolean visible);

    void visitTryCatchBlock(
            final Label start, final Label end, final Label handler, final String type);

    AnnotationParserExtension visitTryCatchAnnotation(
            final int typeRef, final TypePath typePath, final String descriptor, final boolean visible);

    void visitLocalVariable(
            final String name,
            final String descriptor,
            final String signature,
            final Label start,
            final Label end,
            final int index);

    AnnotationParserExtension visitLocalVariableAnnotation(
            final int typeRef,
            final TypePath typePath,
            final Label[] start,
            final Label[] end,
            final int[] index,
            final String descriptor,
            final boolean visible);

    void visitLineNumber(final int line, final Label start);

    void visitMaxs(final int maxStack, final int maxLocals);

    void visitEnd();
}
