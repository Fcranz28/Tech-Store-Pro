package com.techstorepro.techstorepro.auth;

public record AuthResponse(String token, String tokenType, long expiresIn, UserResponse user) {}
