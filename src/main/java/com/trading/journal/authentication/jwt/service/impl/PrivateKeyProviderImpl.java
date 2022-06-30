package com.trading.journal.authentication.jwt.service.impl;

import com.trading.journal.authentication.jwt.service.PrivateKeyProvider;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

@Component
@RequiredArgsConstructor
public class PrivateKeyProviderImpl implements PrivateKeyProvider {

    private final KeyProvider keyProvider;

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

    @SneakyThrows
    private PrivateKey privateKeyGenerator(KeyFactory kf, EncodedKeySpec spec) {
        return kf.generatePrivate(spec);
    }
}
