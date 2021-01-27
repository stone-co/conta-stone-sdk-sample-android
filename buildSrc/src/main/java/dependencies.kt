object Versions {
    const val contaStoneSdk = "3.0.87"

    const val kotlin = "1.3.72"
    const val androidx = "1.3.2"
    const val material = "1.2.1"
    const val constraint = "2.0.4"

    const val jUnit = "4.13.1"

    const val androidjUnit = "1.1.2"
    const val espresso = "3.3.0"

    const val gradle = "4.1.0"
    const val groupie = "2.8.0"
    const val firebase = "17.3.0"
}

object Dependencies {

    val contaStoneSdk = "co.stone:conta:${Versions.contaStoneSdk}"
    val cactusSystem = "co.stone:cactus-system:${Versions.contaStoneSdk}"

    val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"

    val androidx = "androidx.core:core-ktx:${Versions.androidx}"
    val appCompat = "androidx.appcompat:appcompat:1.2.0"
    val material = "com.google.android.material:material:${Versions.material}"
    val constraint = "androidx.constraintlayout:constraintlayout:${Versions.constraint}"

    val jUnit = "junit:junit:${Versions.jUnit}"
    val androidjUnit = "androidx.test.ext:junit:${Versions.androidjUnit}"
    val espresso = "androidx.test.espresso:espresso-core:${Versions.espresso}"

    val groupie = "com.xwray:groupie:${Versions.groupie}"
    val firebase = "com.google.firebase:firebase-crashlytics:${Versions.firebase}"
}

object BuildPlugins {
    val gradle = "com.android.tools.build:gradle:${Versions.gradle}"
    val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
}

object AndroidModule {

    val main = listOf(
        Dependencies.kotlinStdlib,
        Dependencies.androidx,
        Dependencies.appCompat,
        Dependencies.material,
        Dependencies.constraint
    )

    val utilities = listOf(
        Dependencies.groupie,
        Dependencies.firebase
    )

    val unitTesting = listOf(Dependencies.jUnit)

    val androidTesting = listOf(
        Dependencies.androidjUnit,
        Dependencies.espresso
    )
}