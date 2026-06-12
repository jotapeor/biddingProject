package com.bidding.system.bidding.service;

import com.bidding.system.bidding.model.EditalDTO;
import com.bidding.system.bidding.model.UserDTO;
import com.bidding.system.bidding.repository.EditalRepository;
import com.bidding.system.bidding.repository.LanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EditalService {

    @Autowired
    private EditalRepository editalRepository;

    @Autowired
    private LanceRepository lanceRepository;

    @Autowired
    private TokenService tokenService;

    public void novoEdital(EditalDTO edital, UserDTO usuarioLogado) {
        String message = "";
        if (!usuarioLogado.getRole().equals("COMPRADOR")) {
            // Lança 403 Forbidden imediatamente se a role não for COMPRADOR, sem processar mais nada
            throw new ResponseStatusException(HttpStatusCode.valueOf(403),
                    "Acesso negado: apenas usuários com role COMPRADOR podem criar editais"
            );
        }
        if (edital.getTitulo() == null || edital.getTitulo().trim().isEmpty()) {
            message += "O título não pode ser vazio. ";
        } else if (edital.getTitulo().trim().length() < 5) {
            message += "O título deve ter no mínimo 5 caracteres. ";
        } else if (edital.getTitulo().trim().length() > 150) {
            message += "O título deve ter no máximo 150 caracteres. ";
        }
        if (edital.getDescricao() == null || edital.getDescricao().trim().isEmpty()) {
            message += "A descrição não pode ser vazia. ";
        } else if (edital.getDescricao().trim().length() > 1000) {
            message += "A descrição deve ter no máximo 1000 caracteres. ";
        }
        if (edital.getData_fechamento() == null) {
            message += "Informe a data e hora de fechamento. ";
        } else if (edital.getData_fechamento().isBefore(LocalDateTime.now()) || edital.getData_fechamento().isEqual(LocalDateTime.now())) {
            message += "A data de fechamento deve ser no futuro. ";
        }
        if (!message.trim().isEmpty()) {
            // Lança 400 com todas as mensagens de erro acumuladas de uma vez
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), message.trim());
        }
        // O status inicial é sempre "ABERTO" — não pode ser definido pelo cliente na requisição
        edital.setStatus("ABERTO");
        int rows = editalRepository.novoEdital(edital);
        if (rows == 0) {
            // Lança 500 se o repositório retornar 0 (INSERT não afetou nenhuma linha)
            throw new ResponseStatusException(HttpStatusCode.valueOf(500), "Erro ao criar edital");
        }
    }

    public List<EditalDTO> listaEdital(String authHeader, boolean urgente) {
        if (!tokenService.validarToken(authHeader)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token inválido!");
        }
        List<EditalDTO> editais = editalRepository.listaEdital();
        if (!urgente) {
            return editais; // Sem filtro de urgência, retorna todos os editais diretamente
        }
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime limite = agora.plusHours(48); // Define a janela de 48 horas para "urgente"
        return editais.stream()
                .filter(edital -> edital.getStatus() != null && edital.getStatus().startsWith("ABERTO"))
                .filter(edital -> edital.getData_fechamento() != null)
                .filter(edital -> {
                    LocalDateTime fechamento = edital.getData_fechamento();
                    // isAfter(agora): exclui editais já encerrados; isBefore(limite): inclui apenas os que fecham em até 48h
                    return fechamento.isAfter(agora) && fechamento.isBefore(limite);
                })
                .collect(Collectors.toList());
    }

    public EditalDTO buscarEdital(Long id, String authHeader) {
        if (!tokenService.validarToken(authHeader)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token inválido!");
        }
        EditalDTO edital = editalRepository.getById(id);
        if (edital == null) {
            // O repositório retorna null quando o ID não existe; convertemos para 404 aqui
            throw new ResponseStatusException(HttpStatusCode.valueOf(404), "Edital não encontrado");
        }
        if (edital.getStatus() != null && edital.getStatus().startsWith("ABERTO")) {
            // Normaliza o status para "ABERTO" mesmo que contenha a mensagem de adiamento completa
            edital.setStatus("ABERTO");
        }
        return edital;
    }

    @Scheduled(fixedDelay = 5000)
    public void verificarEFecharEditaisExpirados() {
        List<EditalDTO> expirados = editalRepository.getEditaisAbertosExpirados();
        for (EditalDTO edital : expirados) {
            Long id = edital.getId();
            int totalLances = editalRepository.contarLancesByEdital(id);
            if (totalLances == 0) {
                // Regra 1: sem participantes → prorroga 3 dias para não encerrar um edital vazio
                editalRepository.prorrogarEdital(id);
                continue; // Passa para o próximo edital sem executar as regras de encerramento
            }
            // Regra 2: com lances → encerra o edital e persiste o vencedor
            editalRepository.atualizarStatusEdital(id, "ENCERRADO");
            Long idLanceVencedor = lanceRepository.getIdLanceVencedor(id);
            if (idLanceVencedor != null) {
                lanceRepository.resetarVencedores(id);      // Garante que nenhum lance fique marcado erroneamente
                lanceRepository.marcarVencedor(idLanceVencedor); // Persiste o vencedor na tabela de lances
                Long idFornecedor = lanceRepository.getIdFornecedorDoLance(idLanceVencedor);
                if (idFornecedor != null) {
                    editalRepository.atualizarVencedorEdital(id, idFornecedor); // Persiste o vencedor na tabela de editais
                }
            }
        }
    }
}