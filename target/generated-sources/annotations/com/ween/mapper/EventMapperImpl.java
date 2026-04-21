package com.ween.mapper;

import com.ween.dto.response.EventDetailResponse;
import com.ween.dto.response.EventResponse;
import com.ween.entity.Event;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-21T18:21:11+0400",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Microsoft)"
)
@Component
public class EventMapperImpl implements EventMapper {

    @Override
    public EventResponse toEventResponse(Event event) {
        if ( event == null ) {
            return null;
        }

        EventResponse.EventResponseBuilder eventResponse = EventResponse.builder();

        eventResponse.id( event.getId() );
        eventResponse.title( event.getTitle() );
        eventResponse.description( event.getDescription() );
        eventResponse.category( event.getCategory() );
        eventResponse.city( event.getCity() );
        eventResponse.address( event.getAddress() );
        eventResponse.isOnline( event.getIsOnline() );
        eventResponse.startDate( event.getStartDate() );
        eventResponse.endDate( event.getEndDate() );
        eventResponse.registrationDeadline( event.getRegistrationDeadline() );
        eventResponse.maxParticipants( event.getMaxParticipants() );
        eventResponse.organizationId( event.getOrganizationId() );
        eventResponse.status( event.getStatus() );
        eventResponse.coverImageUrl( event.getCoverImageUrl() );
        eventResponse.createdAt( event.getCreatedAt() );
        eventResponse.updatedAt( event.getUpdatedAt() );

        return eventResponse.build();
    }

    @Override
    public EventDetailResponse toEventDetailResponse(Event event) {
        if ( event == null ) {
            return null;
        }

        EventDetailResponse.EventDetailResponseBuilder eventDetailResponse = EventDetailResponse.builder();

        eventDetailResponse.id( event.getId() );
        eventDetailResponse.title( event.getTitle() );
        eventDetailResponse.description( event.getDescription() );
        eventDetailResponse.category( event.getCategory() );
        eventDetailResponse.city( event.getCity() );
        eventDetailResponse.address( event.getAddress() );
        eventDetailResponse.isOnline( event.getIsOnline() );
        eventDetailResponse.startDate( event.getStartDate() );
        eventDetailResponse.endDate( event.getEndDate() );
        eventDetailResponse.registrationDeadline( event.getRegistrationDeadline() );
        eventDetailResponse.maxParticipants( event.getMaxParticipants() );
        eventDetailResponse.organizationId( event.getOrganizationId() );
        eventDetailResponse.status( event.getStatus() );
        eventDetailResponse.coverImageUrl( event.getCoverImageUrl() );
        eventDetailResponse.customFields( event.getCustomFields() );
        eventDetailResponse.createdAt( event.getCreatedAt() );
        eventDetailResponse.updatedAt( event.getUpdatedAt() );

        return eventDetailResponse.build();
    }

    @Override
    public Event toEvent(EventResponse eventResponse) {
        if ( eventResponse == null ) {
            return null;
        }

        Event.EventBuilder event = Event.builder();

        event.title( eventResponse.getTitle() );
        event.description( eventResponse.getDescription() );
        event.category( eventResponse.getCategory() );
        event.city( eventResponse.getCity() );
        event.address( eventResponse.getAddress() );
        event.isOnline( eventResponse.getIsOnline() );
        event.startDate( eventResponse.getStartDate() );
        event.endDate( eventResponse.getEndDate() );
        event.registrationDeadline( eventResponse.getRegistrationDeadline() );
        event.maxParticipants( eventResponse.getMaxParticipants() );
        event.organizationId( eventResponse.getOrganizationId() );
        event.status( eventResponse.getStatus() );
        event.coverImageUrl( eventResponse.getCoverImageUrl() );

        return event.build();
    }
}
