package com.ayush.ems.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.client.http.FileContent;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import java.util.Collections;

import javax.annotation.PostConstruct;

@Service
public class GoogleDriveService {

    @Autowired
    private Environment env;

    @PostConstruct
    public void debugProps() {
        String[] profiles = env.getActiveProfiles();
        System.out.println("🌐 Active Profile(s): " + String.join(", ", profiles));

        System.out.println("⚙️ google.client.id = " + env.getProperty("google.client.id"));
        System.out.println("⚙️ google.client.secret = " + (env.getProperty("google.client.secret") != null ? "loaded" : "null"));
        System.out.println("⚙️ google.refresh.token = " + (env.getProperty("google.refresh.token") != null ? "loaded" : "null"));
        System.out.println("⚙️ google.app.name = " + env.getProperty("google.app.name"));
    }

    /**
     * Build Google Drive Service (with Refresh Token flow)
     */
    @SuppressWarnings("deprecation")
	protected Drive getDriveService() throws Exception {
        String clientId = env.getProperty("google.client.id");
        String clientSecret = env.getProperty("google.client.secret");
        String refreshToken = env.getProperty("google.refresh.token");
        String applicationName = env.getProperty("google.app.name", "Employee Management System");

        if (clientId == null || clientId.isBlank())
            throw new IllegalStateException("google.client.id is missing/blank");
        if (clientSecret == null || clientSecret.isBlank())
            throw new IllegalStateException("google.client.secret is missing/blank");
        if (refreshToken == null || refreshToken.isBlank())
            throw new IllegalStateException("google.refresh.token is missing/blank");

        var http = GoogleNetHttpTransport.newTrustedTransport();
        var json = JacksonFactory.getDefaultInstance();

        var creds = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken)
                .build();

        return new Drive.Builder(http, json, new HttpCredentialsAdapter(creds))
                .setApplicationName(applicationName)
                .build();
    }

    /**
     * Upload file to Google Drive
     */
    public String uploadFile(String filePath, String fileName, String folderId) throws Exception {
        System.out.println("📁 FILEPATH = " + filePath + ", FILENAME = " + fileName + ", FOLDERID = " + folderId);

        Drive drive = getDriveService();

        File meta = new File();
        meta.setName(fileName);
        if (folderId != null && !folderId.isBlank()) {
            meta.setParents(Collections.singletonList(folderId));
        }

        java.io.File local = new java.io.File(filePath);
        FileContent media = new FileContent(guessMime(fileName), local);

        File uploaded = drive.files()
                .create(meta, media)
                .setFields("id,name")
                .execute();

        System.out.println("✅ File uploaded: " + uploaded.getName() + " (ID: " + uploaded.getId() + ")");
        return uploaded.getId();
    }

    /**
     * Detect MIME type from file extension
     */
    private String guessMime(String name) {
        String n = name.toLowerCase();
        if (n.endsWith(".pdf")) return "application/pdf";
        if (n.endsWith(".jpg") || n.endsWith(".jpeg")) return "image/jpeg";
        if (n.endsWith(".png")) return "image/png";
        if (n.endsWith(".txt")) return "text/plain";
        if (n.endsWith(".doc") || n.endsWith(".docx"))
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (n.endsWith(".xls") || n.endsWith(".xlsx"))
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        return "application/octet-stream";
    }
}
