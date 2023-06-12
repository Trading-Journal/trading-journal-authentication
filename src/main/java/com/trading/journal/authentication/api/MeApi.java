package com.trading.journal.authentication.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.authentication.user.MeUpdate;
import com.trading.journal.authentication.user.UserInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/me")
public interface MeApi {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<UserInfo> me(AccessTokenInfo accessTokenInfo);

    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> deleteRequest(AccessTokenInfo accessTokenInfo);

    @DeleteMapping("/delete")
    ResponseEntity<Void> delete(AccessTokenInfo accessTokenInfo, @RequestParam("hash") String hash);

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<UserInfo> update(AccessTokenInfo accessTokenInfo, @RequestBody MeUpdate meUpdate);
}
