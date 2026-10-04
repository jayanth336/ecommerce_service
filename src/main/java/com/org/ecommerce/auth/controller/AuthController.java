package com.org.ecommerce.auth.controller;

import com.org.ecommerce.auth.dto.LoginRequestDto;
import com.org.ecommerce.auth.dto.LoginResponseDto;
import com.org.ecommerce.auth.dto.LogoutRequestDto;
import com.org.ecommerce.auth.dto.RefreshTokenRequestDto;
import com.org.ecommerce.auth.service.AuthService;
import com.org.ecommerce.user.dto.UserRequestDto;
import com.org.ecommerce.user.dto.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(
        name = "Authentication",
        description = "User registration and login APIs"
)
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Register a new user",
            description = "Creates a new customer account"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
    })
    @PostMapping("/register")
    private ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody UserRequestDto requestDto) {
        UserResponseDto responseDto = authService.registerUser(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto); // You are creating a new User here.
    }

    @Operation(
            summary = "Login",
            description = "Authenticates the user and returns a JWT access token"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping("/login")
    private ResponseEntity<LoginResponseDto> loginUser(@Valid @RequestBody LoginRequestDto requestDto) {
        LoginResponseDto responseDto = authService.loginUser(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto); // You are not creating anything here.
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto requestDto) {
        LoginResponseDto responseDto = authService.refreshToken(requestDto.getRefreshToken());
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequestDto requestDto) {
        authService.logout(requestDto.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
