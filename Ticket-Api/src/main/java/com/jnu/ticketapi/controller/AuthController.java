package com.jnu.ticketapi.controller;


import com.jnu.ticketapi.application.port.AuthUseCase;
import com.jnu.ticketapi.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class AuthController {
    private final AuthUseCase authUseCase;

    @PostMapping("/auth/login")
    public ResponseEntity<LoginUserResponseDto> logInUser(
            @RequestBody LoginUserRequestDto loginUserRequestDto) {
        LoginUserResponseDto responseDto = authUseCase.login(loginUserRequestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/auth/reissue")
    public ResponseEntity<ReissueTokenResponseDto> reIssue(
            @RequestBody ReissueTokenRequestDto requestDto,
            @RequestHeader("Authorization") String bearerToken) {
        String accessToken = authUseCase.extractToken(bearerToken);
        authUseCase.validate(requestDto.refreshToken());
        ReissueTokenResponseDto responseDto =
                authUseCase.reissue(accessToken, requestDto.refreshToken());
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<LogoutUserResponseDto> logOut(@RequestHeader("Authorization") String bearerToken) {
        LogoutUserResponseDto responseDto = authUseCase.logout(bearerToken);
        return ResponseEntity.ok(responseDto);
    }
}
