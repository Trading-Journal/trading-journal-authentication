package com.trading.journal.authentication.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.authentication.user.MeUpdate;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.user.service.UserManagementService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class MeController implements MeApi {

    private final UserManagementService userManagementService;

    @Timed(value = "get_me_info", description = "Amount of time to retrieve user information")
    @Override
    public ResponseEntity<UserInfo> me(AccessTokenInfo accessTokenInfo) {
        return ok(userManagementService.getUserByEmail(accessTokenInfo.tenancyId(), accessTokenInfo.subject()));
    }

    @Override
    public ResponseEntity<Void> deleteRequest(AccessTokenInfo accessTokenInfo) {
        userManagementService.deleteMeRequest(accessTokenInfo.tenancyId(), accessTokenInfo.subject());
        return ok().build();
    }

    @Override
    public ResponseEntity<Void> delete(AccessTokenInfo accessTokenInfo, String hash) {
        userManagementService.deleteMe(accessTokenInfo.tenancyId(), accessTokenInfo.subject(), hash);
        return ok().build();
    }

    @Override
    public ResponseEntity<UserInfo> update(AccessTokenInfo accessTokenInfo, @Valid MeUpdate meUpdate) {
        return ok(userManagementService.update(accessTokenInfo.tenancyId(), accessTokenInfo.subject(), meUpdate));
    }

}
