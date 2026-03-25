plugins {
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(project(":common-security"))
    implementation(project(":common-tracking"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.kafka)

    runtimeOnly(libs.postgresql)

    testImplementation(libs.spring.security.test)
    testImplementation(libs.spring.kafka.test)
}
