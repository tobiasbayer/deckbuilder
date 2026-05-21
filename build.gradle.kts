import org.gradle.kotlin.dsl.implementation

plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.deckbuilder"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

val langchain4jVersion = "1.15.0-beta25"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("dev.langchain4j:langchain4j-spring-boot4-starter:$langchain4jVersion")
    implementation("dev.langchain4j:langchain4j-open-ai-spring-boot4-starter:$langchain4jVersion")
    implementation("dev.langchain4j:langchain4j-document-parser-apache-pdfbox:${langchain4jVersion}")
    implementation("dev.langchain4j:langchain4j-embeddings-all-minilm-l6-v2-q:${langchain4jVersion}")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

kotlin {
    jvmToolchain(24)

    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
