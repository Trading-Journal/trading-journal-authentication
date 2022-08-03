package com.trading.journal.authentication.api;

import com.trading.journal.authentication.jwt.data.AccessTokenInfo;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.user.service.UserManagementService;
import com.trading.journal.authentication.user.service.UserService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class MeController implements MeApi {

    private final UserService userService;

    private final UserManagementService userManagementService;

    @Timed(value = "get_me_info", description = "Amount of time to retrieve user information")
    @Override
    public ResponseEntity<UserInfo> me(AccessTokenInfo accessTokenInfo) {
        User user = userService.getUserByEmail(accessTokenInfo.subject())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return ok(new UserInfo(user));
    }

    @Override
    public ResponseEntity<Void> delete(AccessTokenInfo accessTokenInfo) {
        userManagementService.deleteMeRequest(accessTokenInfo.tenancyId(), accessTokenInfo.subject());
        return ok().build();
    }

    @Override
    public ResponseEntity<Void> verify(AccessTokenInfo accessTokenInfo, String hash) {
        userManagementService.deleteMe(accessTokenInfo.tenancyId(), accessTokenInfo.subject(), hash);
        return ok().build();
    }

}
