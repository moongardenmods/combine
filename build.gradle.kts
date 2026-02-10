plugins {
	`maven-publish`

	// NOTE: Loom is only brought in for testing functionality against the minecraft classpath.
	// It *really* is not necessary for this project.
	id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
}

version = properties["version"].toString()
group = properties["group"].toString()


base {
	archivesName = project.properties["id"].toString()
}

repositories {
	maven("https://basique.top/maven/releases") {
		content {
			includeGroup("me.basiqueevangelist")
		}
	}
}

loom {
	runs {
		named("client") {
			ideConfigGenerated(false)
		}

		named("server") {
			ideConfigGenerated(false)
		}
	}
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${project.properties["minecraft_version"]}")
	implementation("net.fabricmc:fabric-loader:${project.properties["loader_version"]}")
	implementation("me.basiqueevangelist:enhanced-reflection:${project.properties["enhanced_reflection_version"]}")
}

tasks {
	processResources {
		inputs.property("version", project.version)
		inputs.property("name", project.properties["name"])
		inputs.property("id", project.properties["id"])

		filesMatching("fabric.mod.json") {
			expand(mutableMapOf("version" to project.version, "name" to project.properties["name"], "id" to project.properties["id"]))
		}
	}

	jar {
		inputs.property("archivesName", project.properties["id"])

		from("LICENSE") {
			rename { "${it}_${project.properties["id"].toString()}"}
		}
	}

	runClient {
		enabled = false
	}

	runServer {
		enabled = false
	}

	register<JavaExec>("genLuaSources") {
		group = "fabric"

		classpath = sourceSets["main"].runtimeClasspath
		mainClass = "dev.moongarden.combine.Combine"
		jvmArgs = listOf("-Dcombine.output=../docs")
		workingDir = file("run")
	}

	register<JavaExec>("genLuaSourcesAll") {
		group = "fabric"

		classpath = sourceSets["main"].runtimeClasspath
		mainClass = "dev.moongarden.combine.Combine"
		jvmArgs = listOf("-Dcombine.output=../docs", "-Dcombine.ignoreAccess")
		workingDir = file("run")
	}
}

java {
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
}

// configure the maven publication
publishing {
	publications {
		register("mavenJava", MavenPublication::class) {
			from(components["java"])
			groupId = group.toString()
			artifactId = base.archivesName.get()
			version = project.version.toString()
		}
	}

	repositories {
		maven {
			name = "hugeblankRelease"
			url = uri("https://maven.hugeblank.dev/releases")
			credentials(PasswordCredentials::class)
			authentication {
				create<BasicAuthentication>("basic")
			}
		}
		maven {
			name = "hugeblankSnapshot"
			url = uri("https://maven.hugeblank.dev/snapshots")
			credentials(PasswordCredentials::class)
			authentication {
				create<BasicAuthentication>("basic")
			}
		}
	}
}