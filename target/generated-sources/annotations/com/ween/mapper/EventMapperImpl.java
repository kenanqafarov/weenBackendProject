package com.ween.mapper;

import com.ween.dto.response.EventDetailResponse;
import com.ween.dto.response.EventResponse;
import com.ween.entity.Event;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-21T22:28:19+0400",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class EventMapperImpl implements EventMapper {

    @Override
    public EventResponse toEventResponse(Event event) {
        if ( event == null ) {
            return null;
        }

        EventResponse.EventResponseBuilder eventResponse = EventResponse.builder();

        eventResponse.address( event.getAddress() );
        eventResponse.category( event.getCategory() );
        eventResponse.city( event.getCity() );
        eventResponse.coverImageUrl( event.getCoverImageUrl() );
        eventResponse.createdAt( event.getCreatedAt() );
        eventResponse.description( event.getDescription() );
        eventResponse.endDate( event.getEndDate() );
        eventResponse.id( event.getId() );
        eventResponse.isOnline( event.getIsOnline() );
        eventResponse.maxParticipants( event.getMaxParticipants() );
        eventResponse.organizationId( event.getOrganizationId() );
        eventResponse.registrationDeadline( event.getRegistrationDeadline() );
        eventResponse.startDate( event.getStartDate() );
        eventResponse.status( event.getStatus() );
        eventResponse.title( event.getTitle() );
        eventResponse.updatedAt( event.getUpdatedAt() );

        return eventResponse.build();
    }

    @Override
    public EventDetailResponse toEventDetailResponse(Event event) {
        if ( event == null ) {
            return null;
        }

        EventDetailResponse.EventDetailResponseBuilder eventDetailResponse = EventDetailResponse.builder();

        eventDetailResponse.address( event.getAddress() );
        eventDetailResponse.category( event.getCategory() );
        eventDetailResponse.city( event.getCity() );
        eventDetailResponse.coverImageUrl( event.getCoverImageUrl() );
        eventDetailResponse.createdAt( event.getCreatedAt() );
        eventDetailResponse.customFields( event.getCustomFields() );
        eventDetailResponse.description( event.getDescription() );
        eventDetailResponse.endDate( event.getEndDate() );
        eventDetailResponse.id( event.getId() );
        eventDetailResponse.isOnline( event.getIsOnline() );
        eventDetailResponse.maxParticipants( event.getMaxParticipants() );
        eventDetailResponse.organizationId( event.getOrganizationId() );
        eventDetailResponse.registrationDeadline( event.getRegistrationDeadline() );
        eventDetailResponse.startDate( event.getStartDate() );
        eventDetailResponse.status( event.getStatus() );
        eventDetailResponse.title( event.getTitle() );
        eventDetailResponse.updatedAt( event.getUpdatedAt() );

        return eventDetailResponse.build();
    }

    @Override
    public Event toEvent(EventResponse eventResponse) {
        if ( eventResponse == null ) {
            return null;
        }

        Event.EventBuilder event = Event.builder();

        event.address( eventResponse.getAddress() );
        event.category( eventResponse.getCategory() );
        event.city( eventResponse.getCity() );
        event.coverImageUrl( eventResponse.getCoverImageUrl() );
        event.description( eventResponse.getDescription() );
        event.endDate( eventResponse.getEndDate() );
        event.isOnline( eventResponse.getIsOnline() );
        event.maxParticipants( eventResponse.getMaxParticipants() );
        event.organizationId( eventResponse.getOrganizationId() );
        event.registrationDeadline( eventResponse.getRegistrationDeadline() );
        event.startDate( eventResponse.getStartDate() );
        event.status( eventResponse.getStatus() );
        event.title( eventResponse.getTitle() );

        return event.build();
    }
}
