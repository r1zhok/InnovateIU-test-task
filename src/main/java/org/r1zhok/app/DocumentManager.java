package org.r1zhok.app;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final List<Document> documents;

    public DocumentManager() {
        this.documents = new ArrayList<>();
    }

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.getId() == null) {
            document.setId(UUID.randomUUID().toString());
        } else {
            for (int i = 0; i < documents.size(); i++) {
                if (documents.get(i).getId().equals(document.getId())) {
                    return documents.set(i, document);
                }
            }
        }
        documents.add(document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        if (request == null || documents.isEmpty()) {
            return Collections.emptyList();
        }

        return documents.stream()
                .filter(doc -> titlePrefixesFilter(doc, request.getTitlePrefixes()))
                .filter(doc -> containsContentsFilter(doc, request.getContainsContents()))
                .filter(doc -> authorsIdsFilter(doc, request.getAuthorIds()))
                .filter(doc -> createdFromFilter(doc, request.getCreatedFrom()))
                .filter(doc -> createdToFilter(doc, request.getCreatedTo()))
                .collect(Collectors.toList());
    }

    private boolean titlePrefixesFilter(Document doc, List<String> titlePrefixes) {
        if (titlePrefixes == null || titlePrefixes.isEmpty()) {
            return true;
        }

        return titlePrefixes.stream()
                .anyMatch(prefix -> doc.getTitle() != null && doc.getTitle().startsWith(prefix));
    }

    private boolean containsContentsFilter(Document doc, List<String> containsContents) {
        if (containsContents == null || containsContents.isEmpty()) {
            return true;
        }

        return containsContents.stream()
                .anyMatch(content -> doc.getContent() != null && doc.getContent().contains(content));
    }

    private boolean authorsIdsFilter(Document doc, List<String> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            return true;
        } else if (doc.getAuthor().getId() == null || doc.getAuthor().getId().isEmpty()) {
            return false;
        }

        return authorIds.contains(doc.getAuthor().getId());
    }

    private boolean createdFromFilter(Document doc, Instant createdFrom) {
        if (createdFrom == null) {
            return true;
        }
        return doc.getCreated() != null && !doc.getCreated().isBefore(createdFrom);
    }

    private boolean createdToFilter(Document doc, Instant createdTo) {
        if (createdTo == null) {
            return true;
        }
        return doc.getCreated() != null && !doc.getCreated().isAfter(createdTo);
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        if (id == null || id.isEmpty()) {return Optional.empty();}
        return documents.stream().filter(doc -> doc.getId().equals(id)).findFirst();
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}