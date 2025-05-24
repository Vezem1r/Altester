import java.util.Properties

val generatedDir = layout.buildDirectory.dir("generated").get()
val properties = Properties().apply {
	val propsFile = file("src/main/resources/application.properties")
	if (propsFile.exists()) {
		load(propsFile.inputStream())
	}
}

fun getAppProperty(key: String, defaultValue: String = ""): String {
	return properties.getProperty(key) ?: defaultValue
}

plugins {
	java
	id("org.springframework.boot") version "3.4.5"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.openapi.generator") version "7.12.0"
}

group = "com.altester"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.core:jackson-databind")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	implementation("org.openapitools:jackson-databind-nullable:0.2.6")

	compileOnly("org.projectlombok:lombok:1.18.38")
	annotationProcessor("org.projectlombok:lombok:1.18.38")
}

openApiGenerate {
	generatorName.set("java")
	remoteInputSpec.set("${getAppProperty("altester.url")}/v3/api-docs")
	outputDir.set("$generatedDir/openapi")
	apiPackage.set("com.altester.client.api")
	modelPackage.set("com.altester.client.model")
	invokerPackage.set("com.altester.client")
	configOptions.set(mapOf(
		"dateLibrary" to "java8",
		"library" to "restclient",
	))
}

tasks.compileJava {
	dependsOn("openApiGenerate")
}

sourceSets {
	main {
		java {
			srcDir("$generatedDir/openapi/src/main/java")
		}
	}
}