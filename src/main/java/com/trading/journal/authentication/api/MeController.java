package com.trading.journal.authentication.api;

import com.trading.journal.authentication.jwt.data.AccessToken;
import com.trading.journal.authentication.jwt.data.AccessTokenInfo;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.user.service.ApplicationUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class MeController implements MeApi {

    private final ApplicationUserService applicationUserService;

    @Override
    public ResponseEntity<UserInfo> me(@AccessToken AccessTokenInfo accessTokenInfo) {
        UserInfo userInfo = applicationUserService.getUserInfo(accessTokenInfo.subject());
        return ok(userInfo);
    }

}
