package com.bidding.system.bidding.service;

import com.bidding.system.bidding.model.EditalDTO;
import com.bidding.system.bidding.model.UserDTO;
import com.bidding.system.bidding.repository.EditalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
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
    private TokenService tokenService;

    public void novoEdital(EditalDTO edital, UserDTO usuarioLogado) {
        String message = "";
        if (!usuarioLogado.getRole().equals("COMPRADOR")) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(403),
                    "Acesso negado: apenas usuários com role COMPRADOR podem criar editais"
            );
        }
        if (edital.getTitulo().isEmpty()) {
            message += "Título não preenchido!";
        }
        if (edital.getDescricao().isEmpty()) {
            message += "Descrição não preenchida!";
        }
        if (edital.getData_fechamento() == null) {
            message += "Data não preenchida!";
        }
        if (!message.isEmpty()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), message);
        }
        edital.setStatus("ABERTO");
        int rows = editalRepository.novoEdital(edital);
        if (rows == 0) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(500),
                    "Erro ao criar edital");
        }
    }

    public List<EditalDTO> listaEdital(String authHeader, boolean urgente) {
        if (!tokenService.validarToken(authHeader)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token inválido!");
        }

        List<EditalDTO> editais = editalRepository.listaEdital();

        if (!urgente) {
            return editais;
        }

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime limite = agora.plusHours(48);

        return editais.stream()
                .filter(edital -> "ABERTO".equalsIgnoreCase(edital.getStatus()))
                .filter(edital -> edital.getData_fechamento() != null)
                .filter(edital -> {
                    LocalDateTime fechamento = edital.getData_fechamento();

                    return fechamento.isAfter(agora)
                            && fechamento.isBefore(limite);
                })
                .collect(Collectors.toList());
    }

    public EditalDTO buscarEdital(Long id, String authHeader) {
        if (!tokenService.validarToken(authHeader)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token inválido!");
        }

        EditalDTO edital = editalRepository.getById(id);
        if (edital == null) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(404), "Edital não encontrado");
        }
        return edital;
    }
}