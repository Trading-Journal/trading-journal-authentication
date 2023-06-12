package com.trading.journal.authentication.api;

import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.user.AuthoritiesChange;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.userauthority.UserAuthorityResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/admin/users")
public interface UsersApi {

    String TENANCY = "tenancy";

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<PageResponse<UserInfo>> getAll(
            @RequestHeader(name = TENANCY) Long tenancy,
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer size,
            @RequestParam(value = "sort", required = false) String[] sort,
            @RequestParam(value = "filter", required = false) String filter);

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<UserInfo> getById(@RequestHeader(name = TENANCY) Long tenancy, @PathVariable Long id);

    @PatchMapping("/{id}/disable")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> disable(@RequestHeader(name = TENANCY) Long tenancy, @PathVariable Long id);

    @PatchMapping("/{id}/enable")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> enable(@RequestHeader(name = TENANCY) Long tenancy, @PathVariable Long id);

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> delete(@RequestHeader(name = TENANCY) Long tenancy, @PathVariable Long id);

    @PutMapping("/{id}/authorities")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<UserAuthorityResponse>> addAuthorities(@RequestHeader(name = TENANCY) Long tenancy, @PathVariable Long id, @RequestBody AuthoritiesChange authorities);

    @DeleteMapping("/{id}/authorities")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<UserAuthorityResponse>> deleteAuthorities(@RequestHeader(name = TENANCY) Long tenancy, @PathVariable Long id, @RequestBody AuthoritiesChange authorities);
}
