package dev.moongarden.combine.parser;

public interface NamedWritable {
    void write(ClassParser parser, StringBuilder builder);
    String getName();
    boolean isStatic();
}
