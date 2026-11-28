package br.com.nexthub.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class DotenvLoader {
    private static final String ACTIVE_PROFILE_ENV = "SPRING_PROFILES_ACTIVE";

    private DotenvLoader() {
    }

    public static void load() throws IOException {
        Map<String, String> values = parse(findEnvFile(Path.of("").toAbsolutePath().normalize()));
        apply(values, System.getenv(), System.getProperties());
    }

    static Map<String, String> parse(Path file) throws IOException {
        if (!Files.exists(file)) return Map.of();
        if (!Files.isRegularFile(file)) throw new IOException("The backend .env path is not a file.");

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        Map<String, String> values = new LinkedHashMap<>();
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            if (index == 0 && line.startsWith("\uFEFF")) line = line.substring(1);
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            if (line.startsWith("export ")) line = line.substring("export ".length()).trim();

            int separator = line.indexOf('=');
            if (separator <= 0) throw invalidLine(index + 1);

            String name = line.substring(0, separator).trim();
            if (!name.matches("[A-Za-z_][A-Za-z0-9_]*")) throw invalidLine(index + 1);
            values.put(name, parseValue(line.substring(separator + 1).trim(), index + 1));
        }
        return values;
    }

    static void apply(Map<String, String> values, Map<String, String> environment, Properties systemProperties) {
        values.forEach((name, value) -> {
            if (environment.containsKey(name) || systemProperties.containsKey(name)) return;
            systemProperties.setProperty(name, value);

            if (ACTIVE_PROFILE_ENV.equals(name)
                    && !value.isBlank()
                    && !environment.containsKey("spring.profiles.active")
                    && !systemProperties.containsKey("spring.profiles.active")) {
                systemProperties.setProperty("spring.profiles.active", value);
            }
        });
    }

    private static Path findEnvFile(Path workingDirectory) {
        Path projectRootEnv = workingDirectory.resolve("Back-end").resolve(".env");
        if (Files.isRegularFile(projectRootEnv)) return projectRootEnv;
        if (workingDirectory.getFileName() != null
                && "Back-end".equalsIgnoreCase(workingDirectory.getFileName().toString())) {
            return workingDirectory.resolve(".env");
        }
        return projectRootEnv;
    }

    private static String parseValue(String value, int lineNumber) throws IOException {
        if (value.startsWith("\"") || value.startsWith("'")) {
            char quote = value.charAt(0);
            int closingQuote = value.indexOf(quote, 1);
            if (closingQuote < 1) throw invalidLine(lineNumber);
            String trailing = value.substring(closingQuote + 1).trim();
            if (!trailing.isEmpty() && !trailing.startsWith("#")) throw invalidLine(lineNumber);
            return value.substring(1, closingQuote);
        }

        int inlineComment = value.indexOf(" #");
        return inlineComment < 0 ? value : value.substring(0, inlineComment).stripTrailing();
    }

    private static IOException invalidLine(int lineNumber) {
        return new IOException("Invalid .env syntax at line " + lineNumber + ".");
    }
}
