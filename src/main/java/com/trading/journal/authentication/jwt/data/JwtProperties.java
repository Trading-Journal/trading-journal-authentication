package com.trading.journal.authentication.jwt.data;

import java.io.File;

import javax.annotation.PostConstruct;

import com.trading.journal.authentication.jwt.JwtException;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("app.jwt")
@Configuration
public class JwtProperties {

    private ServiceType serviceType;
    private File privateKey;
    private File publicKey;
    private long expiration;

    @PostConstruct
    public void init() {
        if (ServiceType.PROVIDER.equals(serviceType)) {
            if (this.privateKey == null || this.publicKey == null) {
                throw new JwtException("For provider service type, both privateKey and publicKey must be provided");
            }
        } else if (this.publicKey == null) {
            throw new JwtException("For resource service type, publicKey must be provided");
        }
        if (this.expiration <= 0) {
            this.expiration = 3600;
        }
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public File getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(File privateKey) {
        this.privateKey = privateKey;
    }

    public File getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(File publicKey) {
        this.publicKey = publicKey;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
}
