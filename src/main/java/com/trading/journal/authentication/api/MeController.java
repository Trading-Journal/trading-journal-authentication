package com.trading.journal.authentication.api;

import com.trading.journal.authentication.jwt.data.AccessToken;
import com.trading.journal.authentication.jwt.data.AccessTokenInfo;
import com.trading.journal.authentication.user.ApplicationUserService;
import com.trading.journal.authentication.user.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class MeController implements MeApi {

    private final ApplicationUserService applicationUserService;

    @Override
    public Mono<ResponseEntity<UserInfo>> me(@AccessToken AccessTokenInfo accessTokenInfo) {
        return applicationUserService.getUserInfo(accessTokenInfo.userName()).map(ResponseEntity::ok);
    }

}
