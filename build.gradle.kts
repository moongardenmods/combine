plugins {
	`maven-publish`
	id("java")
}

version = properties["version"].toString()
group = properties["group"].toString()


base {
	archivesName = project.properties["id"].toString()
}

repositories {
	maven("https://piston-maven.hugeblank.dev/")
	maven("https://maven.fabricmc.net/") {
		content {
			includeGroup("net.fabricmc")
		}
	}
	maven("https://basique.top/maven/releases") {
		content {
			includeGroup("me.basiqueevangelist")
		}
	}
	mavenCentral()
}

configurations {
	register("localRuntime") {
		isCanBeResolved = true
		isCanBeConsumed = false
	}

	runtimeClasspath {
		extendsFrom(getByName("localRuntime"))
	}
}

dependencies {
	add("localRuntime", "net.minecraft:client:${project.properties["minecraft_version"]}")
	add("localRuntime", "net.fabricmc:fabric-loader:${project.properties["loader_version"]}")

	implementation("org.jspecify:jspecify:${project.properties["jspecify_version"]}")
	implementation("org.ow2.asm:asm:${project.properties["asm_version"]}")
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

	register<JavaExec>("genLuaSources") {
		group = "allium"

		classpath = sourceSets["main"].runtimeClasspath
		mainClass = "dev.moongarden.combine.Combine"
		jvmArgs = listOf("-Dcombine.output=../docs")
		workingDir = file("run")
	}

	register<JavaExec>("genLuaSourcesAll") {
		group = "allium"

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
			artifactId = project.properties["id"].toString()
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