import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val lwjglVersion = "3.2.3"

@Suppress("INACCESSIBLE_TYPE")
val lwjglNatives = when (org.gradle.internal.os.OperatingSystem.current()) {
    org.gradle.internal.os.OperatingSystem.LINUX   -> "natives-linux"
    org.gradle.internal.os.OperatingSystem.MAC_OS  -> "natives-macos"
    org.gradle.internal.os.OperatingSystem.WINDOWS -> "natives-windows"
    else -> throw Error("Unrecognized or unsupported operating system. Please set \"lwjglNatives\" manually.")
}

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.6.10"
}

group = "com.exerro"
version = "1.0-SNAPSHOT"

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += "-language-version"
    kotlinOptions.freeCompilerArgs += "1.7"
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation(files("lib/LWAF-1.0-SNAPSHOT.jar"))

    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-opengl")
    implementation("org.lwjgl", "lwjgl-stb")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = lwjglNatives)
}
