package com.trading.journal.authentication.jwt;

import java.util.Date;

public record TokenData(
		String token,
		long expirationIn,
		Date issuedAt) {
}