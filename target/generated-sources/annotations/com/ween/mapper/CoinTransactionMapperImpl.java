package com.ween.mapper;

import com.ween.dto.response.CoinTransactionResponse;
import com.ween.entity.CoinTransaction;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-17T15:04:43+0400",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class CoinTransactionMapperImpl implements CoinTransactionMapper {

    @Override
    public CoinTransactionResponse toCoinTransactionResponse(CoinTransaction transaction) {
        if ( transaction == null ) {
            return null;
        }

        CoinTransactionResponse.CoinTransactionResponseBuilder coinTransactionResponse = CoinTransactionResponse.builder();

        coinTransactionResponse.amount( transaction.getAmount() );
        coinTransactionResponse.createdAt( transaction.getCreatedAt() );
        coinTransactionResponse.id( transaction.getId() );
        coinTransactionResponse.reason( transaction.getReason() );
        coinTransactionResponse.relatedEntityId( transaction.getRelatedEntityId() );

        return coinTransactionResponse.build();
    }
}
