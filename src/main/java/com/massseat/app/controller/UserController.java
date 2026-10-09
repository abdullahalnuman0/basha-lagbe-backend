package com.massseat.app.controller;

import com.massseat.app.dto.user.FcmTokenRequest;
import com.massseat.app.security.UserPrincipal;
import com.massseat.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Profile management and public profiles")
public class UserController {

    private final UserService userService;

    @PutMapping("/me/fcm-token")
    @Operation(summary = "Register this device's FCM token for push notifications")
    public Map<String, String> updateFcmToken(@AuthenticationPrincipal UserPrincipal principal,
                                              @Valid @RequestBody FcmTokenRequest request) {
        userService.updateFcmToken(principal.getId(), request.getToken());
        return Map.of("message", "FCM token saved");
    }

    @PostMapping("/me/heartbeat")
    @Operation(summary = "In-app presence heartbeat (suppresses push while the user is active in-app)")
    public Map<String, String> heartbeat(@AuthenticationPrincipal UserPrincipal principal) {
        userService.heartbeat(principal.getId());
        return Map.of("message", "ok");
    }

    @PostMapping("/me/test-push")
    @Operation(summary = "Send a test push to this device to verify the notification pipeline")
    public Map<String, String> testPush(@AuthenticationPrincipal UserPrincipal principal) {
        return Map.of("message", userService.sendTestPush(principal.getId()));
    }



}
