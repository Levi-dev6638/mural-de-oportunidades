package br.com.nexthub.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DotenvLoaderTests {
    @TempDir
    Path tempDir;

    @Test
    void parsesCommentsBlankLinesAndQuotedValues() throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, """
                # comment

                DATABASE_URL="jdbc:postgresql://localhost/app?sslmode=require"
                DATABASE_PASSWORD='a=value # kept'
                NEXTHUB_SEED_ENABLED=false # inline comment
                export SUPABASE_STORAGE_BUCKET=nexthub-resumes
                """);

        Map<String, String> values = DotenvLoader.parse(envFile);

        assertThat(values)
                .containsEntry("DATABASE_URL", "jdbc:postgresql://localhost/app?sslmode=require")
                .containsEntry("DATABASE_PASSWORD", "a=value # kept")
                .containsEntry("NEXTHUB_SEED_ENABLED", "false")
                .containsEntry("SUPABASE_STORAGE_BUCKET", "nexthub-resumes");
    }

    @Test
    void preservesEnvironmentAndSystemPropertyValues() {
        Properties systemProperties = new Properties();
        systemProperties.setProperty("DATABASE_URL", "system-url");

        DotenvLoader.apply(
                Map.of(
                        "DATABASE_URL", "dotenv-url",
                        "DATABASE_PASSWORD", "dotenv-password",
                        "NEXTHUB_SEED_ENABLED", "false"),
                Map.of("DATABASE_PASSWORD", "render-password"),
                systemProperties);

        assertThat(systemProperties)
                .containsEntry("DATABASE_URL", "system-url")
                .doesNotContainKey("DATABASE_PASSWORD")
                .containsEntry("NEXTHUB_SEED_ENABLED", "false");
    }

    @Test
    void makesDotenvActiveProfileAvailableToSpringWithoutForcingItWhenEmpty() {
        Properties systemProperties = new Properties();
        DotenvLoader.apply(
                Map.of("SPRING_PROFILES_ACTIVE", "prod"),
                Map.of(),
                systemProperties);

        assertThat(systemProperties).containsEntry("spring.profiles.active", "prod");

        Properties emptyProfileProperties = new Properties();
        DotenvLoader.apply(
                Map.of("SPRING_PROFILES_ACTIVE", ""),
                Map.of(),
                emptyProfileProperties);

        assertThat(emptyProfileProperties).doesNotContainKey("spring.profiles.active");
    }

    @Test
    void missingEnvFileIsOptionalAndInvalidLinesDoNotExposeTheirContents() throws IOException {
        assertThat(DotenvLoader.parse(tempDir.resolve("missing.env"))).isEmpty();

        Path invalidFile = tempDir.resolve("invalid.env");
        Files.writeString(invalidFile,
                "DATABASE_PASSWORD=do-not-log-this-secret\nnot a valid assignment\n");

        assertThatThrownBy(() -> DotenvLoader.parse(invalidFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("line 2")
                .hasMessageNotContaining("do-not-log-this-secret");
    }
}
