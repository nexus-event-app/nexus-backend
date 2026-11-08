package com.nexus.backend.i18n;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/** Los tres idiomas deben tener las mismas claves, ninguna vacía y los mismos argumentos {0}, {1}… */
class MessageBundlesTest {

    private static final Pattern ARGUMENT = Pattern.compile("\\{(\\d+)}");

    @Test
    void catalanAndEnglishHaveTheSameKeysAsSpanish() throws IOException {
        Properties spanish = load("messages.properties");

        for (String file : new String[]{"messages_ca.properties", "messages_en.properties"}) {
            Properties other = load(file);
            assertThat(keys(other)).as("claves de " + file).isEqualTo(keys(spanish));
        }
    }

    @Test
    void noMessageIsEmptyAndArgumentsMatch() throws IOException {
        Properties spanish = load("messages.properties");

        for (String file : new String[]{"messages.properties", "messages_ca.properties", "messages_en.properties"}) {
            Properties bundle = load(file);
            for (String key : bundle.stringPropertyNames()) {
                String value = bundle.getProperty(key);
                assertThat(value).as(file + " → " + key).isNotBlank();
                assertThat(arguments(value)).as(file + " → argumentos de " + key).isEqualTo(arguments(spanish.getProperty(key)));
            }
        }
    }

    private static Properties load(String name) throws IOException {
        Properties properties = new Properties();
        try (InputStreamReader reader = new InputStreamReader(new ClassPathResource(name).getInputStream(), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }

    private static Set<String> keys(Properties properties) {
        return new TreeSet<>(properties.stringPropertyNames());
    }

    private static Set<String> arguments(String message) {
        Set<String> found = new TreeSet<>();
        Matcher matcher = ARGUMENT.matcher(message);
        while (matcher.find()) {
            found.add(matcher.group(1));
        }
        return found;
    }
}
