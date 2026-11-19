package com.product.api.controller;

import java.util.List;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.product.api.entity.AuthAccount;
import com.product.api.service.SvcAuth;
import com.product.config.jwt.JwtUtil;
import com.product.exception.ApiException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@RestController
public class CtrlAuth {

    private final SvcAuth authService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public CtrlAuth(SvcAuth authService, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/user")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody Credentials request) {
        AuthAccount account = authService.register(request.username(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UserResponse(account.getId(), account.getUsername(), List.of(account.getRole())));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody Credentials request) {
        if (SvcAuth.exceedsBcryptLimit(request.password())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La contraseña supera los 72 bytes UTF-8");
        }

        try {
            var credentials = UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password());
            AuthAccount account = (AuthAccount) authenticationManager.authenticate(credentials).getPrincipal();
            String token = jwtUtil.issue(account.getUsername(), account.getId(), account.getRole());
            return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new TokenResponse(token));
        } catch (BadCredentialsException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
        }
    }

    public record Credentials(
            @NotBlank(message = "El usuario es obligatorio")
            @Pattern(regexp = SvcAuth.USERNAME_PATTERN, message = SvcAuth.USERNAME_MESSAGE) String username,
            @NotBlank(message = "La contraseña es obligatoria")
            @Size(max = 72, message = "La contraseña supera los 72 caracteres") String password) {
    }

    public record UserResponse(Long id, String username, List<String> roles) {
    }

    public record TokenResponse(String token) {
    }
}
