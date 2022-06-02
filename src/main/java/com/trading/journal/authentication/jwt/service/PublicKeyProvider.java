package com.trading.journal.authentication.jwt.service;

import java.io.File;
import java.io.IOException;
import java.security.Key;

public interface PublicKeyProvider {
    Key provide(File file) throws IOException;
}
