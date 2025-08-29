/**
 * Shared Gradle script for configuring ProGuard mapping file management.
 * This script creates tasks to automatically save mapping files, configuration, and metadata
 * after successful release builds.
 * 
 * Usage: 
 * In module build.gradle.kts:
 * apply(from = "$rootDir/gradle/mapping-tasks.gradle.kts")
 * extra["moduleName"] = "androidNode" // or "androidClient"
 */

// Get the module name from extra properties
val moduleName = project.extra["moduleName"] as? String
    ?: throw GradleException("Module name must be set via extra['moduleName'] before applying mapping-tasks.gradle.kts")

// Validate module name
require(moduleName.isNotBlank()) { "Module name cannot be blank" }

// Task to save ProGuard mapping files after successful release builds
tasks.register("saveMappingFiles") {
    group = "bisq"
    description = "Save ProGuard mapping files to mappings/$moduleName directory"

    // Configuration cache compatible - capture values at configuration time
    val mappingsDirPath = "${rootProject.projectDir}/mappings/$moduleName"
    val buildOutputsDirPath = "${layout.buildDirectory.get().asFile}/outputs"
    val moduleNameForLogging = moduleName

    doLast {
        val mappingsDir = File(mappingsDirPath)
        val buildOutputsDir = File(buildOutputsDirPath)

        // Create mappings directory if it doesn't exist
        mappingsDir.mkdirs()

        // Copy configuration.txt
        val configFile = File(buildOutputsDir, "mapping/release/configuration.txt")
        if (configFile.exists()) {
            configFile.copyTo(File(mappingsDir, "configuration.txt"), overwrite = true)
            println("[$moduleNameForLogging] Saved configuration.txt to $mappingsDir")
        } else {
            println("[$moduleNameForLogging] configuration.txt not found at ${configFile.absolutePath}")
        }

        // Compress and copy mapping.txt as tar.gz
        val mappingFile = File(buildOutputsDir, "mapping/release/mapping.txt")
        if (mappingFile.exists()) {
            val tarGzFile = File(mappingsDir, "mapping.txt.tar.gz")

            // Use tar command to create compressed archive
            val tarCommand = listOf(
                "tar", "-czvf",
                tarGzFile.absolutePath,
                "-C", mappingFile.parent,
                mappingFile.name
            )

            val process = ProcessBuilder(tarCommand)
                .redirectErrorStream(true)
                .start()

            val exitCode = process.waitFor()
            if (exitCode == 0) {
                println("[$moduleNameForLogging] Saved mapping.txt.tar.gz to $mappingsDir")
            } else {
                val output = process.inputStream.bufferedReader().readText()
                throw GradleException("[$moduleNameForLogging] Failed to create mapping.txt.tar.gz: $output")
            }
        } else {
            println("[$moduleNameForLogging] mapping.txt not found at ${mappingFile.absolutePath}")
        }
    }
}

// Task to save output metadata after assembleRelease
tasks.register("saveOutputMetadata") {
    group = "bisq"
    description = "Save output-metadata.json to mappings/$moduleName directory"

    // Configuration cache compatible - capture values at configuration time
    val mappingsDirPath = "${rootProject.projectDir}/mappings/$moduleName"
    val buildOutputsDirPath = "${layout.buildDirectory.get().asFile}/outputs"
    val moduleNameForLogging = moduleName

    doLast {
        val mappingsDir = File(mappingsDirPath)
        val buildOutputsDir = File(buildOutputsDirPath)

        // Create mappings directory if it doesn't exist
        mappingsDir.mkdirs()

        // Copy output-metadata.json
        val metadataFile = File(buildOutputsDir, "apk/release/output-metadata.json")
        if (metadataFile.exists()) {
            metadataFile.copyTo(File(mappingsDir, "output-metadata.json"), overwrite = true)
            println("[$moduleNameForLogging] Saved output-metadata.json to $mappingsDir")
        } else {
            println("[$moduleNameForLogging] output-metadata.json not found at ${metadataFile.absolutePath}")
        }
    }
}

// Hook the tasks to run after successful release builds
afterEvaluate {
    tasks.findByName("bundleRelease")?.let { task ->
        task.finalizedBy("saveMappingFiles")
    }
    
    tasks.findByName("assembleRelease")?.let { task ->
        task.finalizedBy("saveOutputMetadata")
    }
}
