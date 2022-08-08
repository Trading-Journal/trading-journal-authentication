package com.trading.journal.authentication.api;

import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.pageable.PageableRequest;
import com.trading.journal.authentication.user.AuthoritiesChange;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.user.service.UserManagementService;
import com.trading.journal.authentication.userauthority.UserAuthorityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class UsersController implements UsersApi {

    private final UserManagementService userManagementService;

    @Override
    public ResponseEntity<PageResponse<UserInfo>> getAll(Long tenancy, Integer page, Integer size, String[] sort, String filter) {
        PageableRequest pageableRequest = PageableRequest.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .filter(filter)
                .build();
        PageResponse<UserInfo> pageResponse = userManagementService.getAll(tenancy, pageableRequest);
        return ok(pageResponse);
    }

    @Override
    public ResponseEntity<UserInfo> getById(Long tenancy, Long id) {
        return ok(userManagementService.getUserById(tenancy, id));
    }

    @Override
    public ResponseEntity<Void> disable(Long tenancy, Long id) {
        userManagementService.disableUserById(tenancy, id);
        return ok().build();
    }

    @Override
    public ResponseEntity<Void> enable(Long tenancy, Long id) {
        userManagementService.enableUserById(tenancy, id);
        return ok().build();
    }

    @Override
    public ResponseEntity<Void> delete(Long tenancy, Long id) {
        userManagementService.deleteUserById(tenancy, id);
        return ok().build();
    }

    @Override
    public ResponseEntity<List<UserAuthorityResponse>> addAuthorities(Long tenancy, Long id, AuthoritiesChange authorities) {
        return ok(userManagementService.addAuthorities(tenancy, id, authorities));
    }

    @Override
    public ResponseEntity<List<UserAuthorityResponse>> deleteAuthorities(Long tenancy, Long id, AuthoritiesChange authorities) {
        return ok(userManagementService.deleteAuthorities(tenancy, id, authorities));
    }
}
