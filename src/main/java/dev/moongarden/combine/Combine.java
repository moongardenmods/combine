package dev.moongarden.combine;

import dev.moongarden.combine.parser.ClassParser;
import org.jspecify.annotations.NonNull;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Combine {
	public static final Logger LOGGER = LoggerFactory.getLogger("Combine");
	public static final boolean IGNORE_ACCESS = System.getProperty("combine.ignore_access") != null;
	public static final String OUTPUT = Optional.ofNullable(System.getProperty("combine.output")).orElse("./docs");

	private static final List<Path> JARS = new ArrayList<>();
	private static final List<FileSystem> FILESYSTEMS = new ArrayList<>();

	static void main() {
		generate();
	}

    public static void generate() {
		{ // Find system libraries
			FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));
			JARS.add(fs.getPath("/"));
			FILESYSTEMS.add(fs);
		}

		// Find libraries on class path
		String classPathString = System.getProperty("java.class.path");
		if (classPathString == null) throw new IllegalStateException(
				"Could not find Java class path, documentation can not be generated"
		);
		for (String s : classPathString.split(":")) {
			Path jar = Path.of(s);
			if (!Files.exists(jar)) {
                LOGGER.warn("Location {} on classpath does not exist", jar);
				continue;
			}
			if (Files.isDirectory(jar)) {
				JARS.add(jar);
			} else {
				try {
					FileSystem fs = FileSystems.newFileSystem(jar);
					JARS.add(fs.getPath("/"));
					FILESYSTEMS.add(fs);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		// Resolve the output directory
		Path base = Path.of(OUTPUT);
		try { Files.createDirectories(base); } catch (IOException e) { throw new RuntimeException(e); }

		// Set up variables used across both read pass and write pass
        Queue<ClassParser> classQueue = new ArrayDeque<>();
		final AtomicInteger parsed = new AtomicInteger(0);
		final AtomicLong lastMillis = new AtomicLong(0);

		// Read classes in JARs
		for (Path jar : JARS) {
			try {
				Files.walkFileTree(jar, new SimpleFileVisitor<>() {
					@Override
					public @NonNull FileVisitResult visitFile(@NonNull Path file, @NonNull BasicFileAttributes attrs) throws IOException {
						String name = file.getFileName().toString();
						if (name.endsWith(".class") && !(name.equals("module-info.class") || name.equals("package-info.class"))) {
							ClassParser parser = new ClassParser();
							new ClassReader(Files.newInputStream(file)).accept(parser, ClassReader.SKIP_CODE);
							classQueue.add(parser);
							parsed.incrementAndGet();
							long now = System.currentTimeMillis();
							if (now >= lastMillis.addAndGet(2000)) {
								LOGGER.info("{} classes parsed", parsed.get());
								lastMillis.set(now);
							}
						}
						return FileVisitResult.CONTINUE;
					}
				});
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		// Write Lua files out to output directory
		int i = 0;
		while (!classQueue.isEmpty()) {
			ClassParser parser = classQueue.poll();
			if (parser != null) {
				Path save = base.resolve(parser.path());
				StringBuilder builder = new StringBuilder();
				parser.write(builder);
				try {
					Files.createDirectories(save.getParent());
					Files.writeString(save, builder);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				long now = System.currentTimeMillis();
				if (now >= lastMillis.addAndGet(2000)) {
					LOGGER.info(" {} files written", i);
					lastMillis.set(now);
				}
				i++;
			}
		}
		LOGGER.info("{} classes parsed | {} files written", parsed.get(), i);
		for (FileSystem filesystem : FILESYSTEMS) {
			if (!filesystem.isReadOnly()) {
				try {
					filesystem.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
        }
		LOGGER.info("Documentation generation complete.");
	}
}