package dev.moongarden;

import dev.moongarden.structure.ClassLuaElement;
import me.basiqueevangelist.enhancedreflection.api.EClass;
import me.basiqueevangelist.enhancedreflection.impl.GenericEClassImpl;
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
		try { Files.createDirectories(base); } catch (IOException e) { throw new RuntimeException(e); }

		AtomicBoolean done = new AtomicBoolean(false);
		Queue<ClassLuaElement> elementQueue = new ConcurrentLinkedQueue<>();
		final AtomicInteger size = new AtomicInteger(0);
		new Thread(() -> {
			long lastMillis = 0;
			for (int i = 0; i < VISIBLE.size(); i++) {
				EClass<?> clazz = VISIBLE.get(i);
				long now = System.currentTimeMillis();
				if (now % 1000 == 0 && now != lastMillis) {
					LOGGER.info("{}/{} classes parsed.", i, VISIBLE.size());
					lastMillis = now;
				}
				if (VISIBLE.size() % 100 == 0) size.set(VISIBLE.size());
				elementQueue.add(ClassLuaElement.from(clazz));
			}
			size.set(VISIBLE.size());
			done.set(true);
			VISIBLE.clear();
			LOGGER.info("Parsing Complete.");
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
				if (now % 1000 == 0  && now != lastMillis) {
					LOGGER.info("{}/{} classes written.", i, size.get());
					lastMillis = now;
				}
				i++;
			}
		}
		LOGGER.info("Writing Complete.");
	}
}