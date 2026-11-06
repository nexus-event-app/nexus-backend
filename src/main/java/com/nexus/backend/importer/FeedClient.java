package com.nexus.backend.importer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Cliente HTTP para fuentes externas: tiempos de espera acotados, tamaño máximo de respuesta y
 * redirecciones seguidas a mano para validar cada salto con {@link UrlGuard}.
 */
@Component
public class FeedClient {

    private static final int MAX_BYTES = 8 * 1024 * 1024;
    private static final int MAX_REDIRECTS = 5;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(25);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private final String userAgent;

    public FeedClient(@Value("${app.public-url:http://localhost:5173}") String publicUrl) {
        this.userAgent = "PrimerCommitBot/1.0 (+" + publicUrl + ")";
    }

    public byte[] get(String url, String accept) {
        URI uri = UrlGuard.validate(url);
        for (int hop = 0; hop <= MAX_REDIRECTS; hop++) {
            HttpResponse<InputStream> response = send(uri, accept);
            int status = response.statusCode();
            if (status >= 300 && status < 400) {
                String location = response.headers().firstValue("Location")
                        .orElseThrow(() -> new ImportException("La fuente redirige sin indicar adónde"));
                closeQuietly(response.body());
                uri = UrlGuard.validate(uri.resolve(location).toString());
                continue;
            }
            if (status != 200) {
                closeQuietly(response.body());
                throw new ImportException("La fuente respondió con el estado HTTP " + status);
            }
            return readLimited(response.body());
        }
        throw new ImportException("La fuente tiene demasiadas redirecciones");
    }

    private HttpResponse<InputStream> send(URI uri, String accept) {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(REQUEST_TIMEOUT)
                .header("User-Agent", userAgent)
                .header("Accept", accept)
                .GET()
                .build();
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (IOException e) {
            throw new ImportException("No se pudo conectar con la fuente", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ImportException("La conexión con la fuente se interrumpió", e);
        }
    }

    private static byte[] readLimited(InputStream body) {
        try (InputStream in = body) {
            byte[] bytes = in.readNBytes(MAX_BYTES + 1);
            if (bytes.length > MAX_BYTES) {
                throw new ImportException("La respuesta de la fuente es demasiado grande");
            }
            return bytes;
        } catch (IOException e) {
            throw new ImportException("No se pudo leer la respuesta de la fuente", e);
        }
    }

    private static void closeQuietly(InputStream body) {
        try {
            body.close();
        } catch (IOException ignored) {
            // nada que hacer
        }
    }
}
