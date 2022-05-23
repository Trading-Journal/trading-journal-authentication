package com.trading.journal.authentication.jwt.impl;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import com.trading.journal.authentication.jwt.JwtException;
import com.trading.journal.authentication.jwt.PublicKeyProvider;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

@Component
public class PublicKeyProviderImpl implements PublicKeyProvider {

    private final KeyProvider keyProvider;

    public PublicKeyProviderImpl(KeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    @Override
    public Key provide(File file) throws IOException {
        return keyProvider.loadRsaKey(file,
                "PUBLIC",
                this::publicKeySpec,
                this::publicKeyGenerator);
    }

    private EncodedKeySpec publicKeySpec(String data) {
        return new X509EncodedKeySpec(Base64.decodeBase64(data));
    }

    private PublicKey publicKeyGenerator(KeyFactory kf, EncodedKeySpec spec) {
        try {
            return kf.generatePublic(spec);
        } catch (InvalidKeySpecException e) {
            throw new JwtException(e);
        }
    }
}
