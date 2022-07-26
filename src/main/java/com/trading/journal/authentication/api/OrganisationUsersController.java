package com.trading.journal.authentication.api;

import com.trading.journal.authentication.jwt.data.AccessToken;
import com.trading.journal.authentication.jwt.data.AccessTokenInfo;
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
public class OrganisationUsersController implements OrganisationUsersApi {

    private final UserManagementService userManagementService;

    @Override
    public ResponseEntity<PageResponse<UserInfo>> getAll(@AccessToken AccessTokenInfo accessTokenInfo, Integer page, Integer size, String[] sort, String filter) {
        PageableRequest pageableRequest = PageableRequest.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .filter(filter)
                .build();
        PageResponse<UserInfo> pageResponse = userManagementService.getAll(accessTokenInfo.tenancyId(), pageableRequest);
        return ok(pageResponse);
    }

    @Override
    public ResponseEntity<UserInfo> getById(@AccessToken AccessTokenInfo accessTokenInfo, Long id) {
        return ok(userManagementService.getUserById(accessTokenInfo.tenancyId(), id));
    }

    @Override
    public ResponseEntity<Void> disable(@AccessToken AccessTokenInfo accessTokenInfo, Long id) {
        userManagementService.disableUserById(accessTokenInfo.tenancyId(), id);
        return ok().build();
    }

    @Override
    public ResponseEntity<Void> enable(@AccessToken AccessTokenInfo accessTokenInfo, Long id) {
        userManagementService.enableUserById(accessTokenInfo.tenancyId(), id);
        return ok().build();
    }

    @Override
    public ResponseEntity<Void> delete(@AccessToken AccessTokenInfo accessTokenInfo, Long id) {
        userManagementService.deleteUserById(accessTokenInfo.tenancyId(), id);
        return ok().build();
    }

    @Override
    public ResponseEntity<List<UserAuthority>> addAuthorities(@AccessToken AccessTokenInfo accessTokenInfo, Long id, AuthoritiesChange authorities) {
        userManagementService.addAuthorities(accessTokenInfo.tenancyId(), id, authorities);
        return ok().build();
    }

    @Override
    public ResponseEntity<List<UserAuthority>> deleteAuthorities(@AccessToken AccessTokenInfo accessTokenInfo, Long id, AuthoritiesChange authorities) {
        userManagementService.deleteAuthorities(accessTokenInfo.tenancyId(), id, authorities);
        return ok().build();
    }
}
