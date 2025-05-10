// build.gradle.kts (nível do projeto)
buildscript {
    // Definir variáveis em Kotlin DSL
    val compose_version by extra("1.5.1")  // Versão compatível com compileSdk 34
    val kotlin_version by extra("1.9.0")  // Mantido para compatibilidade

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.1.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}