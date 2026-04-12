package com.ween.mapper;

import com.ween.dto.response.CoinTransactionResponse;
import com.ween.entity.CoinTransaction;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-13T00:16:43+0400",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Microsoft)"
)
@Component
public class CoinTransactionMapperImpl implements CoinTransactionMapper {

    @Override
    public CoinTransactionResponse toCoinTransactionResponse(CoinTransaction transaction) {
        if ( transaction == null ) {
            return null;
        }

        CoinTransactionResponse.CoinTransactionResponseBuilder coinTransactionResponse = CoinTransactionResponse.builder();

        coinTransactionResponse.id( transaction.getId() );
        coinTransactionResponse.amount( transaction.getAmount() );
        coinTransactionResponse.reason( transaction.getReason() );
        coinTransactionResponse.relatedEntityId( transaction.getRelatedEntityId() );
        coinTransactionResponse.createdAt( transaction.getCreatedAt() );

        return coinTransactionResponse.build();
    }
}
