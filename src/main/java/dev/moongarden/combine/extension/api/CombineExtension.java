package dev.moongarden.combine.extension.api;

public interface CombineExtension {
    /// Method invoked every time combine comes across a new class.
    ClassParserExtension createClassParser();
}
