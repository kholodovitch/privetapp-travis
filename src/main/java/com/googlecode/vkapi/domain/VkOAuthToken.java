package com.googlecode.vkapi.domain;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Immutable implementation of {@link OAuthToken} for vk.com
 * 
 * @author Alexey Grigorev
 */
public class VkOAuthToken implements OAuthToken {

    private final String accessToken;
    private final Date expirationMoment;

    public VkOAuthToken(String accessToken, int expiresIn) {
        this.accessToken = accessToken;
        this.expirationMoment = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiresIn));
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public boolean isExpired() {
        return System.currentTimeMillis() > expirationMoment.getTime();
    }
    
    @Override
    public Date getExpirationMoment() {
        return new Date(expirationMoment.getTime());
    }

    @Override
    public String toString() {
        return "VkOAuthToken [accessToken=" + accessToken + ", expirationMoment=" + expirationMoment + "]";
    }

}
