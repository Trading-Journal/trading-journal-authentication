package com.trading.journal.authentication.authority;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public enum AuthoritiesHelper {

    ROLE_USER("ROLE_USER", AuthorityCategory.COMMON_USER),
    ROLE_ADMIN("ROLE_ADMIN", AuthorityCategory.ADMINISTRATOR);

    private final String label;

    private final AuthorityCategory category;

    public static List<AuthoritiesHelper> getByCategory(AuthorityCategory category) {
        return Arrays.stream(AuthoritiesHelper.values())
                .filter(authoritiesHelper -> category.equals(authoritiesHelper.getCategory()))
                .collect(Collectors.toList());
    }

    public static AuthoritiesHelper getByName(String name) {
        AuthoritiesHelper authority = null;
        if (Arrays.stream(AuthoritiesHelper.values()).anyMatch(authoritiesHelper -> authoritiesHelper.getLabel().equals(name))) {
            authority = AuthoritiesHelper.valueOf(name);
        }
        return authority;
    }
}
