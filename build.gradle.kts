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

    // antlr & JiteScript
    antlr("org.antlr", "antlr4", "4.7.1")
    compile("org.antlr", "antlr4-runtime", "4.7.1")
    compile("ca.mcgill.sable", "jasmin", "3.0.1")

    // test libs
    testImplementation("org.junit.jupiter", "junit-jupiter", "5.6.2")
}

tasks {
    generateGrammarSource {
        maxHeapSize = "64m"
        arguments.add("-visitor")
    }
    test {
        useJUnitPlatform()
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
