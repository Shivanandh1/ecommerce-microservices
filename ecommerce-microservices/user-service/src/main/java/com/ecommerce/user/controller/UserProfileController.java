package com.ecommerce.user.controller;

import com.ecommerce.user.dto.*;
import com.ecommerce.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService service;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile(@RequestHeader("X-Auth-User") String email) {
        return ResponseEntity.ok(service.getProfile(email));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateProfile(
            @RequestHeader("X-Auth-User") String email,
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(service.updateProfile(email, request));
    }

    @PostMapping("/profile/addresses")
    public ResponseEntity<AddressDto> addAddress(
            @RequestHeader("X-Auth-User") String email,
            @RequestBody AddressDto addressDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addAddress(email, addressDto));
    }
}
