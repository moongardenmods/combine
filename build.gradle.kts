plugins {
	`maven-publish`
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
	mods {
		register(project.name) {
			sourceSet(sourceSets["main"])
		}
	}

	runs {
		register("genLuaSources") {
			client()
			name("Generate Lua Sources")
			vmArg("-Dcombine.enabled")
			vmArg("-Dcombine.targets=net.minecraft.client.Minecraft;com.mojang.authlib.minecraft.client.MinecraftClient;net.fabricmc.loader.api.FabricLoader")
			vmArg("-Dcombine.output=../docs")
			ideConfigGenerated(true)
		}
	}
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${project.properties["minecraft_version"]}")
	implementation("net.fabricmc:fabric-loader:${project.properties["loader_version"]}")
	implementation(include("me.basiqueevangelist:enhanced-reflection:${project.properties["enhanced_reflection_version"]}")!!)
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