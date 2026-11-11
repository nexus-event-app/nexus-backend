package com.nexus.backend.mapper;

import com.nexus.backend.dto.request.ImportSourceRequest;
import com.nexus.backend.dto.response.ImportSourceResponse;
import com.nexus.backend.entity.ImportSource;
import com.nexus.backend.enums.NewsCategory;
import com.nexus.backend.util.ContentLanguage;
import org.springframework.stereotype.Component;

@Component
public class ImportSourceMapper {

    public ImportSource toEntity(ImportSourceRequest request) {
        ImportSource source = new ImportSource();
        updateEntity(source, request);
        return source;
    }

    public void updateEntity(ImportSource source, ImportSourceRequest request) {
        boolean news = request.getType().isNews();
        source.setName(request.getName());
        source.setType(request.getType());
        source.setUrl(request.getUrl());
        source.setNewsCategory(news ? (request.getNewsCategory() != null ? request.getNewsCategory() : NewsCategory.GENERAL) : null);
        source.setAutoPublish(!Boolean.FALSE.equals(request.getAutoPublish()));
        source.setActive(!Boolean.FALSE.equals(request.getActive()));
        source.setRemoteOnly(!news && Boolean.TRUE.equals(request.getRemoteOnly()));
        source.setLanguage(ContentLanguage.orDefault(request.getLanguage()));
        source.setKeywords(request.getKeywords());
        source.setExcludeKeywords(request.getExcludeKeywords());
        source.setUseSourceImages(!Boolean.FALSE.equals(request.getUseSourceImages()));
        int minimum = request.getType().getMinIntervalMinutes();
        source.setIntervalMinutes(request.getIntervalMinutes() == null ? minimum : Math.max(minimum, request.getIntervalMinutes()));
    }

    public ImportSourceResponse toResponse(ImportSource source, boolean running) {
        return ImportSourceResponse.builder()
                .id(source.getId())
                .name(source.getName())
                .type(source.getType())
                .url(source.getUrl())
                .newsCategory(source.getNewsCategory())
                .language(source.getLanguage())
                .autoPublish(source.getAutoPublish())
                .active(source.getActive())
                .remoteOnly(source.getRemoteOnly())
                .useSourceImages(source.getUseSourceImages())
                .keywords(source.getKeywords())
                .excludeKeywords(source.getExcludeKeywords())
                .intervalMinutes(source.getIntervalMinutes())
                .minIntervalMinutes(source.getType().getMinIntervalMinutes())
                .lastRunAt(source.getLastRunAt())
                .lastStatus(source.getLastStatus())
                .lastMessage(source.getLastMessage())
                .lastImportedCount(source.getLastImportedCount())
                .running(running)
                .createdAt(source.getCreatedAt())
                .build();
    }
}
