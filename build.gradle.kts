plugins {
    kotlin("jvm") version "1.3.70"
    application
    antlr
    id("org.openjfx.javafxplugin") version "0.0.8"
}

group = "de.jakobteuber.turingc"
version = "1.0-SNAPSHOT"
application {
    mainClassName = "de.jakobteuber.turingc.MainKt"
}

repositories {
    mavenCentral()
}

javafx {
    version = "12.0.2"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.web")
}

dependencies {
    // stdlib
    implementation("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")

    // ui
    compile("no.tornado", "tornadofx", "1.7.19")
    compile("org.fxmisc.richtext", "richtextfx", "0.10.3")

    // Kotlin Script runtime
    // See also resources/META-INF/services/javax.script.ScriptEngineFactory
    compile("org.jetbrains.kotlin", "kotlin-script-runtime", "1.3.60")
    compile("org.jetbrains.kotlin", "kotlin-script-util", "1.3.60")
    compile("org.jetbrains.kotlin", "kotlin-compiler-embeddable", "1.3.60")
    runtime("org.jetbrains.kotlin", "kotlin-scripting-compiler-embeddable", "1.3.60")

    // antlr & jasmin (Java Assembler)
    antlr("org.antlr", "antlr4", "4.7.1")
    compile("org.antlr", "antlr4-runtime", "4.7.1")
    compile("ca.mcgill.sable", "jasmin", "3.0.1")

    // test libs
    testRuntime("org.jetbrains.kotlin", "kotlin-scripting-compiler-embeddable", "1.3.60")
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.3.1")
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "5.3.1")
}

tasks {
    generateGrammarSource {
        maxHeapSize = "64m"
        arguments.add("-visitor")
    }
    test {
        useJUnit()
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
