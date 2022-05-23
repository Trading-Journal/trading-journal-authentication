package com.trading.journal.authentication.jwt.impl;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import com.trading.journal.authentication.jwt.JwtException;
import com.trading.journal.authentication.jwt.PrivateKeyProvider;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

@Component
public class PrivateKeyProviderImpl implements PrivateKeyProvider {

    private final KeyProvider keyProvider;

    public PrivateKeyProviderImpl(KeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    @Override
    public Key provide(File file) throws IOException {
        return keyProvider.loadRsaKey(file,
                "PRIVATE",
                this::privateKeySpec,
                this::privateKeyGenerator);
    }

    private EncodedKeySpec privateKeySpec(String data) {
        return new PKCS8EncodedKeySpec(Base64.decodeBase64(data));
    }

    private PrivateKey privateKeyGenerator(KeyFactory kf, EncodedKeySpec spec) {
        try {
            return kf.generatePrivate(spec);
        } catch (InvalidKeySpecException e) {
            throw new JwtException(e);
        }
    }
}
