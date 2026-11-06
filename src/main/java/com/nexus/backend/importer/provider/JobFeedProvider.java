package com.nexus.backend.importer.provider;

import com.nexus.backend.entity.ImportSource;
import com.nexus.backend.enums.ImportSourceType;

import java.util.List;

public interface JobFeedProvider {

    boolean supports(ImportSourceType type);

    List<ImportedJob> fetch(ImportSource source);
}
