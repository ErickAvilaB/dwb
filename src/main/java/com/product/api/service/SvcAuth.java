package com.product.api.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.product.api.entity.AuthAccount;
import com.product.api.repository.RepoAuthAccount;
import com.product.exception.ApiException;

@Service
public class SvcAuth implements UserDetailsService {

    public static final String USERNAME_PATTERN = "[a-zA-Z0-9._-]{3,64}";
    public static final String USERNAME_MESSAGE = "El usuario debe tener entre 3 y 64 letras, números, puntos, guiones o guiones bajos";

    private final RepoAuthAccount accounts;
    private final PasswordEncoder passwordEncoder;

    public SvcAuth(RepoAuthAccount accounts, PasswordEncoder passwordEncoder) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthAccount register(String username, String password) {
        if (!username.matches(USERNAME_PATTERN)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, USERNAME_MESSAGE);
        }
        if (!isValidNewPassword(password)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener al menos 8 caracteres y como máximo 72 bytes UTF-8");
        }

        try {
            return save(username.toLowerCase(Locale.ROOT), password, AuthAccount.USER);
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "El nombre de usuario ya está registrado");
        }
    }

    @Override
    public AuthAccount loadUserByUsername(String username) {
        return accounts.findByUsername(username.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    public void provisionAdmin(String username, String password) {
        if (username.isEmpty() && password.isEmpty()) {
            return;
        }
        if (!username.matches(USERNAME_PATTERN) || !isValidNewPassword(password)) {
            throw new IllegalStateException("Configura AUTH_ADMIN_USERNAME y AUTH_ADMIN_PASSWORD con valores válidos");
        }

        String normalizedUsername = username.toLowerCase(Locale.ROOT);
        AuthAccount account = accounts.findByUsername(normalizedUsername).orElse(null);
        if (account == null) {
            try {
                save(normalizedUsername, password, AuthAccount.ADMIN);
                return;
            } catch (DataIntegrityViolationException exception) {
                // Otra instancia pudo crear la cuenta después de la consulta.
                account = accounts.findByUsername(normalizedUsername).orElseThrow(() -> exception);
            }
        }
        if (!AuthAccount.ADMIN.equals(account.getRole())) {
            throw new IllegalStateException("AUTH_ADMIN_USERNAME ya pertenece a una cuenta sin rol Admin");
        }
    }

    public static boolean exceedsBcryptLimit(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length > 72;
    }

    private boolean isValidNewPassword(String password) {
        return !password.isBlank() && password.length() >= 8 && !exceedsBcryptLimit(password);
    }

    private AuthAccount save(String username, String password, String role) {
        return accounts.saveAndFlush(new AuthAccount(username, passwordEncoder.encode(password), role));
    }
}
