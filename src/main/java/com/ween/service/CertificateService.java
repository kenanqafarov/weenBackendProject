package com.ween.service;

// iText imports disabled - PDF generation temporarily disabled
// import com.itextpdf.io.font.constants.StandardFonts;
// import com.itextpdf.kernel.colors.ColorConstants;
// import com.itextpdf.kernel.font.PdfFont;
// import com.itextpdf.kernel.font.PdfFontFactory;
// import com.itextpdf.kernel.geom.PageSize;
// import com.itextpdf.kernel.pdf.PdfDocument;
// import com.itextpdf.kernel.pdf.PdfWriter;
// import com.itextpdf.layout.Document;
// import com.itextpdf.layout.element.Paragraph;
// import com.itextpdf.layout.properties.TextAlignment;

import com.ween.entity.Certificate;
import com.ween.entity.Event;
import com.ween.entity.User;
import com.ween.enums.CertificateTemplate;
import com.ween.exception.ResourceNotFoundException;
import com.ween.repository.CertificateRepository;
import com.ween.repository.EventRepository;
import com.ween.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CoinService coinService;
    private final NotificationService notificationService;
    // private final FirebaseService firebaseService; // DISABLED

    @Transactional
    public Certificate generateCertificate(String userId, String eventId, CertificateTemplate template) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        // Check if certificate already exists
        if (certificateRepository.existsByUserIdAndEventId(userId, eventId)) {
            throw new IllegalArgumentException("Certificate already exists for this user and event");
        }

        // Generate certificate number
        String certificateNumber = generateCertificateNumber();

        // PDF generation disabled - iText dependency commented out
        /* 
        byte[] pdfBytes = generateCertificatePdf(user, event, certificateNumber, template);
        String pdfUrl = storageService.uploadCertificatePdf(pdfBytes, certificateNumber);
        */
        String pdfUrl = "https://placeholder-certificate-url.local/" + certificateNumber + ".pdf";

        // Create certificate record
        Certificate certificate = Certificate.builder()
                .userId(userId)
                .eventId(eventId)
                .certificateNumber(certificateNumber)
                .pdfUrl(pdfUrl)
                .templateType(template)
                .issuedAt(LocalDateTime.now())
                .build();

        Certificate saved = certificateRepository.save(certificate);
        log.info("Certificate generated: {} for user: {} and event: {}", certificateNumber, userId, eventId);

        // Award certificate coins
        try {
            coinService.awardCertificateBonus(userId, saved.getId());
        } catch (Exception e) {
            log.warn("Failed to award certificate coins", e);
        }

        // Send notification
        try {
            notificationService.createCertificateNotification(userId, event.getTitle(), certificateNumber);
        } catch (Exception e) {
            log.warn("Failed to create certificate notification", e);
        }

        return saved;
    }


    public List<Certificate> getUserCertificates(String userId) {
        return certificateRepository.findByUserId(userId);
    }


    public boolean verifyCertificate(String certificateNumber) {
        return certificateRepository.findByCertificateNumber(certificateNumber).isPresent();
    }


    // iText-based PDF generation disabled - dependency commented out
    /*
    private byte[] generateCertificatePdf(User user, Event event, String certificateNumber, CertificateTemplate template) {
        // ... iText PDF generation code disabled ...
    }

    private void addGeneralCertificateContent(Document document, User user, Event event, String certificateNumber) throws Exception {
        // ... iText content generation disabled ...
    }

    private void addInternationalCertificateContent(Document document, User user, Event event, String certificateNumber) throws Exception {
        // ... iText content generation disabled ...
    }

    private void addSeminarCertificateContent(Document document, User user, Event event, String certificateNumber) throws Exception {
        // ... iText content generation disabled ...
    }
    */

    /*
    private void addGeneralCertificateContent(Document document, User user, Event event, String certificateNumber) throws Exception {
        // ... iText code disabled ...
    }

    private void addInternationalCertificateContent(Document document, User user, Event event, String certificateNumber) throws Exception {
        // ... iText code disabled ...
    }

    private void addSeminarCertificateContent(Document document, User user, Event event, String certificateNumber) throws Exception {
        // ... iText code disabled ...
    }
    */

    private String generateCertificateNumber() {
        return "CERT-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }


    public byte[] downloadCertificatePdf(String userId, String id) {
        return new byte[0];
    }

    public String generateCertificatesAsync(String userId, String eventId) {
        return userId;
    }
}
