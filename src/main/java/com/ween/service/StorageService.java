package com.ween.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StorageService {

    private final S3Client s3Client;

    @Value("${ween.s3.bucket-name:ween-storage}")
    private String bucketName;

    @Value("${ween.s3.base-url:http://localhost:9000}")
    private String baseUrl;

    public String uploadFile(MultipartFile file, String folder) {
        try {
            String key = generateFileKey(folder, file.getOriginalFilename());
            byte[] bytes = file.getBytes();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));

            String fileUrl = baseUrl + "/" + bucketName + "/" + key;
            log.info("File uploaded successfully: {}", fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public String uploadFileFromBytes(byte[] fileBytes, String folder, String fileName, String contentType) {
        try {
            String key = generateFileKey(folder, fileName);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));

            String fileUrl = baseUrl + "/" + bucketName + "/" + key;
            log.info("File uploaded successfully from bytes: {}", fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("Failed to upload file from bytes: {}", fileName, e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public String uploadProfilePhoto(MultipartFile file, String userId) {
        return uploadFile(file, "profile-photos/" + userId);
    }

    public String uploadCertificatePdf(byte[] pdfBytes, String certificateNumber) {
        return uploadFileFromBytes(pdfBytes, "certificates", certificateNumber + ".pdf", "application/pdf");
    }

    public String uploadEventCoverImage(MultipartFile file, String eventId) {
        return uploadFile(file, "events/" + eventId + "/cover");
    }

    public String uploadOrganizationLogo(MultipartFile file, String organizationId) {
        return uploadFile(file, "organizations/" + organizationId + "/logo");
    }

    public InputStream downloadFile(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            log.info("File downloaded: {}", fileUrl);
            return s3Client.getObject(getObjectRequest);
        } catch (Exception e) {
            log.error("Failed to download file: {}", fileUrl, e);
            throw new RuntimeException("Failed to download file", e);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            s3Client.deleteObject(builder -> builder.bucket(bucketName).key(key));
            log.info("File deleted: {}", fileUrl);
        } catch (Exception e) {
            log.error("Failed to delete file: {}", fileUrl, e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    public boolean fileExists(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            s3Client.headObject(builder -> builder.bucket(bucketName).key(key));
            return true;
        } catch (Exception e) {
            log.debug("File not found: {}", fileUrl);
            return false;
        }
    }

    private String generateFileKey(String folder, String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID() + extension;
        return folder + "/" + uniqueFileName;
    }

    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf('.');
        if (lastIndexOf == -1) {
            return "";
        }
        return fileName.substring(lastIndexOf);
    }

    private String extractKeyFromUrl(String fileUrl) {
        String bucketPrefix = bucketName + "/";
        int startIndex = fileUrl.indexOf(bucketPrefix);
        if (startIndex == -1) {
            throw new IllegalArgumentException("Invalid file URL");
        }
        return fileUrl.substring(startIndex + bucketPrefix.length());
    }
}
