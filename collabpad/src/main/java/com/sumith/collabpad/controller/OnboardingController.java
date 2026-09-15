package com.sumith.collabpad.controller;

import com.sumith.collabpad.dto.EnrollmentRequest;
import com.sumith.collabpad.service.OnboardingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping("/enroll")
    public ResponseEntity<?> enrollCompany(@AuthenticationPrincipal Jwt jwt, @RequestBody EnrollmentRequest request) {
        try {
            onboardingService.enrollCompany(jwt, request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to enroll company: " + e.getMessage());
        }
    }
}
