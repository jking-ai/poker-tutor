package com.jkingai.pokertutor.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Firebase Admin SDK initialization.
 * Provides access to Firebase Realtime Database for game state synchronization.
 */
@Configuration
public class FirebaseConfig {

    // TODO: Initialize FirebaseApp with service account credentials
    //   - Local: load from classpath:gcp-credentials.json
    //   - Production: use default credentials (Cloud Run service account)

    // TODO: Create FirebaseDatabase bean for RTDB access

    // TODO: Configure database URL from application properties
}
