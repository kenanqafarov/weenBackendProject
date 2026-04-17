package com.ween.mapper;

import com.ween.dto.response.CertificateResponse;
import com.ween.entity.Certificate;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-17T14:02:35+0400",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class CertificateMapperImpl implements CertificateMapper {

    @Override
    public CertificateResponse toCertificateResponse(Certificate certificate) {
        if ( certificate == null ) {
            return null;
        }

        CertificateResponse.CertificateResponseBuilder certificateResponse = CertificateResponse.builder();

        certificateResponse.certificateNumber( certificate.getCertificateNumber() );
        certificateResponse.eventId( certificate.getEventId() );
        certificateResponse.id( certificate.getId() );
        certificateResponse.issuedAt( certificate.getIssuedAt() );
        certificateResponse.pdfUrl( certificate.getPdfUrl() );
        certificateResponse.templateType( certificate.getTemplateType() );
        certificateResponse.userId( certificate.getUserId() );

        return certificateResponse.build();
    }

    @Override
    public Certificate toCertificate(CertificateResponse certificateResponse) {
        if ( certificateResponse == null ) {
            return null;
        }

        Certificate.CertificateBuilder certificate = Certificate.builder();

        certificate.certificateNumber( certificateResponse.getCertificateNumber() );
        certificate.eventId( certificateResponse.getEventId() );
        certificate.issuedAt( certificateResponse.getIssuedAt() );
        certificate.pdfUrl( certificateResponse.getPdfUrl() );
        certificate.templateType( certificateResponse.getTemplateType() );
        certificate.userId( certificateResponse.getUserId() );

        return certificate.build();
    }
}
