package com.trading.journal.authentication.jwt;

import java.io.File;
import java.io.IOException;
import java.security.Key;

public interface PublicKeyProvider {
    Key provide(File file) throws IOException;
}
