package com.product.config.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.product.api.controller.CtrlCategory;
import com.product.api.controller.CtrlProduct;
import com.product.api.controller.CtrlProductImage;
import com.product.api.service.SvcCategory;
import com.product.api.service.SvcProduct;
import com.product.api.service.SvcProductImage;
import com.product.config.jwt.JwtUtil;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@WebMvcTest(
        controllers = {CtrlCategory.class, CtrlProduct.class, CtrlProductImage.class},
        properties = {
                "app.jwt.secret=" + ProductSecurityTest.TEST_SIGNING_SECRET,
                "app.cors.allowed-origins=" + ProductSecurityTest.FRONTEND_ORIGIN
        })
@Import({SecurityConfig.class, JwtUtil.class, CorsConfig.class})
@ImportAutoConfiguration({ServletWebSecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
class ProductSecurityTest {

    static final String TEST_SIGNING_SECRET = "only-for-tests-representing-a-hex-encoded-jwt-signing-key-123456";
    static final String FRONTEND_ORIGIN = "http://localhost:4200";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private SvcCategory categoryService;

    @MockitoBean
    private SvcProduct productService;

    @MockitoBean
    private SvcProductImage imageService;

    @ParameterizedTest
    @CsvSource({
            "User, /category/active, findActive",
            "Customer, /product/1, getProduct",
            "User, /product/1/image, getProductImages"
    })
    void customerCanReadActiveCategoriesAndProductDetails(String role, String path, String controllerMethod)
            throws Exception {
        mockMvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, authorizationFor(role)))
                .andExpect(status().isOk())
                .andExpect(handler().methodName(controllerMethod));
    }

    @ParameterizedTest
    @CsvSource({
            "GET, /category",
            "GET, /category/1/childs",
            "GET, /product",
            "PATCH, /product/1/enable",
            "DELETE, /product/1/image/2"
    })
    void catalogAdministrationRequiresAdmin(String method, String path) throws Exception {
        mockMvc.perform(request(HttpMethod.valueOf(method), path)
                        .header(HttpHeaders.AUTHORIZATION, authorizationFor("User")))
                .andExpect(status().isForbidden());

        mockMvc.perform(request(HttpMethod.valueOf(method), path)
                        .header(HttpHeaders.AUTHORIZATION, authorizationFor("Admin")))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsMissingAndInvalidTokens() throws Exception {
        String path = "/category/active";
        List<String> roles = List.of("User");
        String expiredToken = signedAuthorization(roles, Instant.now().minusSeconds(600), TEST_SIGNING_SECRET);
        String incorrectlySignedToken = signedAuthorization(roles, Instant.now().plusSeconds(300),
                "independent-signing-key-only-for-security-tests");
        String tokenWithoutExpiration = signedAuthorization(
                Jwts.builder().subject("test@example.com").claim("roles", roles), TEST_SIGNING_SECRET);

        mockMvc.perform(get(path))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"));
        mockMvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, "Bearer invalid"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, expiredToken))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, incorrectlySignedToken))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, tokenWithoutExpiration))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUserWithoutAnAllowedRoleIsForbidden() throws Exception {
        mockMvc.perform(get("/category/active")
                        .header(HttpHeaders.AUTHORIZATION, authorizationFor("Other")))
                .andExpect(status().isForbidden());
    }

    @Test
    void acceptsAdministratorRoleFromCourseAuth() throws Exception {
        mockMvc.perform(get("/category")
                        .header(HttpHeaders.AUTHORIZATION, authorizationFor("Administrator")))
                .andExpect(status().isOk());
    }

    @Test
    void issuesTokensVerifiableWithTheConfiguredKey() {
        String token = jwtUtil.issue("cliente-prueba", 1L, "User");
        var key = Keys.hmacShaKeyFor(TEST_SIGNING_SECRET.getBytes(StandardCharsets.UTF_8));
        var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

        assertThat(claims.getSubject()).isEqualTo("cliente-prueba");
        assertThat(claims.get("id", Long.class)).isEqualTo(1L);
        assertThat(claims.get("roles")).isEqualTo(List.of("User"));
    }

    @Test
    void acceptsAuthorityObjectsAndAuthenticatesOnlyTheCurrentRequest() throws Exception {
        String adminAuthorization = signedAuthorization(List.of(Map.of("authority", "Admin")),
                Instant.now().plusSeconds(300), TEST_SIGNING_SECRET);

        mockMvc.perform(get("/category/active").header(HttpHeaders.AUTHORIZATION, adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getRequest().getSession(false)).isNull());

        mockMvc.perform(get("/category/active")).andExpect(status().isUnauthorized());
    }

    @Test
    void configuredFrontendCanPreflightWithAuthorizationHeader() throws Exception {
        mockMvc.perform(options("/product/1")
                        .header(HttpHeaders.ORIGIN, FRONTEND_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, HttpHeaders.AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, FRONTEND_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, HttpHeaders.AUTHORIZATION));
    }

    private String authorizationFor(String role) {
        return signedAuthorization(List.of(role), Instant.now().plusSeconds(300), TEST_SIGNING_SECRET);
    }

    private String signedAuthorization(List<?> roles, Instant expiration, String secret) {
        JwtBuilder token = Jwts.builder()
                .subject("test@example.com")
                .claim("id", 1)
                .claim("roles", roles)
                .expiration(Date.from(expiration));
        return signedAuthorization(token, secret);
    }

    private String signedAuthorization(JwtBuilder token, String secret) {
        return "Bearer " + token
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
