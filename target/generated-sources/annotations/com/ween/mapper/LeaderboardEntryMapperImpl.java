package com.ween.mapper;

import com.ween.dto.response.LeaderboardEntryResponse;
import com.ween.entity.LeaderboardEntry;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-17T14:02:35+0400",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class LeaderboardEntryMapperImpl implements LeaderboardEntryMapper {

    @Override
    public LeaderboardEntryResponse toLeaderboardEntryResponse(LeaderboardEntry leaderboardEntry) {
        if ( leaderboardEntry == null ) {
            return null;
        }

        LeaderboardEntryResponse.LeaderboardEntryResponseBuilder leaderboardEntryResponse = LeaderboardEntryResponse.builder();

        leaderboardEntryResponse.coinCount( leaderboardEntry.getCoinCount() );
        leaderboardEntryResponse.period( leaderboardEntry.getPeriod() );
        leaderboardEntryResponse.rankPosition( leaderboardEntry.getRankPosition() );
        leaderboardEntryResponse.scope( leaderboardEntry.getScope() );
        leaderboardEntryResponse.userId( leaderboardEntry.getUserId() );

        return leaderboardEntryResponse.build();
    }

    @Override
    public LeaderboardEntry toLeaderboardEntry(LeaderboardEntryResponse response) {
        if ( response == null ) {
            return null;
        }

        LeaderboardEntry.LeaderboardEntryBuilder leaderboardEntry = LeaderboardEntry.builder();

        leaderboardEntry.coinCount( response.getCoinCount() );
        leaderboardEntry.period( response.getPeriod() );
        leaderboardEntry.rankPosition( response.getRankPosition() );
        leaderboardEntry.scope( response.getScope() );
        leaderboardEntry.userId( response.getUserId() );

        return leaderboardEntry.build();
    }
}
