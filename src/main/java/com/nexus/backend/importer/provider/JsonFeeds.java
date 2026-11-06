package com.nexus.backend.importer.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.backend.importer.ImportException;
import com.nexus.backend.util.TextUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** Utilidades para leer APIs JSON de empleo de forma tolerante a campos ausentes. */
final class JsonFeeds {

    private JsonFeeds() {
    }

    static JsonNode read(ObjectMapper mapper, byte[] body) {
        try {
            return mapper.readTree(body);
        } catch (IOException e) {
            throw new ImportException("La fuente no devolvió un JSON válido", e);
        }
    }

    static Stream<JsonNode> stream(JsonNode array) {
        if (array == null || !array.isArray()) {
            throw new ImportException("La respuesta de la fuente no tiene el formato esperado");
        }
        return StreamSupport.stream(array.spliterator(), false);
    }

    static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isValueNode() ? TextUtil.optionalLine(value.asText()) : null;
    }

    static boolean hasText(JsonNode node, String... fields) {
        for (String field : fields) {
            if (text(node, field) == null) {
                return false;
            }
        }
        return true;
    }

    static List<String> textList(JsonNode array) {
        if (array == null || !array.isArray()) {
            return List.of();
        }
        return StreamSupport.stream(array.spliterator(), false)
                .map(JsonNode::asText)
                .map(TextUtil::optionalLine)
                .filter(value -> value != null)
                .toList();
    }

    /** Acepta fechas ISO con o sin zona horaria. */
    static LocalDateTime dateTime(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
            try {
                return OffsetDateTime.parse(value).toLocalDateTime();
            } catch (DateTimeParseException e) {
                return null;
            }
        }
    }
}
