package com.ween.mapper;

import com.ween.dto.response.CertificateResponse;
import com.ween.entity.Certificate;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-19T04:10:46+0400",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Microsoft)"
)
@Component
public class CertificateMapperImpl implements CertificateMapper {

    @Override
    public CertificateResponse toCertificateResponse(Certificate certificate) {
        if ( certificate == null ) {
            return null;
        }

        CertificateResponse.CertificateResponseBuilder certificateResponse = CertificateResponse.builder();

        certificateResponse.id( certificate.getId() );
        certificateResponse.certificateNumber( certificate.getCertificateNumber() );
        certificateResponse.userId( certificate.getUserId() );
        certificateResponse.eventId( certificate.getEventId() );
        certificateResponse.pdfUrl( certificate.getPdfUrl() );
        certificateResponse.templateType( certificate.getTemplateType() );
        certificateResponse.issuedAt( certificate.getIssuedAt() );

        return certificateResponse.build();
    }

    @Override
    public Certificate toCertificate(CertificateResponse certificateResponse) {
        if ( certificateResponse == null ) {
            return null;
        }

        Certificate.CertificateBuilder certificate = Certificate.builder();

        certificate.userId( certificateResponse.getUserId() );
        certificate.eventId( certificateResponse.getEventId() );
        certificate.certificateNumber( certificateResponse.getCertificateNumber() );
        certificate.pdfUrl( certificateResponse.getPdfUrl() );
        certificate.templateType( certificateResponse.getTemplateType() );
        certificate.issuedAt( certificateResponse.getIssuedAt() );

        return certificate.build();
    }
}
