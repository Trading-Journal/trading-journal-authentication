package com.trading.journal.authentication.api;

import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.pageable.PageableRequest;
import com.trading.journal.authentication.user.AuthoritiesChange;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.user.service.UserManagementService;
import com.trading.journal.authentication.userauthority.UserAuthority;
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
    public ResponseEntity<PageResponse<UserInfo>> getAll(Integer page, Integer size, String[] sort, String filter) {
        PageableRequest pageableRequest = PageableRequest.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .filter(filter)
                .build();
        PageResponse<UserInfo> pageResponse = userManagementService.getAll(pageableRequest);
        return ok(pageResponse);
    }

    @Override
    public ResponseEntity<UserInfo> getById(Long id) {
        return ok(userManagementService.getUserById(id));
    }

    @Override
    public ResponseEntity<Void> disable(Long id) {
        userManagementService.disableUserById(id);
        return ok().build();
    }

    @Override
    public ResponseEntity<Void> enable(Long id) {
        userManagementService.enableUserById(id);
        return ok().build();
    }

    @Override
    public ResponseEntity<Void> delete(Long id) {
        userManagementService.deleteUserById(id);
        return ok().build();
    }

    @Override
    public ResponseEntity<List<UserAuthority>> addAuthorities(Long id, AuthoritiesChange authorities) {
        return ok(userManagementService.addAuthorities(id, authorities));
    }

    @Override
    public ResponseEntity<List<UserAuthority>> deleteAuthorities(Long id, AuthoritiesChange authorities) {
        return ok(userManagementService.deleteAuthorities(id, authorities));
    }
}
