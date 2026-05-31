plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

tasks.register("buildFrontend") {
    group = "build"
    description = "Builds the Vue frontend"
    doLast {
        val frontendDir = rootProject.projectDir.resolve("frontend")
        if (frontendDir.resolve("node_modules").exists()) {
            exec {
                workingDir = frontendDir
                commandLine("pnpm", "build")
            }
        } else {
            println("frontend/node_modules not found, run 'cd frontend && pnpm install' first")
        }
    }
}
