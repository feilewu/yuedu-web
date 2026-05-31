plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "io.legado"
version = "1.0.0"

dependencies {
    implementation(libs.jsoup)
    implementation("xmlpull:xmlpull:1.1.3.1")
}
