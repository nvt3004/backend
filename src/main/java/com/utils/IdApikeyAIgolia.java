package com.utils;

import org.springframework.stereotype.Component;

@Component
public class IdApikeyAIgolia {

    private final String applicationId;
    private final String adminApiKey;

    public IdApikeyAIgolia() {
        this.applicationId = "PRQT2I1OB2";
        this.adminApiKey = "c9b038d452cc9b1937cf572c7deec5ff";
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getAdminApiKey() {
        return adminApiKey;
    }
}
