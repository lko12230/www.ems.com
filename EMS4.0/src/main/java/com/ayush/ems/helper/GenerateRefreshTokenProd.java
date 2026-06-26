package com.ayush.ems.helper;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.util.Collections;
import java.util.Scanner;

public class GenerateRefreshTokenProd {

    // ✅ Google OAuth Client ID & Secret
    private static final String CLIENT_ID = "390041736640-ss22vqk3pil3a9s24sap6l51nrr8a89c.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";

    // ✅ Only Production Redirect URI (Railway)
    private static final String PROD_REDIRECT_URI = "https://wwwemscom-production.up.railway.app/login/oauth2/code/google";

    public static void main(String[] args) throws Exception {
        var http = GoogleNetHttpTransport.newTrustedTransport();
        var json = JacksonFactory.getDefaultInstance();

        // ✅ Always use PROD URI
        String redirectUri = PROD_REDIRECT_URI;

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                http, json, CLIENT_ID, CLIENT_SECRET,
                Collections.singleton("https://www.googleapis.com/auth/drive.file"))
                .setAccessType("offline")   // refresh token ke liye
                .setApprovalPrompt("force") // har baar refresh token milega
                .build();

        // 🔗 Step 1: Print consent screen URL
        String url = new GoogleAuthorizationCodeRequestUrl(CLIENT_ID, redirectUri,
                Collections.singleton("https://www.googleapis.com/auth/drive.file"))
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();

        System.out.println("👉 Open this URL in your browser and allow access: ");
        System.out.println(url);

        // 🔑 Step 2: User pastes code here
        System.out.print("Enter authorization code: ");
        String code = new Scanner(System.in).nextLine();

        // 🔄 Step 3: Exchange code for tokens
        GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
                .setRedirectUri(redirectUri)
                .execute();

        System.out.println("✅ Redirect URI used: " + redirectUri);
        System.out.println("✅ Your REFRESH TOKEN: " + tokenResponse.getRefreshToken());
        System.out.println("✅ Your ACCESS TOKEN: " + tokenResponse.getAccessToken());
    }
}
