plugins {
    java
    id("org.springframework.boot") version "3.3.6" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

group = "com.dmytrozah"
version = "0.0.1-SNAPSHOT"
description = "PS_Task_2"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}


subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {

}

tasks.withType<Test> {
    useJUnitPlatform()
}
