package com.nexus.backend.importer.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.backend.entity.ImportSource;
import com.nexus.backend.enums.ContractType;
import com.nexus.backend.enums.ImportSourceType;
import com.nexus.backend.enums.WorkMode;
import com.nexus.backend.importer.FeedClient;
import com.nexus.backend.importer.ImportException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * API de Adzuna (https://developer.adzuna.com): junta ofertas de muchos portales, también de España y en castellano.
 * Pide registro: el identificador y la clave van en ADZUNA_APP_ID y ADZUNA_APP_KEY, nunca en la URL guardada de la
 * fuente (que se ve en el panel). La URL es la de una búsqueda sin página, p. ej.
 * {@code https://api.adzuna.com/v1/api/jobs/es/search?category=it-jobs&what_or=junior%20becario}; aquí se le añaden la
 * página y las claves. Se enlaza siempre la oferta en Adzuna, como piden sus condiciones.
 */
@Component
public class AdzunaJobProvider implements JobFeedProvider {

    /** Páginas por ejecución: cada una es una consulta y el plan gratuito tiene un cupo mensual. */
    static final int MAX_PAGES = 2;
    private static final int PAGE_SIZE = 50;

    private static final Pattern REMOTE = Pattern.compile("\\b(remoto|teletrabajo|remote|100% remoto|en remoto)\\b");
    private static final Pattern HYBRID = Pattern.compile("\\b(hibrid[oa]s?|hybrid|semipresencial)\\b");
    private static final Pattern INTERNSHIP = Pattern.compile(
            "\\b(practicas|becari[oa]s?|beca|intern|internship|trainee|practicante)\\b");

    private final FeedClient feedClient;
    private final ObjectMapper objectMapper;
    private final String appId;
    private final String appKey;

    public AdzunaJobProvider(FeedClient feedClient, ObjectMapper objectMapper,
                             @Value("${app.import.adzuna.app-id:}") String appId,
                             @Value("${app.import.adzuna.app-key:}") String appKey) {
        this.feedClient = feedClient;
        this.objectMapper = objectMapper;
        this.appId = appId == null ? "" : appId.trim();
        this.appKey = appKey == null ? "" : appKey.trim();
    }

    @Override
    public boolean supports(ImportSourceType type) {
        return type == ImportSourceType.ADZUNA_JOBS;
    }

    @Override
    public List<ImportedJob> fetch(ImportSource source) {
        if (appId.isEmpty() || appKey.isEmpty()) {
            throw new ImportException("Faltan las claves de Adzuna (ADZUNA_APP_ID y ADZUNA_APP_KEY)");
        }
        List<ImportedJob> jobs = new ArrayList<>();
        for (int page = 1; page <= MAX_PAGES; page++) {
            List<ImportedJob> found = parse(JsonFeeds.read(objectMapper,
                    feedClient.get(pageUrl(source.getUrl(), page, appId, appKey), "application/json")));
            jobs.addAll(found);
            if (found.size() < PAGE_SIZE) {
                break;
            }
        }
        return jobs;
    }

    /** «…/search?x=1» → «…/search/2?x=1&app_id=…&app_key=…&results_per_page=50&sort_by=date». */
    static String pageUrl(String searchUrl, int page, String appId, String appKey) {
        int query = searchUrl.indexOf('?');
        String path = (query < 0 ? searchUrl : searchUrl.substring(0, query)).replaceAll("/+$", "");
        String params = query < 0 ? "" : searchUrl.substring(query + 1);
        StringBuilder url = new StringBuilder(path).append('/').append(page).append('?');
        if (!params.isEmpty()) {
            url.append(params).append('&');
        }
        url.append("app_id=").append(encode(appId)).append("&app_key=").append(encode(appKey));
        if (!params.contains("results_per_page=")) {
            url.append("&results_per_page=").append(PAGE_SIZE);
        }
        if (!params.contains("sort_by=")) {
            url.append("&sort_by=date");
        }
        return url.toString();
    }

    List<ImportedJob> parse(JsonNode root) {
        return JsonFeeds.stream(root.path("results"))
                .filter(job -> JsonFeeds.hasText(job, "id", "title", "redirect_url"))
                .map(AdzunaJobProvider::toImportedJob)
                .toList();
    }

    private static ImportedJob toImportedJob(JsonNode job) {
        String title = JsonFeeds.text(job, "title");
        String description = JsonFeeds.text(job, "description");
        String text = normalize(title + " " + (description == null ? "" : description));
        String company = JsonFeeds.text(job.path("company"), "display_name");
        return new ImportedJob(
                ExternalIds.of("adzuna", JsonFeeds.text(job, "id")),
                title,
                company == null ? "Empresa confidencial" : company,
                description,
                JsonFeeds.text(job.path("location"), "display_name"),
                workMode(text),
                contractType(text, JsonFeeds.text(job, "contract_time"), JsonFeeds.text(job, "contract_type")),
                List.of(),
                JsonFeeds.text(job, "redirect_url"),
                JsonFeeds.dateTime(JsonFeeds.text(job, "created")),
                List.of(),
                // Se pide la categoría «it-jobs»: todo lo que llega es del sector, aunque el título no lo diga.
                "it-jobs".equals(JsonFeeds.text(job.path("category"), "tag")));
    }

    /** Adzuna no da la modalidad: se deduce del texto; sin pistas, presencial. */
    private static WorkMode workMode(String text) {
        if (REMOTE.matcher(text).find()) {
            return WorkMode.REMOTE;
        }
        return HYBRID.matcher(text).find() ? WorkMode.HYBRID : WorkMode.ONSITE;
    }

    private static ContractType contractType(String text, String contractTime, String contractType) {
        if (INTERNSHIP.matcher(text).find()) {
            return ContractType.INTERNSHIP;
        }
        if ("part_time".equals(contractTime)) {
            return ContractType.PART_TIME;
        }
        return "contract".equals(contractType) ? ContractType.TEMPORARY : ContractType.FULL_TIME;
    }

    private static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
