package com.ween.service;

import com.ween.dto.request.CreateEventRequest;
import com.ween.dto.request.UpdateEventRequest;
import com.ween.dto.response.EventDetailResponse;
import com.ween.dto.response.EventResponse;
import com.ween.dto.response.EventStatsResponse;
import com.ween.entity.Event;
import com.ween.entity.Organization;
import com.ween.enums.EventCategory;
import com.ween.enums.EventStatus;
import com.ween.exception.ResourceNotFoundException;
import com.ween.mapper.EventMapper;
import com.ween.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final OrganizationService organizationService;
    private final RegistrationService registrationService;

    @Transactional
    public Event createEvent(CreateEventRequest request, String organizationId) {
        Organization organization = organizationService.getOrganizationById(organizationId);

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .city(request.getCity())
                .address(request.getAddress())
                .isOnline(request.getIsOnline())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .registrationDeadline(request.getRegistrationDeadline())
                .maxParticipants(request.getMaxParticipants())
                .organizationId(organizationId)
                .status(EventStatus.DRAFT)
                .build();

        Event saved = eventRepository.save(event);
        log.info("Event created: {} by organization: {}", saved.getTitle(), organizationId);
        return saved;
    }

    public Event getEventById(String eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
    }

    @Transactional
    public Event updateEvent(String eventId, String id, UpdateEventRequest request) {
        Event event = getEventById(eventId);

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }

        if (request.getCategory() != null) {
            event.setCategory(request.getCategory());
        }

        if (request.getCity() != null) {
            event.setCity(request.getCity());
        }

        if (request.getAddress() != null) {
            event.setAddress(request.getAddress());
        }

        if (request.getIsOnline() != null) {
            event.setIsOnline(request.getIsOnline());
        }

        if (request.getStartDate() != null) {
            event.setStartDate(request.getStartDate());
        }

        if (request.getEndDate() != null) {
            event.setEndDate(request.getEndDate());
        }

        if (request.getRegistrationDeadline() != null) {
            event.setRegistrationDeadline(request.getRegistrationDeadline());
        }

        if (request.getMaxParticipants() != null) {
            event.setMaxParticipants(request.getMaxParticipants());
        }

        if (request.getStatus() != null) {
            event.setStatus(request.getStatus());
        }

        Event updated = eventRepository.save(event);
        log.info("Event updated: {}", eventId);
        return updated;
    }

    @Transactional
    public void publishEvent(String eventId) {
        Event event = getEventById(eventId);
        event.setStatus(EventStatus.PUBLISHED);
        eventRepository.save(event);
        log.info("Event published: {}", eventId);
    }

    @Transactional
    public void startEvent(String eventId) {
        Event event = getEventById(eventId);
        event.setStatus(EventStatus.ONGOING);
        eventRepository.save(event);
        log.info("Event started: {}", eventId);
    }

    @Transactional
    public void completeEvent(String eventId) {
        Event event = getEventById(eventId);
        event.setStatus(EventStatus.COMPLETED);
        eventRepository.save(event);
        log.info("Event completed: {}", eventId);
    }

    @Transactional
    public void cancelEvent(String eventId, String id) {
        Event event = getEventById(eventId);
        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
        log.info("Event cancelled: {}", eventId);
    }

    @Transactional
    public void deleteEvent(String eventId) {
        Event event = getEventById(eventId);
        eventRepository.delete(event);
        log.info("Event deleted: {}", eventId);
    }

    public Page<Event> getEventsByOrganization(String organizationId, Pageable pageable) {
        return eventRepository.findByOrganizationId(organizationId, pageable);
    }

    public Page<Event> getAllPublishedEvents(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    public long getEventParticipantCount(String eventId) {
        return eventRepository.findById(eventId)
                .map(event -> {
                    // Will be calculated from EventRegistration count in RegistrationService
                    return 0L;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    public boolean isEventCapacityFull(String eventId) {
        Event event = getEventById(eventId);
        if (event.getMaxParticipants() == null) {
            return false;
        }

        // Get registrations count from repository
        long registrationCount = eventRepository.findById(eventId)
                .map(e -> 0L) // Will be replaced with actual count from RegistrationService
                .orElse(0L);

        return registrationCount >= event.getMaxParticipants();
    }

    public Integer getRemainingCapacity(String eventId) {
        Event event = getEventById(eventId);
        if (event.getMaxParticipants() == null) {
            return Integer.MAX_VALUE;
        }

        long registrationCount = 0; // Will be fetched from RegistrationService
        return Math.max(0, (int) (event.getMaxParticipants() - registrationCount));
    }

    public boolean isRegistrationDeadlinePassed(String eventId) {
        Event event = getEventById(eventId);
        if (event.getRegistrationDeadline() == null) {
            return false;
        }
        return java.time.LocalDateTime.now().isAfter(event.getRegistrationDeadline());
    }

    public boolean isEventInFuture(String eventId) {
        Event event = getEventById(eventId);
        if (event.getStartDate() == null) {
            return false;
        }
        return event.getStartDate().isAfter(java.time.LocalDateTime.now());
    }

    public boolean isEventOngoing(String eventId) {
        Event event = getEventById(eventId);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        return now.isAfter(event.getStartDate()) && now.isBefore(event.getEndDate());
    }

    @Transactional
    public void setCustomFields(String eventId, String customFieldsJson) {
        Event event = getEventById(eventId);
        event.setCustomFields(customFieldsJson);
        eventRepository.save(event);
        log.info("Custom fields set for event: {}", eventId);
    }

    public Integer getEventLimitForOrganization(String organizationId) {
        return Integer.MAX_VALUE;
    }

    public Page<EventResponse> getOrganizationEvents(String id, Pageable pageable) {
        Page<Event> events = eventRepository.findByOrganizationId(id, pageable);
        return events.map(event -> {
            EventResponse response = eventMapper.toEventResponse(event);
            response.setCurrentRegistrations((int) registrationService.getEventRegistrationCount(event.getId()));
            return response;
        });
    }

    public Page<EventResponse> listEvents(EventCategory category, String city, LocalDateTime dateFrom, LocalDateTime dateTo, String search, String organizationId, String sort, Pageable pageable) {
        Page<Event> events = eventRepository.findAll(pageable);
        
        var eventList = events.getContent().stream()
                .filter(e -> category == null || category.equals(e.getCategory()))
                .filter(e -> city == null || city.isEmpty() || city.equalsIgnoreCase(e.getCity()))
                .filter(e -> dateFrom == null || (e.getStartDate() != null && e.getStartDate().isAfter(dateFrom)))
                .filter(e -> dateTo == null || (e.getEndDate() != null && e.getEndDate().isBefore(dateTo)))
                .filter(e -> search == null || search.isEmpty() || 
                        e.getTitle().toLowerCase().contains(search.toLowerCase()) ||
                        e.getDescription().toLowerCase().contains(search.toLowerCase()))
                .filter(e -> organizationId == null || organizationId.isEmpty() || organizationId.equals(e.getOrganizationId()))
                .filter(e -> EventStatus.PUBLISHED.equals(e.getStatus()))
                .map(event -> {
                    EventResponse response = eventMapper.toEventResponse(event);
                    response.setCurrentRegistrations((int) registrationService.getEventRegistrationCount(event.getId()));
                    try {
                        Organization org = organizationService.getOrganizationById(event.getOrganizationId());
                        response.setOrganizationName(org.getName());
                    } catch (Exception e) {
                        log.warn("Organization not found for event: {}", event.getId());
                    }
                    return response;
                })
                .toList();
        
        return new PageImpl<>(eventList, pageable, eventList.size());
    }

    public EventDetailResponse getEventDetail(String id) {
        Event event = getEventById(id);
        EventDetailResponse response = eventMapper.toEventDetailResponse(event);
        response.setCurrentRegistrations((int) registrationService.getEventRegistrationCount(id));
        response.setAttendeeCount((int) registrationService.getEventJoinedCount(id));
        
        try {
            Organization org = organizationService.getOrganizationById(event.getOrganizationId());
            response.setOrganizationName(org.getName());
        } catch (Exception e) {
            log.warn("Organization not found for event: {}", event.getId());
        }
        
        return response;
    }

    public EventStatsResponse getEventStats(String userId, String id) {
        Event event = getEventById(id);
        long totalRegistered = registrationService.getEventRegistrationCount(id);
        long totalAttended = registrationService.getEventJoinedCount(id);
        
        long registrationRate = event.getMaxParticipants() != null && event.getMaxParticipants() > 0
                ? (totalRegistered * 100) / event.getMaxParticipants()
                : 0;
        
        long attendanceRate = totalRegistered > 0
                ? (totalAttended * 100) / totalRegistered
                : 0;
        
        return EventStatsResponse.builder()
                .eventId(event.getId())
                .eventTitle(event.getTitle())
                .totalRegistered(totalRegistered)
                .totalAttended(totalAttended)
                .registrationRate(registrationRate)
                .attendanceRate(attendanceRate)
                .build();
    }
}
