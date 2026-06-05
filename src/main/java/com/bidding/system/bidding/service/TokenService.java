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

@Service // registra esta classe como bean de serviço no contexto do Spring; permite injeção via @Autowired
public class TokenService {

    @Value("${api.security.token.secret}") // injeta o valor da propriedade "api.security.token.secret" do application.properties — mantém o segredo fora do código-fonte
    private String secret;

    // Decodifica a chave secreta de Base64 e cria um objeto SecretKey HMAC-SHA usado para assinar e verificar tokens
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.secret); // decodifica a string Base64 para bytes
        return Keys.hmacShaKeyFor(keyBytes); // cria a chave criptográfica HMAC-SHA a partir dos bytes
    }

    // Gera e retorna um token JWT assinado com os dados do usuário autenticado
    public String gerarToken(UserDTO user) {
        if (user.getId() == null || user.getId() == 0 ||
                user.getNome() == null || user.getNome().isEmpty() ||
                user.getEmail() == null || user.getEmail().isEmpty() ||
                user.getSenha() == null || user.getSenha().isEmpty()) { // valida os campos antes de gerar: dados inválidos indicam que o login falhou
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Credenciais inválidas ou utilizador não encontrado.");
        }
        return Jwts.builder()
                .subject(user.getNome())              // campo "sub" do JWT: identifica o sujeito do token (convenção JWT)
                .claim("id", user.getId())            // adiciona o id do usuário ao payload — extraído pelo TokenService.extrairClaim()
                .claim("nome", user.getNome())        // adiciona o nome ao payload — exibido na navbar do front-end
                .claim("role", user.getRole())        // adiciona a role ao payload — usada nas validações de autorização dos Services
                .issuedAt(new Date())                 // "iat": registra o momento em que o token foi emitido
                .expiration(new Date(System.currentTimeMillis() + 3000000)) // "exp": token expira em 3.000.000 ms = 50 minutos
                .signWith(getSignKey())               // assina o token com HMAC-SHA usando a chave secreta
                .compact();                           // serializa o token para o formato compacto: header.payload.signature
    }

    // Extrai os dados do usuário (id, nome, role) do payload do JWT sem consultar o banco
    public UserDTO extrairClaim(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignKey())  // configura a chave para verificar a assinatura do token
                .build()
                .parseSignedClaims(token) // analisa o token, verificando assinatura e expiração; lança JwtException se inválido
                .getPayload();            // retorna o payload (claims) do token como objeto Claims

        UserDTO user = new UserDTO();
        user.setId(claims.get("id", Long.class));       // lê o claim "id" do payload e converte para Long
        user.setNome(claims.get("nome", String.class)); // lê o claim "nome" do payload
        user.setRole(claims.get("role", String.class)); // lê o claim "role" do payload
        return user; // retorna um UserDTO com os dados necessários para autorização, sem precisar de nova consulta ao banco
    }

    // Valida a assinatura e a expiração do token; retorna true se válido, false se adulterado, expirado ou malformado
    public boolean validarToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSignKey()) // configura a chave para verificação
                    .build()
                    .parseClaimsJws(token);      // tenta analisar o token; lança exceção se inválido ou expirado
            return true; // se chegou até aqui sem exceção, o token é válido
        } catch (JwtException | IllegalArgumentException e) {
            return false; // captura qualquer problema com o token (adulterado, expirado, nulo) e retorna false sem lançar exceção ao chamador
        }
    }
}