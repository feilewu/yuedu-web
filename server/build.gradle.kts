plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.sam)
    application
}

group = "io.legado"
version = "1.0.0"

dependencies {
    implementation(project(":modules:rhino"))
    implementation(project(":modules:book"))

    implementation(libs.kotlin.stdlib)
    implementation(libs.bundles.coroutines)
    implementation(libs.okhttp)
    implementation(libs.jsoup)
    implementation(libs.gson)
    implementation(libs.json.path)
    implementation(libs.jsoupxpath)
    implementation(libs.nanohttpd)
    implementation(libs.nanohttpd.websocket)
    implementation(libs.commons.text)
    implementation(libs.hutool.crypto)
    implementation(libs.sqlite.jdbc)
    implementation(libs.logback.classic)
    implementation(libs.slf4j.api)
}

application {
    mainClass = "io.legado.server.AppKt"
    applicationDefaultJvmArgs = listOf(
        "--add-opens", "java.base/java.time=ALL-UNNAMED"
    )
}

tasks.jar {
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.exists() }.map { zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "io.legado.server.AppKt"
    }
}
