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

@Service // registra esta classe como bean de serviço no contexto do Spring; permite injeção via @Autowired
public class EditalService {

    @Autowired                   // injeta o bean EditalRepository gerenciado pelo Spring
    private EditalRepository editalRepository;

    @Autowired                   // injeta o bean TokenService para validar tokens nas operações de listagem e busca
    private TokenService tokenService;

    // Cria um novo edital após validar permissão (role COMPRADOR) e campos obrigatórios
    public void novoEdital(EditalDTO edital, UserDTO usuarioLogado) {
        String message = "";
        if (!usuarioLogado.getRole().equals("COMPRADOR")) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(403), // lança 403 Forbidden se o usuário não for COMPRADOR
                    "Acesso negado: apenas usuários com role COMPRADOR podem criar editais"
            );
        }
        if (edital.getTitulo().isEmpty()) {
            message += "Título não preenchido!";     // acumula erro se o título estiver vazio
        }
        if (edital.getDescricao().isEmpty()) {
            message += "Descrição não preenchida!";  // acumula erro se a descrição estiver vazia
        }
        if (edital.getData_fechamento() == null) {
            message += "Data não preenchida!";       // acumula erro se a data de fechamento não foi informada
        }
        if (!message.isEmpty()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), message); // lança 400 Bad Request com todas as mensagens de erro acumuladas
        }
        edital.setStatus("ABERTO"); // o status é sempre "ABERTO" na criação — não é definido pelo cliente
        int rows = editalRepository.novoEdital(edital); // persiste o edital no banco e armazena o número de linhas afetadas
        if (rows == 0) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(500), // lança 500 se o INSERT não afetou nenhuma linha
                    "Erro ao criar edital");
        }
    }

    // Retorna a lista de editais, com filtragem opcional por urgência (editais ABERTOS com prazo nas próximas 48h)
    public List<EditalDTO> listaEdital(String authHeader, boolean urgente) {
        if (!tokenService.validarToken(authHeader)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token inválido!"); // lança 401 se o token for inválido ou expirado
        }

        List<EditalDTO> editais = editalRepository.listaEdital(); // busca todos os editais do banco

        if (!urgente) {
            return editais; // se não foi pedido filtro de urgência, retorna todos sem processamento adicional
        }

        LocalDateTime agora = LocalDateTime.now();          // captura o momento atual para comparação
        LocalDateTime limite = agora.plusHours(48);         // define o limite de 48 horas a partir de agora para considerar "urgente"

        return editais.stream()
                .filter(edital -> "ABERTO".equalsIgnoreCase(edital.getStatus()))   // descarta editais FECHADOS
                .filter(edital -> edital.getData_fechamento() != null)             // descarta editais sem data de fechamento definida
                .filter(edital -> {
                    LocalDateTime fechamento = edital.getData_fechamento();
                    return fechamento.isAfter(agora)    // isAfter(agora): descarta editais cujo prazo já passou
                            && fechamento.isBefore(limite); // isBefore(limite): mantém apenas os que fecham dentro das próximas 48h
                })
                .collect(Collectors.toList()); // coleta os editais filtrados em uma nova lista e retorna
    }

    // Busca um edital pelo id; valida o token (401) e lança 404 se o edital não existir
    public EditalDTO buscarEdital(Long id, String authHeader) {
        if (!tokenService.validarToken(authHeader)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token inválido!"); // lança 401 se o token for inválido ou expirado
        }

        EditalDTO edital = editalRepository.getById(id); // consulta o banco; retorna null se o id não existir
        if (edital == null) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(404), "Edital não encontrado"); // lança 404 Not Found quando o repositório retorna null
        }
        return edital;
    }
}