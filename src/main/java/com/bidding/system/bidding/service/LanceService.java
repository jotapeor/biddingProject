package com.bidding.system.bidding.service;

import com.bidding.system.bidding.model.EditalDTO;
import com.bidding.system.bidding.model.LanceDTO;
import com.bidding.system.bidding.model.UserDTO;
import com.bidding.system.bidding.repository.EditalRepository;
import com.bidding.system.bidding.repository.LanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class LanceService {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private EditalRepository editalRepository;

    @Autowired
    private LanceRepository lanceRepository;

    public void novoLance(Long id, LanceDTO lance, String token) {
        if (tokenService.validarToken(token)) {
            UserDTO userLogado = tokenService.extrairClaim(token);
            EditalDTO edital = editalRepository.getById(id);
            if (!userLogado.getRole().equals("FORNECEDOR")) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Necessário ser fornecedor para criar novo lance!");
            }
            if (!edital.getStatus().equals("ABERTO")) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Edital fechado para lances!");
            }
            if (LocalDateTime.now().isAfter(edital.getData_fechamento())) {
                throw new ResponseStatusException(
                        HttpStatusCode.valueOf(400),
                        "Edital fechado para lances!"
                );
            }
            
            lance.setData_lance(LocalDateTime.now());
            lance.setId_edital(id);
            lance.setId_usuario(userLogado.getId());
            
            int rows = lanceRepository.novoLance(lance);
            if (rows == 0) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(500), "Erro ao criar lance!");
            }
        } else {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token inválido!");
        }
    }

    public java.util.List<LanceDTO> listarLances(Long idEdital, String token) {
        if (!tokenService.validarToken(token)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token inválido!");
        }

        UserDTO userLogado = tokenService.extrairClaim(token);
        EditalDTO edital = editalRepository.getById(idEdital);
        if (edital == null) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(404), "Edital não encontrado!");
        }

        boolean isFechado = !"ABERTO".equals(edital.getStatus()) || LocalDateTime.now().isAfter(edital.getData_fechamento());

        if (isFechado) {
            // Se fechado, todos veem todos os lances
            return lanceRepository.getLancesByEdital(idEdital);
        } else {
            // Se aberto, FORNECEDOR só vê os próprios
            if ("FORNECEDOR".equals(userLogado.getRole())) {
                return lanceRepository.getLancesByEditalAndUsuario(idEdital, userLogado.getId());
            } else {
                // Se aberto, COMPRADOR vê todos os lances
                return lanceRepository.getLancesByEdital(idEdital);
            }
        }
    }

    public java.util.List<com.bidding.system.bidding.model.MeuLanceDTO> getMeusLances(String token) {
        if (!tokenService.validarToken(token)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token inválido!");
        }

        UserDTO userLogado = tokenService.extrairClaim(token);
        if (!"FORNECEDOR".equals(userLogado.getRole())) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Apenas fornecedores podem visualizar seus lances.");
        }

        java.util.List<com.bidding.system.bidding.model.MeuLanceDTO> meusLances = lanceRepository.getMeusLances(userLogado.getId());

        for (com.bidding.system.bidding.model.MeuLanceDTO lance : meusLances) {
            boolean isFechado = !"ABERTO".equals(lance.getStatusEdital()); // We could also check dates, but status is simpler. If status is FECHADO, then we can show winner.
            // Also checking dates if status not updated, wait, let's just check the status returned from DB.
            if (isFechado) {
                Double menorValor = lanceRepository.getMenorLanceByEdital(lance.getIdEdital());
                if (menorValor != null && lance.getValor() == menorValor) {
                    lance.setVencedor(true);
                } else {
                    lance.setVencedor(false);
                }
            } else {
                lance.setVencedor(false);
            }
        }

        return meusLances;
    }
}