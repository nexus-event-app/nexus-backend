package com.nexus.backend.service.impl;

import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.dto.response.SearchResponse;
import com.nexus.backend.dto.response.SearchResponse.Group;
import com.nexus.backend.dto.response.SearchResponse.Person;
import com.nexus.backend.entity.User;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.repository.spec.SpecificationUtils;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.DiscussionService;
import com.nexus.backend.service.JobService;
import com.nexus.backend.service.NewsService;
import com.nexus.backend.service.SearchService;
import com.nexus.backend.util.SlugUtil;
import com.nexus.backend.util.TextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Búsqueda en toda la web. Reutiliza los filtros públicos de cada sección, así que encuentra lo mismo que buscar
 * dentro de ella (y nada que no sea público).
 */
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final NewsService newsService;
    private final JobService jobService;
    private final DiscussionService discussionService;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public SearchResponse search(String query, int limit, UserDetail viewer) {
        String text = normalize(query);
        if (text.length() < MIN_QUERY_LENGTH) {
            return new SearchResponse(text, empty(), empty(), empty(), empty());
        }
        int size = Math.min(Math.max(limit, 1), MAX_LIMIT);
        return new SearchResponse(
                text,
                Group.of(newsService.getPublicNews(null, text, 0, size)),
                Group.of(jobService.getPublicJobs(null, null, null, text, 0, size)),
                Group.of(discussionService.getDiscussions(null, text, 0, size, viewer)),
                people(text, size));
    }

    /** Perfiles de cuentas activas cuyo nombre, titular o tecnologías contienen el texto. */
    private Group<Person> people(String text, int size) {
        Specification<User> spec = SpecificationUtils.<User>equalTo("enabled", true)
                .and(SpecificationUtils.textContains(text, "name", "headline", "technologies"));
        Page<User> page = userRepository.findAll(spec, PageRequest.of(0, size, Sort.by("name", "id")));
        List<Person> items = page.getContent().stream()
                .map(user -> new Person(user.getId(), SlugUtil.slugify(user.getName()), user.getName(),
                        user.getHeadline(), user.getAvatarUrl()))
                .toList();
        return new Group<>(page.getTotalElements(), items);
    }

    private static String normalize(String query) {
        String text = TextUtil.optionalLine(query);
        if (text == null) {
            return "";
        }
        return text.length() > MAX_QUERY_LENGTH ? text.substring(0, MAX_QUERY_LENGTH) : text;
    }

    private static <T> Group<T> empty() {
        return Group.of(PageResponse.<T>builder().content(List.of()).totalElements(0).build());
    }
}
