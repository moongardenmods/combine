package dev.moongarden.combine.parser;

import dev.moongarden.combine.extension.api.AnnotationParserExtension;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.Objects;

public class AnnotationParser extends AnnotationVisitor {

    private final List<AnnotationParserExtension> extensionVisitors;

    public AnnotationParser(List<AnnotationParserExtension> extensionVisitors) {
        super(Opcodes.ASM9);
        this.extensionVisitors = extensionVisitors;
    }

    @Override
    public void visit(String name, Object value) {
        extensionVisitors.forEach((e) -> e.visit(name, value));
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        extensionVisitors.forEach((e) -> e.visitEnum(name, descriptor, value));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        return new AnnotationParser(
                extensionVisitors.stream()
                        .map((e) -> e.visitAnnotation(name, descriptor))
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        return new AnnotationParser(
                extensionVisitors.stream()
                        .map((e) -> e.visitArray(name))
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
