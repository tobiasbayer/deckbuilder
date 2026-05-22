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
    testImplementation("io.mockk:mockk:1.14.9")
    testImplementation("com.ninja-squad:springmockk:5.0.1")
    testImplementation("org.wiremock:wiremock-standalone:3.13.2")
}

kotlin {
    jvmToolchain(24)

    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("--enable-native-access=ALL-UNNAMED", "-XX:+EnableDynamicAgentLoading")
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.test {
    useJUnitPlatform {
        excludeTags("token-burning")
    }
}

tasks.register<Test>("tokenBurningTest") {
    description = "Runs integration tests against real OpenAI."
    group = "verification"

    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath

    useJUnitPlatform {
        includeTags("token-burning")
    }

    shouldRunAfter("test")
}
