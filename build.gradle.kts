// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    // alias(libs.plugins.sonarqube)
    jacoco
}

// ===============================
// Jacoco Report Task
// ===============================
tasks.register<JacocoReport>("jacocoFullReport") {
    group = "Reports"
    description = "Generate JaCoCo coverage reports (Unit + Instrumented) for all modules"

    val fileFilter = listOf(
        // Android-specific generated files
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",

        // Hilt & Dagger Generated
        "**/hilt_aggregated_deps/**",
        "**/dagger/hilt/internal/**",
        "**/dagger/hilt/android/internal/**",
        "**/*_MembersInjector.class",
        "**/Dagger*Component.class",
        "**/*Module_*Factory.class",
        "**/*_Factory.class",
        "**/*_Provide*Factory.class",
        "**/*_HiltModules*.*",

        // Compose & UI Generated
        "**/*Preview*.*",
        "**/*ComposableSingletons*.*",

        // Compose Destinations
        "**/destinations/**",
        "**/*Destination.class",
        "**/*NavGraph.class"
    )

    val javaClasses = mutableListOf<FileTree>()
    val kotlinClasses = mutableListOf<FileTree>()
    val sourceDirs = mutableListOf<File>()
    val executionDataFiles = mutableListOf<FileTree>()

    rootProject.subprojects.forEach { proj ->
        val buildDir = proj.layout.buildDirectory.get().asFile

        // Dossiers sources possibles (priorité à la version finale transformée)
        val transformedDir = File(buildDir, "intermediates/classes/debug/transformDebugClassesWithAsm/dirs")
        val kotlinDir = File(buildDir, "intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")
        val javaDir = File(buildDir, "intermediates/javac/debug/compileDebugJavaWithJavac/classes")

        if (transformedDir.exists()) {
            // Version finale (recommandé pour Hilt)
            javaClasses.add(proj.fileTree(transformedDir) {
                include("com/openclassrooms/rebonnte/**")
                exclude(fileFilter)
            })
        } else {
            // Fallback si pas de transformation
            if (kotlinDir.exists()) {
                javaClasses.add(proj.fileTree(kotlinDir) {
                    include("com/openclassrooms/rebonnte/**")
                    exclude(fileFilter)
                })
            }
            if (javaDir.exists()) {
                javaClasses.add(proj.fileTree(javaDir) {
                    include("com/openclassrooms/rebonnte/**")
                    exclude(fileFilter)
                })
            }
        }

        // Sources
        val sDirJava = File(proj.projectDir, "src/main/java")
        if (sDirJava.exists()) sourceDirs.add(sDirJava)
        
        val sDirKotlin = File(proj.projectDir, "src/main/kotlin")
        if (sDirKotlin.exists()) sourceDirs.add(sDirKotlin)

        // Données d'exécution (Unitaires + Instrumentés)
        executionDataFiles.add(proj.fileTree(buildDir) {
            include(
                "jacoco/*.exec",
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "outputs/code_coverage/debugAndroidTest/connected/**/*.ec"
            )
        })
    }

    sourceDirectories.setFrom(files(sourceDirs))
    classDirectories.setFrom(files(javaClasses + kotlinClasses))
    executionData.setFrom(files(executionDataFiles))

    reports {
        val reportDir = layout.buildDirectory.dir("reports/jacoco/jacocoFullReport")
        xml.required.set(true)
        xml.outputLocation.set(reportDir.get().file("jacocoFullReport.xml"))
        html.required.set(true)
        html.outputLocation.set(reportDir.get().asFile)
    }

    doLast {
        println("✅ Combined coverage report generated at:")
        println("📄 file://${reports.html.outputLocation.get()}/index.html")
    }
}

// ===========================
// Run all test + Report Task
// ===========================
tasks.register("runAllCoverageAndReport") {
    group = "Verification"
    description = "Runs unit + UI tests only where they exist and generates a full Jacoco report"

    val testTaskPaths = mutableListOf<String>()

    rootProject.subprojects.forEach { proj ->
        // Vérifie si le dossier de tests unitaires existe et contient des fichiers
        val hasUnitTests = File(proj.projectDir, "src/test").exists()
        if (hasUnitTests) {
            testTaskPaths.add("${proj.path}:testDebugUnitTest")
        }

        // Vérifie si le dossier de tests d'intégration existe et n'est pas vide
        val androidTestDir = File(proj.projectDir, "src/androidTest")
        val hasAndroidTests = androidTestDir.exists() && androidTestDir.walk().any { it.isFile && (it.extension == "kt" || it.extension == "java") }
        
        if (hasAndroidTests) {
            testTaskPaths.add("${proj.path}:connectedDebugAndroidTest")
        }
    }

    dependsOn(testTaskPaths)
    
    // On utilise finalizedBy pour que le rapport soit généré même si certains tests échouent
    finalizedBy("jacocoFullReport")

    doFirst {
        if (testTaskPaths.isEmpty()) {
            println("⚠️ Aucun test détecté.")
        } else {
            println("🚀 Lancement des tests détectés (Unit + UI) :")
            testTaskPaths.forEach { println(" - $it") }
        }
    }
}

/*
sonar {
    properties {
        property("sonar.projectKey", "rebonnte")
        property("sonar.projectName", "rebonnte")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "${layout.buildDirectory.get().asFile.absolutePath}/reports/jacoco/jacocoFullReport/jacocoFullReport.xml")
        property("sonar.kotlin.binaries", "${layout.buildDirectory.get().asFile.absolutePath}/tmp/kotlin-classes/debug")
        property("sonar.exclusions", jacocoExcludes.joinToString(","))
    }
}
*/
