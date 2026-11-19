package com.product.config.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import com.product.api.controller.CtrlAuth;
import com.product.api.controller.CtrlCategory;
import com.product.api.entity.AuthAccount;
import com.product.api.repository.RepoAuthAccount;
import com.product.api.service.SvcAuth;
import com.product.api.service.SvcCategory;
import com.product.config.jwt.JwtUtil;

@WebMvcTest(controllers = {CtrlAuth.class, CtrlCategory.class}, properties = "app.jwt.secret=")
@Import({SecurityConfig.class, AuthConfig.class, CorsConfig.class, JwtUtil.class, SvcAuth.class})
@ImportAutoConfiguration({ServletWebSecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
class ProductLoginTest {

    private static final String USERNAME = "cliente-prueba";
    private static final String PASSWORD = "Cliente-prueba-123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private RepoAuthAccount accounts;

    @MockitoBean
    private SvcCategory categories;

    @Test
    void registersAndLogsInWithAHashedPasswordAndNormalUserRole() throws Exception {
        when(accounts.saveAndFlush(any(AuthAccount.class))).thenAnswer(invocation -> {
            AuthAccount account = invocation.getArgument(0);
            ReflectionTestUtils.setField(account, "id", 1L);
            return account;
        });

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s","role":"Admin","roles":["Admin"]}
                                """.formatted(USERNAME, PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.roles[0]").value("User"))
                .andExpect(jsonPath("$.password").doesNotExist());

        ArgumentCaptor<AuthAccount> savedAccount = ArgumentCaptor.forClass(AuthAccount.class);
        verify(accounts).saveAndFlush(savedAccount.capture());
        AuthAccount account = savedAccount.getValue();
        assertThat(account.getPassword()).isNotEqualTo(PASSWORD);
        assertThat(passwordEncoder.matches(PASSWORD, account.getPassword())).isTrue();
        when(accounts.findByUsername(USERNAME)).thenReturn(Optional.of(account));

        String token = loginAs(USERNAME, PASSWORD);
        mockMvc.perform(get("/category/active").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/category").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/category/active")).andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatesAnExistingAdminWithItsStoredRole() throws Exception {
        AuthAccount account = account(USERNAME, "Admin");
        when(accounts.findByUsername(USERNAME)).thenReturn(Optional.of(account));

        String token = loginAs(USERNAME, PASSWORD);
        mockMvc.perform(get("/category").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({"cliente-prueba, Clave-incorrecta-123!", "cuenta-inexistente, Cliente-prueba-123!"})
    void rejectsIncorrectCredentials(String username, String password) throws Exception {
        when(accounts.findByUsername(USERNAME)).thenReturn(Optional.of(account(USERNAME, "User")));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(username, password)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void rejectsIncompleteCredentials() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cliente-prueba\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsDuplicateUsernames() throws Exception {
        when(accounts.saveAndFlush(any(AuthAccount.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate username"));

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(USERNAME, PASSWORD)))
                .andExpect(status().isConflict());
    }

    private AuthAccount account(String username, String role) {
        AuthAccount account = new AuthAccount(username, passwordEncoder.encode(PASSWORD), role);
        ReflectionTestUtils.setField(account, "id", 1L);
        return account;
    }

    private String loginAs(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(username, password)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(result -> assertThat(result.getRequest().getSession(false)).isNull())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }

    private String credentials(String username, String password) {
        return "{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password);
    }
}
