plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "io.legado"
version = "1.0.0"

dependencies {
    api(libs.mozilla.rhino)
    implementation(libs.bundles.coroutines)
    implementation(libs.okhttp)
}
