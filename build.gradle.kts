import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.5.8"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    kotlin("jvm") version "1.8.21"
    kotlin("plugin.spring") version "1.8.21"
}

group = "lofitsky.misc"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:5.0.0")
    implementation("io.github.cdimascio:java-dotenv:5.2.2")
    implementation("org.seleniumhq.selenium:selenium-server:3.141.59")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.bootJar {
    mainClass.set("lofitsky.misc.news_bot.NewsBotApp")
    enabled = true
    exclude("**/application-test.yml")
}

tasks.bootRun {
    enabled = true
    mainClass.set("lofitsky.misc.news_bot.NewsBotApp")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
