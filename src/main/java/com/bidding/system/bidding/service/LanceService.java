package com.bidding.system.bidding.service;

import com.bidding.system.bidding.model.EditalDTO;
import com.bidding.system.bidding.model.LanceDTO;
import com.bidding.system.bidding.model.MeuLanceDTO;
import com.bidding.system.bidding.model.UserDTO;
import com.bidding.system.bidding.repository.EditalRepository;
import com.bidding.system.bidding.repository.LanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LanceService {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private EditalRepository editalRepository;

    @Autowired
    private LanceRepository lanceRepository;

    public void novoLance(Long id, LanceDTO lance, String token) {
        if (!tokenService.validarToken(token)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token inválido!");
        }
        // Extrai id, nome e role do payload JWT sem consultar o banco de dados
        UserDTO userLogado = tokenService.extrairClaim(token);
        EditalDTO edital = editalRepository.getById(id);
        if (!userLogado.getRole().equals("FORNECEDOR")) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(403),
                    "Necessário ser fornecedor para criar novo lance!"
            );
        }
        // Permite lances apenas em editais ABERTOS (o status pode ser "ABERTO (ADIADO...)")
        if (edital.getStatus() == null || !edital.getStatus().startsWith("ABERTO")) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Edital fechado para lances!");
        }
        // Dupla verificação de prazo: o status pode ainda ser ABERTO mas a data ter passado (race condition com o job)
        if (LocalDateTime.now().isAfter(edital.getData_fechamento())) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Edital fechado para lances!");
        }
        if (lance.getValor() <= 0) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400),
                    "O valor do lance deve ser maior que zero!"
            );
        }
        int lancesDoFornecedor = lanceRepository.contarLancesPorFornecedor(id, userLogado.getId());
        if (lancesDoFornecedor > 0) {
            // Regra de negócio: um fornecedor só pode enviar um lance por edital
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Apenas um lance por fornecedor!");
        }
        lance.setData_lance(LocalDateTime.now());   // Data definida pelo servidor para evitar adulteração
        lance.setId_edital(id);                     // ID do edital extraído da URL, não do corpo da requisição
        lance.setId_usuario(userLogado.getId());    // ID do fornecedor extraído do JWT
        int rows = lanceRepository.novoLance(lance);
        if (rows == 0) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(500), "Erro ao criar lance!");
        }
    }

    public List<LanceDTO> listarLances(Long idEdital, String token) {
        if (!tokenService.validarToken(token)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token inválido!");
        }
        UserDTO userLogado = tokenService.extrairClaim(token);
        EditalDTO edital = editalRepository.getById(idEdital);
        if (edital == null) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(404), "Edital não encontrado!");
        }
        // Considera encerrado se o status for ENCERRADO OU se o prazo já passou (mesmo que o job ainda não atualizou)
        boolean isFechado = "ENCERRADO".equals(edital.getStatus())
                || LocalDateTime.now().isAfter(edital.getData_fechamento());
        if (isFechado) {
            // Edital encerrado: todos os lances são visíveis; o campo vencedor já vem do banco
            return lanceRepository.getLancesByEdital(idEdital);
        }
        if ("FORNECEDOR".equals(userLogado.getRole())) {
            // Edital aberto + FORNECEDOR: retorna apenas os próprios lances para preservar sigilo competitivo
            return lanceRepository.getLancesByEditalAndUsuario(idEdital, userLogado.getId());
        }
        // Edital aberto + COMPRADOR: retorna todos os lances
        return lanceRepository.getLancesByEdital(idEdital);
    }

    public List<MeuLanceDTO> getMeusLances(String token) {
        if (!tokenService.validarToken(token)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token inválido!");
        }
        UserDTO userLogado = tokenService.extrairClaim(token);
        if (!"FORNECEDOR".equals(userLogado.getRole())) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(403),
                    "Apenas fornecedores podem visualizar seus lances."
            );
        }
        // O campo "vencedor" já é lido diretamente da coluna no banco pelo repositório
        return lanceRepository.getMeusLances(userLogado.getId());
    }
}