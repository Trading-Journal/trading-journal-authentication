package com.trading.journal.authentication.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.AuthoritiesChange;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.userauthority.UserAuthorityResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/organisation/users")
public interface OrganisationUsersApi extends PageableApi<UserInfo> {

    @PostMapping()
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<UserInfo> create(AccessTokenInfo accessTokenInfo, @RequestBody UserRegistration userRegistration);

    @PatchMapping("/{id}/disable")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> disable(AccessTokenInfo accessTokenInfo, @PathVariable Long id);

    @PatchMapping("/{id}/enable")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> enable(AccessTokenInfo accessTokenInfo, @PathVariable Long id);

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> delete(AccessTokenInfo accessTokenInfo, @PathVariable Long id);

    @PutMapping("/{id}/authorities")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<UserAuthorityResponse>> addAuthorities(AccessTokenInfo accessTokenInfo, @PathVariable Long id, @RequestBody AuthoritiesChange authorities);

    @DeleteMapping("/{id}/authorities")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<UserAuthorityResponse>> deleteAuthorities(AccessTokenInfo accessTokenInfo, @PathVariable Long id, @RequestBody AuthoritiesChange authorities);
}
