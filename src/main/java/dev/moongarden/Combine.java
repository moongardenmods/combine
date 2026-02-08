package dev.moongarden;

import dev.moongarden.structure.ClassLuaElement;
import me.basiqueevangelist.enhancedreflection.api.EClass;
import me.basiqueevangelist.enhancedreflection.impl.GenericEClassImpl;
import net.fabricmc.loader.api.FabricLoader;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Combine {
	public static final Logger LOGGER = LoggerFactory.getLogger("Combine");
	public static final boolean ENABLED = System.getProperty("combine.enabled") != null;
	public static final String TARGETS = System.getProperty("combine.targets");
	public static final String OUTPUT = Optional.ofNullable(System.getProperty("combine.output")).orElse("./docs");

	private static final List<EClass<?>> VISIBLE = new ArrayList<>();

	public static void makeVisible(EClass<?> clazz) {
		if (
				clazz != null && !VISIBLE.contains(clazz) && !clazz.raw().isPrimitive() &&
				!clazz.raw().isArray()
		) {
			if (clazz instanceof GenericEClassImpl<?> genericEClass) {
				genericEClass.typeVariableValues().forEach((type) -> {
					if (type.upperBound().equals(genericEClass)) return;
					makeVisible(type.lowerBound());
					if (!type.lowerBound().equals(type.upperBound()) && !type.lowerBound().equals(genericEClass)) {
						makeVisible(type.upperBound());
					}
				});
				makeVisible(EClass.fromJava(genericEClass.raw()));
			} else {
				VISIBLE.add(clazz);
			}
		}
	}

	public static void generate() {
		if (TARGETS == null) throw new IllegalStateException("Expected System Property combine.targets");
		try {
			for (String className : TARGETS.split(";")) {
					if (!className.isEmpty())
						VISIBLE.add(EClass.fromJava(Combine.class.getClassLoader().loadClass(className)));
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		Path base = FabricLoader.getInstance().getGameDir().resolve(OUTPUT);
		try {
			Files.createDirectories(base);
            for (int i = 0; i < VISIBLE.size(); i++) {
				EClass<?> clazz = VISIBLE.get(i);
				if (i % 100 == 0) {
					LOGGER.info("{}/{} classes parsed.", i, VISIBLE.size());
				}
				ClassLuaElement.from(clazz).write(base);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}