package dev.moongarden;

import dev.moongarden.structure.ClassLuaElement;
import me.basiqueevangelist.enhancedreflection.api.EClass;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Combine {
	public static final Logger LOGGER = LoggerFactory.getLogger("Combine");
	public static final boolean ENABLED = System.getProperty("combine.enabled") != null;
	public static final String TARGETS = System.getProperty("combine.targets");
	public static final boolean IGNORE_ACCESS = System.getProperty("combine.ignore_access") != null;
	public static final String OUTPUT = Optional.ofNullable(System.getProperty("combine.output")).orElse("./docs");

	private static final List<EClass<?>> VISIBLE = new ArrayList<>();

	public static void makeVisible(EClass<?> clazz) {
		if (clazz != null && !clazz.raw().isPrimitive()) {
			if (clazz.raw().isArray()) {
				makeVisible(clazz.arrayComponent());
				return;
			}
			EClass<?> stripped = EClass.fromJava(clazz.raw());
			if (!VISIBLE.contains(stripped)) {
				clazz.typeVariableValues().forEach((type) -> {
					if (type.upperBound().equals(clazz)) return;
					makeVisible(type.lowerBound());
					if (!type.lowerBound().equals(type.upperBound()) && !type.lowerBound().equals(clazz)) {
						makeVisible(type.upperBound());
					}
				});
				VISIBLE.add(stripped);
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
		try { Files.createDirectories(base); } catch (IOException e) { throw new RuntimeException(e); }

		AtomicBoolean done = new AtomicBoolean(false);
		Queue<ClassLuaElement> elementQueue = new ConcurrentLinkedQueue<>();
		final AtomicInteger size = new AtomicInteger(0);
		final AtomicInteger parsed = new AtomicInteger(0);
		new Thread(() -> {
			for (int i = 0; i < VISIBLE.size(); i++) {
				EClass<?> clazz = VISIBLE.get(i);
				parsed.set(i+1);
				size.set(VISIBLE.size());
				elementQueue.add(ClassLuaElement.from(clazz));
			}
			size.set(VISIBLE.size());
			done.set(true);
		}).start();
		int i = 0;
		long lastMillis = 0;
		while (!done.get() || !elementQueue.isEmpty()) {
			ClassLuaElement element = elementQueue.poll();
			if (element != null) {
				Path save = element.resolve(base);
				StringBuilder builder = new StringBuilder();
				element.build(builder);
				try {
					Files.createDirectories(save.getParent());
					Files.writeString(save, builder);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				long now = System.currentTimeMillis();
				if (now >= lastMillis+2000) {
					LOGGER.info("{} classes discovered | {} classes parsed | {} classes written", size.get(), parsed.get(), i);
					lastMillis = now;
				}
				i++;
			}
		}
		LOGGER.info("{} classes discovered | {} classes parsed | {} classes written", size.get(), parsed.get(), i);
		LOGGER.info("Documentation generation complete.");
	}
}