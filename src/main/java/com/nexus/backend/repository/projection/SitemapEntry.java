package com.nexus.backend.repository.projection;

import java.time.LocalDateTime;

public interface SitemapEntry {

    Long getId();

    String getTitle();

    LocalDateTime getLastModified();
}
