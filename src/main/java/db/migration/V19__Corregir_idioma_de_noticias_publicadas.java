package db.migration;

import com.nexus.backend.util.LanguageDetector;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Corrige el idioma de las noticias importadas que se publicaron desde el panel: «Publicar» enviaba la noticia sin
 * idioma y el servidor la pasaba a castellano, así que noticias en inglés salían con el filtro «Español» y en la
 * portada. Se vuelve a detectar por el texto y, si el texto no lo deja claro, se usa el idioma de su fuente.
 */
public class V19__Corregir_idioma_de_noticias_publicadas extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws SQLException {
        Connection connection = context.getConnection();
        try (Statement select = connection.createStatement();
             ResultSet rows = select.executeQuery("""
                     select n.id, n.title, n.content, n.language, s.language as source_language
                       from news n
                       left join import_sources s on s.name = n.source
                      where n.external_id is not null""");
             PreparedStatement update = connection.prepareStatement("update news set language = ? where id = ?")) {
            while (rows.next()) {
                String current = rows.getString("language");
                String sourceLanguage = rows.getString("source_language");
                String detected = LanguageDetector.detectOr(rows.getString("title") + ". " + rows.getString("content"),
                        sourceLanguage != null ? sourceLanguage : current);
                if (!detected.equals(current)) {
                    update.setString(1, detected);
                    update.setLong(2, rows.getLong("id"));
                    update.addBatch();
                }
            }
            update.executeBatch();
        }
    }
}
