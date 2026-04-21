package com.ween.mapper;

import com.ween.dto.response.LeaderboardEntryResponse;
import com.ween.entity.LeaderboardEntry;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-21T18:21:13+0400",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Microsoft)"
)
@Component
public class LeaderboardEntryMapperImpl implements LeaderboardEntryMapper {

    @Override
    public LeaderboardEntryResponse toLeaderboardEntryResponse(LeaderboardEntry leaderboardEntry) {
        if ( leaderboardEntry == null ) {
            return null;
        }

        LeaderboardEntryResponse.LeaderboardEntryResponseBuilder leaderboardEntryResponse = LeaderboardEntryResponse.builder();

        leaderboardEntryResponse.userId( leaderboardEntry.getUserId() );
        leaderboardEntryResponse.rankPosition( leaderboardEntry.getRankPosition() );
        leaderboardEntryResponse.coinCount( leaderboardEntry.getCoinCount() );
        leaderboardEntryResponse.period( leaderboardEntry.getPeriod() );
        leaderboardEntryResponse.scope( leaderboardEntry.getScope() );

        return leaderboardEntryResponse.build();
    }

    @Override
    public LeaderboardEntry toLeaderboardEntry(LeaderboardEntryResponse response) {
        if ( response == null ) {
            return null;
        }

        LeaderboardEntry.LeaderboardEntryBuilder leaderboardEntry = LeaderboardEntry.builder();

        leaderboardEntry.userId( response.getUserId() );
        leaderboardEntry.period( response.getPeriod() );
        leaderboardEntry.scope( response.getScope() );
        leaderboardEntry.rankPosition( response.getRankPosition() );
        leaderboardEntry.coinCount( response.getCoinCount() );

        return leaderboardEntry.build();
    }
}
