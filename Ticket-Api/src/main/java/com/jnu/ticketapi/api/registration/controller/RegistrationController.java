package com.jnu.ticketapi.api.registration.controller;


import com.jnu.ticketapi.api.registration.model.request.FinalSaveRequest;
import com.jnu.ticketapi.api.registration.model.request.TemporarySaveRequest;
import com.jnu.ticketapi.api.registration.model.response.FinalSaveResponse;
import com.jnu.ticketapi.api.registration.model.response.GetRegistrationResponse;
import com.jnu.ticketapi.api.registration.model.response.TemporarySaveResponse;
import com.jnu.ticketapi.api.user.service.UserUseCase;
import com.jnu.ticketapi.application.port.RegistrationUseCase;
import com.jnu.ticketapi.common.aop.GetEmail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "access-token")
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "4. [신청]")
public class RegistrationController {
    private final RegistrationUseCase registrationUseCase;
    private final UserUseCase userUseCase;

    @Operation(
            summary = "임시 저장 조회",
            description = "임시 저장 했던 정보를 조회(임시 저장을 하지 않은 유저는 Email, Sector 빼고 null 반환)")
    @GetMapping("/registration")
    public ResponseEntity<GetRegistrationResponse> getRegistration(@GetEmail String email) {
        GetRegistrationResponse responseDto = registrationUseCase.getRegistration(email);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "임시 저장", description = "임시 저장")
    @PostMapping("/registration/false")
    public ResponseEntity<TemporarySaveResponse> temporarySave(
            @RequestBody TemporarySaveRequest requestDto) {
        TemporarySaveResponse responseDto = registrationUseCase.temporarySave(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "1차 신청", description = "1차 신청")
    @PostMapping("/registration/true")
    public ResponseEntity<FinalSaveResponse> finalSave(@RequestBody FinalSaveRequest requestDto) {
        FinalSaveResponse responseDto = registrationUseCase.finalSave(requestDto);
        return ResponseEntity.ok(responseDto);
    }
}
