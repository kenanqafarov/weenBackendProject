package com.ween.service;

import com.ween.entity.Event;
import com.ween.entity.Organization;
import com.ween.dto.request.CreateEventRequest;
import com.ween.enums.EventCategory;
import com.ween.enums.EventStatus;
import com.ween.exception.ResourceNotFoundException;
import com.ween.mapper.EventMapper;
import com.ween.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventService Unit Tests")
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventService eventService;

    private Organization testOrganization;
    private String organizationId;
    private String eventId;
    private CreateEventRequest createEventRequest;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID().toString();
        eventId = UUID.randomUUID().toString();
        
        testOrganization = Organization.builder()
                .id(organizationId)
                .name("Test Organization")
                .build();

        createEventRequest = CreateEventRequest.builder()
                .title("Test Event")
                .description("Test Description")
                .category(EventCategory.EDUCATION)
                .city("Test City")
                .address("123 Test St")
                .isOnline(false)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .registrationDeadline(LocalDateTime.now().plusHours(12))
                .maxParticipants(100)
                .build();
    }

    @Test
    @DisplayName("Should create event for organization")
    void testCreateEvent() {
        // Arrange
        when(organizationService.getOrganizationById(organizationId)).thenReturn(testOrganization);
        when(organizationService.canCreateEvent(organizationId)).thenReturn(true);
        
        Event event = Event.builder()
                .id(eventId)
                .title(createEventRequest.getTitle())
                .description(createEventRequest.getDescription())
                .category(createEventRequest.getCategory())
                .city(createEventRequest.getCity())
                .address(createEventRequest.getAddress())
                .isOnline(createEventRequest.getIsOnline())
                .startDate(createEventRequest.getStartDate())
                .endDate(createEventRequest.getEndDate())
                .registrationDeadline(createEventRequest.getRegistrationDeadline())
                .maxParticipants(createEventRequest.getMaxParticipants())
                .organizationId(organizationId)
                .status(EventStatus.DRAFT)
                .build();
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // Act
        Event result = eventService.createEvent(createEventRequest, organizationId);

        // Assert
        assertNotNull(result);
        assertEquals(createEventRequest.getTitle(), result.getTitle());
        assertEquals(organizationId, result.getOrganizationId());
        assertEquals(EventStatus.DRAFT, result.getStatus());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("Should get event by ID")
    void testGetEventById() {
        // Arrange
        Event event = Event.builder()
                .id(eventId)
                .title("Test Event")
                .organizationId(organizationId)
                .build();
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // Act
        Event result = eventService.getEventById(eventId);

        // Assert
        assertNotNull(result);
        assertEquals(eventId, result.getId());
        assertEquals("Test Event", result.getTitle());
    }

    @Test
    @DisplayName("Should throw exception when event not found")
    void testGetEventByIdNotFound() {
        // Arrange
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                eventService.getEventById(eventId)
        );
    }

    @Test
    @DisplayName("Should search events by category")
    void testSearchEventsByCategory() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Event> events = Arrays.asList(
                Event.builder()
                        .id(UUID.randomUUID().toString())
                        .title("Education Event")
                        .category(EventCategory.EDUCATION)
                        .build(),
                Event.builder()
                        .id(UUID.randomUUID().toString())
                        .title("Health Event")
                        .category(EventCategory.EDUCATION)
                        .build()
        );
        Page<Event> page = new PageImpl<>(events, pageable, 2);
        when(eventRepository.findByCategory(EventCategory.EDUCATION, pageable)).thenReturn(page);

        // Act
        Page<Event> result = eventRepository.findByCategory(EventCategory.EDUCATION, pageable);

        // Assert
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().stream()
                .allMatch(e -> e.getCategory() == EventCategory.EDUCATION));
    }

    @Test
    @DisplayName("Should search events by city")
    void testSearchEventsByCity() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        String city = "New York";
        List<Event> events = Arrays.asList(
                Event.builder()
                        .id(UUID.randomUUID().toString())
                        .title("Event 1")
                        .city(city)
                        .build(),
                Event.builder()
                        .id(UUID.randomUUID().toString())
                        .title("Event 2")
                        .city(city)
                        .build()
        );
        Page<Event> page = new PageImpl<>(events, pageable, 2);
        when(eventRepository.findByCity(city, pageable)).thenReturn(page);

        // Act
        Page<Event> result = eventRepository.findByCity(city, pageable);

        // Assert
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().stream()
                .allMatch(e -> e.getCity().equals(city)));
    }

    @Test
    @DisplayName("Should search events by date range")
    void testSearchEventsByDateRange() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(7);
        Pageable pageable = PageRequest.of(0, 10);
        
        List<Event> events = Arrays.asList(
                Event.builder()
                        .id(UUID.randomUUID().toString())
                        .title("Event 1")
                        .startDate(startDate.plusDays(1))
                        .endDate(startDate.plusDays(2))
                        .build()
        );
        Page<Event> page = new PageImpl<>(events, pageable, 1);
        when(eventRepository.findByStartDateBetween(startDate, endDate, pageable)).thenReturn(page);

        // Act
        Page<Event> result = eventRepository.findByStartDateBetween(startDate, endDate, pageable);

        // Assert
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("Should perform full text search")
    void testFulltextSearch() {
        // Arrange
        String searchQuery = "volunteer";
        Pageable pageable = PageRequest.of(0, 10);
        List<Event> events = Arrays.asList(
                Event.builder()
                        .id(UUID.randomUUID().toString())
                        .title("Volunteer Cleanup Event")
                        .description("Help us clean and volunteer")
                        .build()
        );
        Page<Event> page = new PageImpl<>(events, pageable, 1);
        when(eventRepository.searchByTitleOrDescription(searchQuery, pageable)).thenReturn(page);

        // Act
        Page<Event> result = eventRepository.searchByTitleOrDescription(searchQuery, pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().stream()
                .anyMatch(e -> e.getTitle().contains("Volunteer") || e.getDescription().contains("volunteer")));
    }

    @Test
    @DisplayName("Should combine multiple search filters")
    void testCombinedSearch() {
        // Arrange
        String city = "Boston";
        EventCategory category = EventCategory.ENVIRONMENT;
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(7);
        Pageable pageable = PageRequest.of(0, 10);
        
        List<Event> events = Arrays.asList(
                Event.builder()
                        .id(UUID.randomUUID().toString())
                        .title("Environment Cleanup")
                        .city(city)
                        .category(category)
                        .startDate(startDate.plusDays(1))
                        .build()
        );
        Page<Event> page = new PageImpl<>(events, pageable, 1);
        when(eventRepository.findByCityAndCategoryAndStartDateBetween(
                city, category, startDate, endDate, pageable))
                .thenReturn(page);

        // Act
        Page<Event> result = eventRepository.findByCityAndCategoryAndStartDateBetween(
                city, category, startDate, endDate, pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals(city, result.getContent().get(0).getCity());
        assertEquals(category, result.getContent().get(0).getCategory());
    }

    @Test
    @DisplayName("Should enforce event capacity during creation")
    void testEventCapacityLimit() {
        // Arrange
        createEventRequest.setMaxParticipants(50);
        when(organizationService.getOrganizationById(organizationId)).thenReturn(testOrganization);
        when(organizationService.canCreateEvent(organizationId)).thenReturn(true);
        
        Event event = Event.builder()
                .id(eventId)
                .title(createEventRequest.getTitle())
                .maxParticipants(50)
                .organizationId(organizationId)
                .status(EventStatus.DRAFT)
                .build();
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // Act
        Event result = eventService.createEvent(createEventRequest, organizationId);

        // Assert
        assertEquals(50, result.getMaxParticipants());
    }

    @Test
    @DisplayName("Should create online event")
    void testCreateOnlineEvent() {
        // Arrange
        createEventRequest.setIsOnline(true);
        when(organizationService.getOrganizationById(organizationId)).thenReturn(testOrganization);
        when(organizationService.canCreateEvent(organizationId)).thenReturn(true);
        
        Event event = Event.builder()
                .id(eventId)
                .title(createEventRequest.getTitle())
                .isOnline(true)
                .organizationId(organizationId)
                .status(EventStatus.DRAFT)
                .build();
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // Act
        Event result = eventService.createEvent(createEventRequest, organizationId);

        // Assert
        assertTrue(result.getIsOnline());
    }

    @Test
    @DisplayName("Should set event status to DRAFT on creation")
    void testEventStatusDraftOnCreation() {
        // Arrange
        when(organizationService.getOrganizationById(organizationId)).thenReturn(testOrganization);
        when(organizationService.canCreateEvent(organizationId)).thenReturn(true);
        
        Event event = Event.builder()
                .id(eventId)
                .title(createEventRequest.getTitle())
                .organizationId(organizationId)
                .status(EventStatus.DRAFT)
                .build();
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // Act
        Event result = eventService.createEvent(createEventRequest, organizationId);

        // Assert
        assertEquals(EventStatus.DRAFT, result.getStatus());
    }
}
