package com.ween.mapper;

import com.ween.dto.response.NotificationResponse;
import com.ween.entity.Notification;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-17T15:04:41+0400",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class NotificationMapperImpl implements NotificationMapper {

    @Override
    public NotificationResponse toNotificationResponse(Notification notification) {
        if ( notification == null ) {
            return null;
        }

        NotificationResponse.NotificationResponseBuilder notificationResponse = NotificationResponse.builder();

        notificationResponse.body( notification.getBody() );
        notificationResponse.createdAt( notification.getCreatedAt() );
        notificationResponse.id( notification.getId() );
        notificationResponse.isRead( notification.getIsRead() );
        notificationResponse.title( notification.getTitle() );
        notificationResponse.type( notification.getType() );

        return notificationResponse.build();
    }
}
