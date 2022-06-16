package com.trading.journal.authentication.api;

import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.pageable.PageableRequest;
import com.trading.journal.authentication.user.AuthoritiesChange;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.user.service.ApplicationUserManagementService;
import com.trading.journal.authentication.userauthority.UserAuthority;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class UsersController implements UsersApi {

    private final ApplicationUserManagementService applicationUserManagementService;

    @Override
    public ResponseEntity<PageResponse<UserInfo>> getAll(Integer page, Integer size, String[] sort, String filter) {
        PageableRequest pageableRequest = PageableRequest.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .filter(filter)
                .build();
        PageResponse<UserInfo> pageResponse = applicationUserManagementService.getAll(pageableRequest);
        return ok(pageResponse);
    }

    @Override
    public ResponseEntity<UserInfo> getById(Long id) {
        return null;
    }

    @Override
    public ResponseEntity<Void> disable(Long id) {
        return null;
    }

    @Override
    public ResponseEntity<Void> enable(Long id) {
        return null;
    }

    @Override
    public ResponseEntity<Void> delete(Long id) {
        return null;
    }

    @Override
    public ResponseEntity<List<UserAuthority>> addAuthorities(Long id, AuthoritiesChange authorities) {
        return null;
    }

    @Override
    public ResponseEntity<List<UserAuthority>> deleteAuthorities(Long id, AuthoritiesChange authorities) {
        return null;
    }
}
