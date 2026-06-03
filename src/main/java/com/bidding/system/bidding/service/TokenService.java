package com.bidding.system.bidding.service;

import com.bidding.system.bidding.model.UserDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String gerarToken(UserDTO user) {
        if (user.getId() == null || user.getId() == 0 ||
                user.getNome() == null || user.getNome().isEmpty() ||
                user.getEmail() == null || user.getEmail().isEmpty() ||
                user.getSenha() == null || user.getSenha().isEmpty()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Credenciais inválidas ou utilizador não encontrado.");
        }
        return Jwts.builder()
                .subject(user.getNome())
                .claim("id", user.getId())
                .claim("nome", user.getNome())
                .claim("role", user.getRole())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3000000))
                .signWith(getSignKey())
                .compact();
    }

    public UserDTO extrairClaim(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        UserDTO user = new UserDTO();
        user.setId(claims.get("id", Long.class));
        user.setNome(claims.get("nome", String.class));
        user.setRole(claims.get("role", String.class));
        return user;
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}