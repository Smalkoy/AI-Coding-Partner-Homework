package com.support.ticketsystem.service;

import com.support.ticketsystem.domain.dto.request.TicketCreateRequest;
import com.support.ticketsystem.domain.dto.request.TicketUpdateRequest;
import com.support.ticketsystem.domain.dto.response.TicketResponse;
import com.support.ticketsystem.domain.entity.Ticket;
import com.support.ticketsystem.domain.enums.Category;
import com.support.ticketsystem.domain.enums.Priority;
import com.support.ticketsystem.domain.enums.Status;
import com.support.ticketsystem.exception.TicketNotFoundException;
import com.support.ticketsystem.mapper.TicketMapper;
import com.support.ticketsystem.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TicketService Tests")
class TicketServiceTest {

    private TicketRepository ticketRepository;
    private ClassificationService classificationService;
    private TicketMapper ticketMapper = new TicketMapper();
    private TicketService ticketService;

    private static final UUID TEST_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    // In-memory storage for fake repository
    private Map<UUID, Ticket> ticketStore;

    @BeforeEach
    void setUp() {
        ticketStore = new HashMap<>();
        ticketRepository = createFakeTicketRepository();
        classificationService = createTestClassificationService();
        ticketService = new TicketService(ticketRepository, ticketMapper, classificationService);
    }

    @Test
    @DisplayName("Create ticket successfully")
    void testCreateTicket_Success() {
        TicketCreateRequest request = createValidRequest();

        TicketResponse result = ticketService.createTicket(request);

        assertThat(result).isNotNull();
        assertThat(result.customerId()).isEqualTo("CUST001");
        assertThat(ticketStore).hasSize(1);
    }

    @Test
    @DisplayName("Create ticket with auto-classify")
    void testCreateTicket_WithAutoClassify() {
        TicketCreateRequest request = new TicketCreateRequest(
            "CUST001", "test@example.com", "Test User",
            "Cannot login", "I am unable to login to my account",
            null, null, null, null, null, null
        );

        TicketResponse result = ticketService.createTicket(request, true);

        assertThat(result).isNotNull();
        assertThat(result.category()).isEqualTo(Category.ACCOUNT_ACCESS);
        assertThat(result.priority()).isEqualTo(Priority.HIGH);
    }

    @Test
    @DisplayName("Get ticket by ID - found")
    void testGetTicket_Found() {
        Ticket ticket = createRealTicketEntity();
        setTicketId(ticket, TEST_ID);
        ticketStore.put(TEST_ID, ticket);

        TicketResponse result = ticketService.getTicket(TEST_ID);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(TEST_ID);
    }

    @Test
    @DisplayName("Get ticket by ID - not found")
    void testGetTicket_NotFound() {
        assertThatThrownBy(() -> ticketService.getTicket(TEST_ID))
            .isInstanceOf(TicketNotFoundException.class)
            .hasMessageContaining(TEST_ID.toString());
    }

