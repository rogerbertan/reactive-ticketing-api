plugins {
	kotlin("jvm") version "2.3.21"
	kotlin("plugin.spring") version "2.3.21"
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
	id("info.solidsoft.pitest") version "1.19.0"
	jacoco
}

group = "dev.rogerbertan"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.springframework:spring-jdbc")
	implementation("tools.jackson.module:jackson-module-kotlin")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("org.postgresql:r2dbc-postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-data-r2dbc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testImplementation("io.mockk:mockk:1.14.11")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers-postgresql")
	testImplementation("org.testcontainers:testcontainers-r2dbc")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

jacoco {
	toolVersion = "0.8.15"
}

val coverageExclusions = listOf(
	"**/dto/**",
	"**/command/**",
	"**/exception/**",
	"**/port/**",
	"**/*MapperKt.class",
	"**/*Entity.class",
	"**/*Type.class",
	"**/application/domain/Event.class",
	"**/application/domain/Sale.class",
	"**/ReactiveTicketingApiApplication*",
	"**/*\$\$inlined\$*",
)

tasks.test {
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required = true
		html.required = true
	}
	classDirectories.setFrom(
		sourceSets.main.get().output.classesDirs.map { fileTree(it) { exclude(coverageExclusions) } }
	)
}

pitest {
	pitestVersion = "1.30.0"
	junit5PluginVersion = "1.2.3"
	targetClasses = listOf(
		"dev.rogerbertan.reactive_ticketing_api.*.application.domain.*",
		"dev.rogerbertan.reactive_ticketing_api.*.application.service.*",
		"dev.rogerbertan.reactive_ticketing_api.ticket.adapter.in.http.TicketController*",
	)
	excludedClasses = listOf(
		"dev.rogerbertan.reactive_ticketing_api.*.application.domain.exception.*",
		"dev.rogerbertan.reactive_ticketing_api.*.application.domain.Event",
		"dev.rogerbertan.reactive_ticketing_api.*.application.domain.Sale",
		"dev.rogerbertan.reactive_ticketing_api.*.application.domain.*Type",
		// bytecode copied from kotlinx flow inline operators (map/filter), not project logic
		"dev.rogerbertan.reactive_ticketing_api.*\$\$inlined\$*",
	)
	targetTests = listOf(
		"dev.rogerbertan.reactive_ticketing_api.*.application.*Test",
		"dev.rogerbertan.reactive_ticketing_api.*.adapter.*Test",
	)
	avoidCallsTo = setOf("kotlin.jvm.internal")
	threads = 4
	outputFormats = setOf("HTML", "XML")
	timestampedReports = false
	mutationThreshold = 80
}
