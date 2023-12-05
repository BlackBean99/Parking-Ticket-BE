package com.jnu.ticketapi.api.user.controller;


import com.jnu.ticketapi.api.user.model.request.UpdateRoleRequest;
import com.jnu.ticketapi.api.user.model.response.UpdateRoleResponse;
import com.jnu.ticketapi.api.user.service.UserUseCase;
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
@Tag(name = "5. [유저]")
public class UserController {
    private final UserUseCase userUseCase;

    @Operation(summary = "권한 설정", description = "사용자의 권한을 설정(ADMIN인 유저만 권한 설정을 할 수 있음)")
    @PutMapping("/admin/role/{userId}")
    public ResponseEntity<UpdateRoleResponse> updateRole(
            @PathVariable("userId") Long userId, @RequestBody UpdateRoleRequest request) {
        UpdateRoleResponse response = userUseCase.updateRole(userId, request.role());
        return ResponseEntity.ok(response);
    }
}