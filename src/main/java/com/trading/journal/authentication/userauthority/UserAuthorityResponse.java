package com.trading.journal.authentication.userauthority;

import com.trading.journal.authentication.authority.AuthorityCategory;

public record UserAuthorityResponse(Long id, String name, AuthorityCategory category) {

}
