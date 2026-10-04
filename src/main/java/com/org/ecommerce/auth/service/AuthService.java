package com.org.ecommerce.auth.service;

import com.org.ecommerce.auth.dto.LoginRequestDto;
import com.org.ecommerce.auth.dto.LoginResponseDto;
import com.org.ecommerce.auth.exception.InvalidCredentialsException;
import com.org.ecommerce.common.enums.Role;
import com.org.ecommerce.common.security.JwtUtil;
import com.org.ecommerce.user.dto.UserRequestDto;
import com.org.ecommerce.user.dto.UserResponseDto;
import com.org.ecommerce.user.entity.User;
import com.org.ecommerce.user.exception.UserNotFoundException;
import com.org.ecommerce.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                       RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    public UserResponseDto registerUser(UserRequestDto requestDto) {
        // CREATE A NEW USER AND SET THE DETAILS
        User newUser = new User();
        newUser.setName(requestDto.getName());
        newUser.setEmail(requestDto.getEmail());

        String password = requestDto.getPassword();
        String encodedPassword = passwordEncoder.encode(password);

        newUser.setPassword(encodedPassword);

        // ROLE IS USER BY DEFAULT
        newUser.setRole(Role.USER);

        // SAVE THE USER
        User savedUser = userRepository.save(newUser);

        // RETURN THE USER_RESPONSE_DTO
        return mapToResponseDto(savedUser);
    }

    public LoginResponseDto loginUser(LoginRequestDto requestDto) {
        // GET THE USER
        String emailId = requestDto.getEmail();
        User user = userRepository.findByEmail(emailId).orElseThrow(() -> new UserNotFoundException(emailId));
        Role role = user.getRole();

        // CHECK WHETHER THE PASSWORD MATCHES
        boolean match = passwordEncoder.matches(requestDto.getPassword(), user.getPassword());
        if(match==false) throw new InvalidCredentialsException();

        LoginResponseDto responseDto = new LoginResponseDto();

        // GENERATE BOTH THE TOKENS
        String accessToken = jwtUtil.generateAccessToken(emailId, role);
        String refreshToken = refreshTokenService.generateRefreshToken();

        //STORE REFRESH TOKEN IN REDIS
        refreshTokenService.storeRefreshToken(refreshToken, user.getId());

        responseDto.setAccessToken(accessToken);
        responseDto.setRefreshToken(refreshToken);

        return responseDto;
    }

    public LoginResponseDto refreshToken(String refreshToken) {
        // GET USER_ID FOR THIS TOKEN
        Long userId = refreshTokenService.getUserId(refreshToken);

        // GET USER FROM DATABASE
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // GENERATE BOTH THE TOKENS
        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole());
        String newRefreshToken = refreshTokenService.generateRefreshToken();

        // DELETE THE OLD REFRESH TOKEN AND STORE THE NEW ONE IN REDIS
        refreshTokenService.deleteRefreshToken(refreshToken);
        refreshTokenService.storeRefreshToken(newRefreshToken, userId);

        LoginResponseDto responseDto = new LoginResponseDto();
        responseDto.setAccessToken(newAccessToken);
        responseDto.setRefreshToken(newRefreshToken);

        return responseDto;
    }

    public void logout(String refreshToken) {
        refreshTokenService.deleteRefreshToken(refreshToken);
    }

    private UserResponseDto mapToResponseDto(User user) {
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(user.getId());
        userResponseDto.setName(user.getName());
        userResponseDto.setEmail(user.getEmail());
        userResponseDto.setRole(user.getRole().name());
        return userResponseDto;
    }
}