    @Test
    @DisplayName("Get all tickets without filters")
    void testGetAllTickets_NoFilters() {
        Ticket ticket = createRealTicketEntity();
        setTicketId(ticket, TEST_ID);
        ticketStore.put(TEST_ID, ticket);

        List<TicketResponse> result = ticketService.getAllTickets(null, null, null, null);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Get all tickets with filters")
    void testGetAllTickets_WithFilters() {
        Ticket ticket = createRealTicketEntity();
        ticket.setStatus(Status.NEW);
        ticket.setCategory(Category.BUG_REPORT);
        ticket.setPriority(Priority.HIGH);
        setTicketId(ticket, TEST_ID);
        ticketStore.put(TEST_ID, ticket);

        List<TicketResponse> result = ticketService.getAllTickets(
            Status.NEW, Category.BUG_REPORT, Priority.HIGH, null
        );

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Update ticket successfully")
    void testUpdateTicket_Success() {
        Ticket ticket = createRealTicketEntity();
        setTicketId(ticket, TEST_ID);
        ticketStore.put(TEST_ID, ticket);
        TicketUpdateRequest request = new TicketUpdateRequest(
            null, null, null, Priority.HIGH, Status.IN_PROGRESS, null, null, null
        );

        TicketResponse result = ticketService.updateTicket(TEST_ID, request);

        assertThat(result).isNotNull();
        assertThat(ticketStore.get(TEST_ID).getPriority()).isEqualTo(Priority.HIGH);
        assertThat(ticketStore.get(TEST_ID).getStatus()).isEqualTo(Status.IN_PROGRESS);
    }

    @Test
    @DisplayName("Delete ticket successfully")
    void testDeleteTicket_Success() {
        Ticket ticket = createRealTicketEntity();
        setTicketId(ticket, TEST_ID);
        ticketStore.put(TEST_ID, ticket);

        ticketService.deleteTicket(TEST_ID);

        assertThat(ticketStore).isEmpty();
    }

    @Test
    @DisplayName("Delete ticket - not found")
    void testDeleteTicket_NotFound() {
        assertThatThrownBy(() -> ticketService.deleteTicket(TEST_ID))
            .isInstanceOf(TicketNotFoundException.class);
    }

    private TicketCreateRequest createValidRequest() {
        return new TicketCreateRequest(
            "CUST001", "test@example.com", "Test User",
            "Test Subject", "This is a valid description for testing.",
            Category.TECHNICAL_ISSUE, Priority.MEDIUM, Status.NEW,
            null, List.of("test"), null
        );
    }

    private Ticket createRealTicketEntity() {
        return Ticket.builder()
            .customerId("CUST001")
            .customerEmail("test@example.com")
            .customerName("Test User")
            .subject("Test Subject")
            .description("This is a valid description for testing.")
            .category(Category.TECHNICAL_ISSUE)
            .priority(Priority.MEDIUM)
            .status(Status.NEW)
            .tags(new ArrayList<>())
            .build();
    }

    private void setTicketId(Ticket ticket, UUID id) {
        try {
            var field = Ticket.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(ticket, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ticket ID", e);
        }
    }

    /**
     * Creates a fake TicketRepository implementation for testing.
     */
    private TicketRepository createFakeTicketRepository() {
        return new TicketRepository() {
            @Override
            public List<Ticket> findByStatus(Status status) {
                return ticketStore.values().stream()
                    .filter(t -> t.getStatus() == status)
                    .toList();
            }

            @Override
            public List<Ticket> findByCategory(Category category) {
                return ticketStore.values().stream()
                    .filter(t -> t.getCategory() == category)
                    .toList();
            }

            @Override
            public List<Ticket> findByPriority(Priority priority) {
                return ticketStore.values().stream()
                    .filter(t -> t.getPriority() == priority)
                    .toList();
            }

            @Override
            public List<Ticket> findByCustomerId(String customerId) {
                return ticketStore.values().stream()
                    .filter(t -> customerId.equals(t.getCustomerId()))
                    .toList();
            }

            @Override
            public List<Ticket> findByStatusAndCategory(Status status, Category category) {
                return ticketStore.values().stream()
                    .filter(t -> t.getStatus() == status && t.getCategory() == category)
                    .toList();
            }

            @Override
            public List<Ticket> findByStatusAndPriority(Status status, Priority priority) {
                return ticketStore.values().stream()
                    .filter(t -> t.getStatus() == status && t.getPriority() == priority)
                    .toList();
            }

            @Override
            public List<Ticket> findByCategoryAndPriority(Category category, Priority priority) {
                return ticketStore.values().stream()
                    .filter(t -> t.getCategory() == category && t.getPriority() == priority)
                    .toList();
            }

            @Override
            public List<Ticket> findByFilters(Status status, Category category, Priority priority, String customerId) {
                return ticketStore.values().stream()
                    .filter(t -> (status == null || t.getStatus() == status))
                    .filter(t -> (category == null || t.getCategory() == category))
                    .filter(t -> (priority == null || t.getPriority() == priority))
                    .filter(t -> (customerId == null || customerId.equals(t.getCustomerId())))
                    .toList();
            }

            @Override
            public long countByStatus(Status status) {
                return ticketStore.values().stream().filter(t -> t.getStatus() == status).count();
            }

            @Override
            public long countByCategory(Category category) {
                return ticketStore.values().stream().filter(t -> t.getCategory() == category).count();
            }

            @Override
            public long countByPriority(Priority priority) {
                return ticketStore.values().stream().filter(t -> t.getPriority() == priority).count();
            }

            @Override
            public <S extends Ticket> S save(S entity) {
                if (entity.getId() == null) {
                    setTicketId(entity, UUID.randomUUID());
                }
                ticketStore.put(entity.getId(), entity);
                return entity;
            }

            @Override
            public <S extends Ticket> List<S> saveAll(Iterable<S> entities) {
                List<S> result = new ArrayList<>();
                entities.forEach(e -> result.add(save(e)));
                return result;
            }

            @Override
            public Optional<Ticket> findById(UUID uuid) {
                return Optional.ofNullable(ticketStore.get(uuid));
            }

            @Override
            public boolean existsById(UUID uuid) {
                return ticketStore.containsKey(uuid);
            }

            @Override
            public List<Ticket> findAll() {
                return new ArrayList<>(ticketStore.values());
            }

            @Override
            public List<Ticket> findAllById(Iterable<UUID> uuids) {
                List<Ticket> result = new ArrayList<>();
                uuids.forEach(id -> {
                    Ticket t = ticketStore.get(id);
                    if (t != null) result.add(t);
                });
                return result;
            }

            @Override
            public long count() {
                return ticketStore.size();
            }

            @Override
            public void deleteById(UUID uuid) {
                ticketStore.remove(uuid);
            }

            @Override
            public void delete(Ticket entity) {
                ticketStore.remove(entity.getId());
            }

            @Override
            public void deleteAllById(Iterable<? extends UUID> uuids) {
                uuids.forEach(ticketStore::remove);
            }

            @Override
            public void deleteAll(Iterable<? extends Ticket> entities) {
                entities.forEach(e -> ticketStore.remove(e.getId()));
            }

            @Override
            public void deleteAll() {
                ticketStore.clear();
            }

            @Override
            public void flush() {}

            @Override
            public <S extends Ticket> S saveAndFlush(S entity) {
                return save(entity);
            }

            @Override
            public <S extends Ticket> List<S> saveAllAndFlush(Iterable<S> entities) {
                return saveAll(entities);
            }

            @Override
            public void deleteAllInBatch(Iterable<Ticket> entities) {
                deleteAll(entities);
            }

            @Override
            public void deleteAllByIdInBatch(Iterable<UUID> uuids) {
                deleteAllById(uuids);
            }

            @Override
            public void deleteAllInBatch() {
                deleteAll();
            }

            @Override
            public Ticket getOne(UUID uuid) {
                return ticketStore.get(uuid);
            }

            @Override
            public Ticket getById(UUID uuid) {
                return ticketStore.get(uuid);
            }

            @Override
            public Ticket getReferenceById(UUID uuid) {
                return ticketStore.get(uuid);
            }

            @Override
            public <S extends Ticket> Optional<S> findOne(org.springframework.data.domain.Example<S> example) {
                return Optional.empty();
            }

            @Override
            public <S extends Ticket> List<S> findAll(org.springframework.data.domain.Example<S> example) {
                return List.of();
            }

            @Override
            public <S extends Ticket> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) {
                return List.of();
            }

            @Override
            public <S extends Ticket> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) {
                return org.springframework.data.domain.Page.empty();
            }

            @Override
            public <S extends Ticket> long count(org.springframework.data.domain.Example<S> example) {
                return 0;
            }

            @Override
            public <S extends Ticket> boolean exists(org.springframework.data.domain.Example<S> example) {
                return false;
            }

            @Override
            public <S extends Ticket, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
                return null;
            }

            @Override
            public List<Ticket> findAll(org.springframework.data.domain.Sort sort) {
                return new ArrayList<>(ticketStore.values());
            }

            @Override
            public org.springframework.data.domain.Page<Ticket> findAll(org.springframework.data.domain.Pageable pageable) {
                return new org.springframework.data.domain.PageImpl<>(new ArrayList<>(ticketStore.values()));
            }

            @Override
            public Optional<Ticket> findOne(org.springframework.data.jpa.domain.Specification<Ticket> spec) {
                return Optional.empty();
            }

            @Override
            public List<Ticket> findAll(org.springframework.data.jpa.domain.Specification<Ticket> spec) {
                return new ArrayList<>(ticketStore.values());
            }

            @Override
            public org.springframework.data.domain.Page<Ticket> findAll(org.springframework.data.jpa.domain.Specification<Ticket> spec, org.springframework.data.domain.Pageable pageable) {
                return new org.springframework.data.domain.PageImpl<>(new ArrayList<>(ticketStore.values()));
            }

            @Override
            public List<Ticket> findAll(org.springframework.data.jpa.domain.Specification<Ticket> spec, org.springframework.data.domain.Sort sort) {
                return new ArrayList<>(ticketStore.values());
            }

            @Override
            public long count(org.springframework.data.jpa.domain.Specification<Ticket> spec) {
                return ticketStore.size();
            }

            @Override
            public boolean exists(org.springframework.data.jpa.domain.Specification<Ticket> spec) {
                return !ticketStore.isEmpty();
            }

            @Override
            public long delete(org.springframework.data.jpa.domain.Specification<Ticket> spec) {
                long count = ticketStore.size();
                ticketStore.clear();
                return count;
            }

            @Override
            public <S extends Ticket, R> R findBy(org.springframework.data.jpa.domain.Specification<Ticket> spec, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
                return null;
            }
        };
    }

