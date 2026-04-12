package com.ween.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ween.dto.request.CreateEventRequest;
import com.ween.dto.request.CreateOrganizationRequest;
import com.ween.dto.request.RegisterRequest;
import com.ween.entity.Event;
import com.ween.entity.Organization;
import com.ween.entity.User;
import com.ween.enums.EventCategory;
import com.ween.enums.EventStatus;
import com.ween.repository.EventRepository;
import com.ween.repository.OrganizationRepository;
import com.ween.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("EventController Integration Tests")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class EventControllerIT {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("ween_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private User organizerUser;
    private User volunteerUser;
    private Organization organization;
    private String organizerToken;
    private String volunteerToken;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        organizationRepository.deleteAll();
        userRepository.deleteAll();

        // Create and register organizer
        organizerUser = User.builder()
                .id(UUID.randomUUID().toString())
                .username("organizer")
                .email("organizer@example.com")
                .fullName("Organizer User")
                .build();
        userRepository.save(organizerUser);

        // Create and register volunteer
        volunteerUser = User.builder()
                .id(UUID.randomUUID().toString())
                .username("volunteer")
                .email("volunteer@example.com")
                .fullName("Volunteer User")
                .build();
        userRepository.save(volunteerUser);

        // Create organization
        organization = Organization.builder()
                .id(UUID.randomUUID().toString())
                .name("Test Organization")
                .ownerId(organizerUser.getId())
                .build();
        organizationRepository.save(organization);
    }

    @Test
    @DisplayName("Should create event as ORGANIZER")
    void testCreateEventAsOrganizer() {
        // Arrange
        CreateEventRequest createRequest = CreateEventRequest.builder()
                .title("Test Event")
                .description("Test Description")
                .category(EventCategory.EDUCATION)
                .city("Boston")
                .address("123 Test St")
                .isOnline(false)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .registrationDeadline(LocalDateTime.now().plusHours(12))
                .maxParticipants(100)
                .build();

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/events?organizationId=" + organization.getId(),
                createRequest,
                Map.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("id"));
        assertEquals(createRequest.getTitle(), response.getBody().get("title"));

        // Verify persistence
        List<Event> events = eventRepository.findAll();
        assertEquals(1, events.size());
        assertEquals(organization.getId(), events.get(0).getOrganizationId());
    }

    @Test
    @DisplayName("Should search events with filters")
    void testSearchEventsWithFilters() {
        // Arrange - Create test events
        Event event1 = Event.builder()
                .id(UUID.randomUUID().toString())
                .title("Education Event")
                .category(EventCategory.EDUCATION)
                .city("Boston")
                .organizationId(organization.getId())
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .status(EventStatus.PUBLISHED)
                .build();
        eventRepository.save(event1);

        Event event2 = Event.builder()
                .id(UUID.randomUUID().toString())
                .title("Health Event")
                .category(EventCategory.HEALTH)
                .city("New York")
                .organizationId(organization.getId())
                .startDate(LocalDateTime.now().plusDays(5))
                .endDate(LocalDateTime.now().plusDays(6))
                .status(EventStatus.PUBLISHED)
                .build();
        eventRepository.save(event2);

        // Act - Search by category
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/v1/events/search?category=EDUCATION",
                Map.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should search events by city filter")
    void testSearchEventsByCity() {
        // Arrange
        Event event = Event.builder()
                .id(UUID.randomUUID().toString())
                .title("Boston Event")
                .city("Boston")
                .organizationId(organization.getId())
                .startDate(LocalDateTime.now().plusDays(1))
                .status(EventStatus.PUBLISHED)
                .build();
        eventRepository.save(event);

        // Act
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/v1/events/search?city=Boston",
                Map.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should search events by date range")
    void testSearchEventsByDateRange() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(2);
        
        Event event = Event.builder()
                .id(UUID.randomUUID().toString())
                .title("Date Range Event")
                .organizationId(organization.getId())
                .startDate(startDate)
                .endDate(endDate)
                .status(EventStatus.PUBLISHED)
                .build();
        eventRepository.save(event);

        // Act
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/v1/events/search?startDate=" + startDate + "&endDate=" + endDate,
                Map.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should register VOLUNTEER for event")
    void testVolunteerRegistrationForEvent() {
        // Arrange
        Event event = Event.builder()
                .id(UUID.randomUUID().toString())
                .title("Volunteer Event")
                .organizationId(organization.getId())
                .maxParticipants(50)
                .status(EventStatus.PUBLISHED)
                .registrationDeadline(LocalDateTime.now().plusHours(12))
                .build();
        eventRepository.save(event);

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/events/" + event.getId() + "/register",
                Map.of("userId", volunteerUser.getId()),
                Map.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should get event details including participants")
    void testGetEventWithParticipants() {
        // Arrange
        Event event = Event.builder()
                .id(UUID.randomUUID().toString())
                .title("Test Event")
                .description("Event Description")
                .organizationId(organization.getId())
                .maxParticipants(100)
                .status(EventStatus.PUBLISHED)
                .build();
        eventRepository.save(event);

        // Act
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/v1/events/" + event.getId(),
                Map.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> eventData = response.getBody();
        assertNotNull(eventData);
        assertEquals(event.getId(), eventData.get("id"));
        assertEquals(event.getTitle(), eventData.get("title"));
    }

    @Test
    @DisplayName("Should prevent registration when event is at capacity")
    void testPreventRegistrationAtCapacity() {
        // Arrange
        Event event = Event.builder()
                .id(UUID.randomUUID().toString())
                .title("Capacity Test Event")
                .organizationId(organization.getId())
                .maxParticipants(1)
                .status(EventStatus.PUBLISHED)
                .registrationDeadline(LocalDateTime.now().plusHours(12))
                .build();
        eventRepository.save(event);

        // First registration
        restTemplate.postForEntity(
                "/api/v1/events/" + event.getId() + "/register",
                Map.of("userId", volunteerUser.getId()),
                Map.class
        );

        // Act - Try second registration (should fail)
        User anotherVolunteer = User.builder()
                .id(UUID.randomUUID().toString())
                .username("volunteer2")
                .email("volunteer2@example.com")
                .build();
        userRepository.save(anotherVolunteer);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/events/" + event.getId() + "/register",
                Map.of("userId", anotherVolunteer.getId()),
                Map.class
        );

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("Should prevent duplicate registrations")
    void testPreventDuplicateRegistration() {
        // Arrange
        Event event = Event.builder()
                .id(UUID.randomUUID().toString())
                .title("Duplicate Test Event")
                .organizationId(organization.getId())
                .maxParticipants(100)
                .status(EventStatus.PUBLISHED)
                .registrationDeadline(LocalDateTime.now().plusHours(12))
                .build();
        eventRepository.save(event);

        // First registration
        restTemplate.postForEntity(
                "/api/v1/events/" + event.getId() + "/register",
                Map.of("userId", volunteerUser.getId()),
                Map.class
        );

        // Act - Try duplicate registration
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/events/" + event.getId() + "/register",
                Map.of("userId", volunteerUser.getId()),
                Map.class
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should verify event data persists correctly in database")
    void testEventDataPersistence() {
        // Arrange
        CreateEventRequest createRequest = CreateEventRequest.builder()
                .title("Persistence Test Event")
                .description("Test Description")
                .category(EventCategory.ENVIRONMENT)
                .city("San Francisco")
                .address("456 Green St")
                .isOnline(false)
                .startDate(LocalDateTime.now().plusDays(3))
                .endDate(LocalDateTime.now().plusDays(4))
                .registrationDeadline(LocalDateTime.now().plusDays(2))
                .maxParticipants(75)
                .build();

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/events?organizationId=" + organization.getId(),
                createRequest,
                Map.class
        );

        String eventId = (String) response.getBody().get("id");

        // Verify persistence
        Optional<Event> savedEvent = eventRepository.findById(eventId);
        assertTrue(savedEvent.isPresent());
        
        Event event = savedEvent.get();
        assertEquals(createRequest.getTitle(), event.getTitle());
        assertEquals(createRequest.getDescription(), event.getDescription());
        assertEquals(createRequest.getCategory(), event.getCategory());
        assertEquals(createRequest.getCity(), event.getCity());
        assertEquals(createRequest.getAddress(), event.getAddress());
        assertEquals(createRequest.getIsOnline(), event.getIsOnline());
        assertEquals(createRequest.getMaxParticipants(), event.getMaxParticipants());
    }

    @Test
    @DisplayName("Should update event successfully")
    void testUpdateEvent() {
        // Arrange
        Event event = Event.builder()
                .id(UUID.randomUUID().toString())
                .title("Original Title")
                .description("Original Description")
                .organizationId(organization.getId())
                .status(EventStatus.DRAFT)
                .build();
        eventRepository.save(event);

        // Act
        Map<String, String> updateRequest = Map.of(
                "title", "Updated Title",
                "description", "Updated Description"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/events/" + event.getId(),
                updateRequest,
                Map.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify persistence
        Optional<Event> updatedEvent = eventRepository.findById(event.getId());
        assertTrue(updatedEvent.isPresent());
        assertEquals("Updated Title", updatedEvent.get().getTitle());
        assertEquals("Updated Description", updatedEvent.get().getDescription());
    }

    @Test
    @DisplayName("Should return 404 for non-existent event")
    void testGetNonExistentEvent() {
        // Act
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/v1/events/" + UUID.randomUUID().toString(),
                Map.class
        );

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should list user's registered events")
    void testGetUserRegisteredEvents() {
        // Arrange
        Event event = Event.builder()
                .id(UUID.randomUUID().toString())
                .title("User Event")
                .organizationId(organization.getId())
                .status(EventStatus.PUBLISHED)
                .build();
        eventRepository.save(event);

        // Act
        ResponseEntity<List> response = restTemplate.getForEntity(
                "/api/v1/users/" + volunteerUser.getId() + "/events",
                List.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
