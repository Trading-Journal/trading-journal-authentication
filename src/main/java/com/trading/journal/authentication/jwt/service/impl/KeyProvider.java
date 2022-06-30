package com.trading.journal.authentication.jwt.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.EncodedKeySpec;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.trading.journal.authentication.jwt.JwtException;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class KeyProvider {
    public <T extends Key> T loadRsaKey(File file, String keyType, Function<String, EncodedKeySpec> keySpec,
            BiFunction<KeyFactory, EncodedKeySpec, T> keyGenerator) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[10];
            while (reader.read(buffer) != -1) {
                content.append(new String(buffer));
                buffer = new char[10];
            }
            reader.close();

            String keyString = content.toString();
            keyString = keyString.replace("-----BEGIN " + keyType + " KEY-----", "");
            keyString = keyString.replace("-----END " + keyType + " KEY-----", "");
            keyString = keyString.replaceAll("\\s+", "");

            return keyGenerator.apply(KeyFactory.getInstance("RSA"), keySpec.apply(keyString));
        } catch (NoSuchAlgorithmException e) {
            throw new JwtException(String.format("Failed to load RSA %s key", keyType), e);
        }
    }
}