    /**
     * Creates a real ClassificationService with test keyword configuration.
     */
    private ClassificationService createTestClassificationService() {
        Map<Category, List<String>> categoryKeywords = new EnumMap<>(Category.class);
        categoryKeywords.put(Category.ACCOUNT_ACCESS, List.of("login", "password", "access", "locked", "authentication"));
        categoryKeywords.put(Category.BILLING_QUESTION, List.of("payment", "invoice", "charge", "refund", "billing"));
        categoryKeywords.put(Category.TECHNICAL_ISSUE, List.of("error", "bug", "crash", "not working", "broken"));
        categoryKeywords.put(Category.BUG_REPORT, List.of("bug", "defect", "issue", "problem"));
        categoryKeywords.put(Category.FEATURE_REQUEST, List.of("feature", "request", "suggest", "would like"));
        categoryKeywords.put(Category.OTHER, List.of());

        Map<Priority, List<String>> priorityKeywords = new EnumMap<>(Priority.class);
        priorityKeywords.put(Priority.URGENT, List.of("urgent", "emergency", "critical", "asap", "immediately"));
        priorityKeywords.put(Priority.HIGH, List.of("important", "high priority", "cannot", "unable", "blocked"));
        priorityKeywords.put(Priority.LOW, List.of("minor", "low priority", "when possible", "nice to have"));
        priorityKeywords.put(Priority.MEDIUM, List.of());

        List<Priority> priorityOrder = List.of(Priority.URGENT, Priority.HIGH, Priority.LOW, Priority.MEDIUM);
        List<Category> categoryOrder = List.of(
            Category.ACCOUNT_ACCESS, Category.BILLING_QUESTION, Category.TECHNICAL_ISSUE,
            Category.BUG_REPORT, Category.FEATURE_REQUEST, Category.OTHER
        );

        return new ClassificationService(categoryKeywords, priorityKeywords, priorityOrder, categoryOrder);
    }
}
