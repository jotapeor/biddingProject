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

    /**
     * Chave secreta em Base64 lida do {@code application.properties}.
     * Injetada pelo Spring via {@code @Value}, o que evita hardcoding no código-fonte
     * e permite configuração por ambiente (dev, staging, prod).
     */
    @Value("${api.security.token.secret}")
    private String secret;

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.secret); // Decodifica a string Base64 para bytes brutos
        return Keys.hmacShaKeyFor(keyBytes); // Cria a chave criptográfica HMAC-SHA a partir dos bytes
    }

    public String gerarToken(UserDTO user) {
        if (user.getId() == null || user.getId() == 0
                || user.getNome() == null || user.getNome().isEmpty()
                || user.getEmail() == null || user.getEmail().isEmpty()
                || user.getSenha() == null || user.getSenha().isEmpty()) {
            // Dados inválidos indicam que o login falhou; lançamos 400 em vez de gerar um token inválido
            throw new ResponseStatusException(HttpStatusCode.valueOf(400),
                    "Credenciais inválidas ou utilizador não encontrado."
            );
        }
        return Jwts.builder()
                .subject(user.getNome())              // "sub": sujeito do token (convenção JWT)
                .claim("id", user.getId())            // Claim customizado: ID do usuário, extraído pelo TokenService.extrairClaim()
                .claim("nome", user.getNome())        // Claim customizado: nome exibido na navbar do frontend
                .claim("role", user.getRole())        // Claim customizado: role usada nas validações de autorização
                .issuedAt(new Date())                 // "iat": momento de emissão do token
                .expiration(new Date(System.currentTimeMillis() + 3_000_000)) // "exp": 3.000.000 ms = ~50 minutos
                .signWith(getSignKey())               // Assina o token com HMAC-SHA usando a chave secreta
                .compact();                           // Serializa para o formato compacto: header.payload.signature
    }

    public UserDTO extrairClaim(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignKey())  // Configura a chave para verificar a assinatura antes de ler o payload
                .build()
                .parseSignedClaims(token) // Analisa e verifica o token; lança JwtException se inválido
                .getPayload();            // Retorna o payload (claims) como objeto Claims
        UserDTO user = new UserDTO();
        user.setId(claims.get("id", Long.class));       // Lê o claim "id" e converte para Long
        user.setNome(claims.get("nome", String.class)); // Lê o claim "nome"
        user.setRole(claims.get("role", String.class)); // Lê o claim "role" para autorização
        return user;
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSignKey()) // Configura a chave para verificação da assinatura
                    .build()
                    .parseClaimsJws(token);      // Lança JwtException se o token for inválido ou expirado
            return true; // Se não lançou exceção, o token é válido
        } catch (JwtException | IllegalArgumentException e) {
            // Captura token adulterado, expirado, nulo ou malformado — retorna false sem propagar exceção
            return false;
        }
    }
}