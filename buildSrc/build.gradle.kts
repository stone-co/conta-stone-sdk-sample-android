import org.gradle.kotlin.dsl.`kotlin-dsl`

plugins {
    @Suppress("RemoveRedundantBackticks")
    `kotlin-dsl`
}

repositories {
    jcenter()
    google()
}

dependencies {
    // Dependency below can't be BuildPlugins.gradle because it is executed
    // before the BuildPlugins class be recognized in the classpath
    implementation("com.android.tools.build:gradle:3.5.3")
}
