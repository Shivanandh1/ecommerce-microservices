package com.ecommerce.user.service;

import com.ecommerce.user.dto.*;
import com.ecommerce.user.entity.*;
import com.ecommerce.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository repository;

    public UserProfileDto getProfile(String email) {
        UserProfile profile = repository.findByEmail(email)
                .orElseGet(() -> createDefaultProfile(email));
        return toDto(profile);
    }

    @Transactional
    public UserProfileDto updateProfile(String email, UpdateProfileRequest req) {
        UserProfile profile = repository.findByEmail(email)
                .orElseGet(() -> createDefaultProfile(email));
        if (req.getFirstName() != null) profile.setFirstName(req.getFirstName());
        if (req.getLastName() != null) profile.setLastName(req.getLastName());
        if (req.getPhone() != null) profile.setPhone(req.getPhone());
        if (req.getAvatarUrl() != null) profile.setAvatarUrl(req.getAvatarUrl());
        return toDto(repository.save(profile));
    }

    @Transactional
    public AddressDto addAddress(String email, AddressDto req) {
        UserProfile profile = repository.findByEmail(email)
                .orElseGet(() -> createDefaultProfile(email));
        Address address = Address.builder()
                .street(req.getStreet()).city(req.getCity())
                .state(req.getState()).zipCode(req.getZipCode())
                .country(req.getCountry()).isDefault(req.isDefault())
                .userProfile(profile).build();
        profile.getAddresses().add(address);
        repository.save(profile);
        return req;
    }

    private UserProfile createDefaultProfile(String email) {
        return repository.save(UserProfile.builder().email(email).build());
    }

    private UserProfileDto toDto(UserProfile p) {
        List<AddressDto> addresses = p.getAddresses().stream().map(a ->
                AddressDto.builder().id(a.getId()).street(a.getStreet())
                        .city(a.getCity()).state(a.getState())
                        .zipCode(a.getZipCode()).country(a.getCountry())
                        .isDefault(a.isDefault()).build()
        ).collect(Collectors.toList());
        return UserProfileDto.builder()
                .id(p.getId()).email(p.getEmail())
                .firstName(p.getFirstName()).lastName(p.getLastName())
                .phone(p.getPhone()).avatarUrl(p.getAvatarUrl())
                .addresses(addresses).createdAt(p.getCreatedAt()).build();
    }
}
