package com.nano_electronics_cital.kheyr_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.enabled:false}")
    private boolean firebaseEnabled;

    @Value("${firebase.service-account-file:}")
    private String serviceAccountFile;

    @Value("${firebase.service-account-json:}")
    private String serviceAccountJson;

    @PostConstruct
    public void initialize() throws IOException {
        if (!firebaseEnabled) {
            log.info("Firebase is disabled. Push notifications will be skipped.");
            return;
        }

        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try (InputStream serviceAccount = openServiceAccountStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase app initialized successfully.");
        }
    }

    private InputStream openServiceAccountStream() throws IOException {
        if (StringUtils.hasText(serviceAccountJson)) {
            return new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
        }

        if (!StringUtils.hasText(serviceAccountFile)) {
            throw new FileNotFoundException(
                    "Firebase is enabled but no service account file or JSON was provided."
            );
        }

        if (serviceAccountFile.startsWith("classpath:")) {
            String resourcePath = serviceAccountFile.substring("classpath:".length());
            if (resourcePath.startsWith("/")) {
                resourcePath = resourcePath.substring(1);
            }

            InputStream inputStream = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream(resourcePath);

            if (inputStream == null) {
                throw new FileNotFoundException(
                        "Firebase file not found in classpath: " + resourcePath
                );
            }

            return inputStream;
        }

        return new FileInputStream(serviceAccountFile);
    }
}
