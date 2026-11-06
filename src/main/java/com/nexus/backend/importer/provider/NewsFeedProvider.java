package com.nexus.backend.importer.provider;

import com.nexus.backend.entity.ImportSource;
import com.nexus.backend.enums.ImportSourceType;

import java.util.List;

public interface NewsFeedProvider {

    boolean supports(ImportSourceType type);

    List<ImportedNews> fetch(ImportSource source);
}
