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

@Service // registra esta classe como bean de serviço no contexto do Spring; permite injeção via @Autowired
public class LanceService {

    @Autowired                     // injeta o bean TokenService para validar e extrair dados do JWT
    private TokenService tokenService;

    @Autowired                     // injeta o bean EditalRepository para verificar o status e a data do edital
    private EditalRepository editalRepository;

    @Autowired                     // injeta o bean LanceRepository para persistir e consultar lances
    private LanceRepository lanceRepository;

    // Submete um novo lance a um edital após validar token, role, status do edital e prazo de fechamento
    public void novoLance(Long id, LanceDTO lance, String token) {
        if (tokenService.validarToken(token)) { // verifica se o token é válido antes de qualquer operação
            UserDTO userLogado = tokenService.extrairClaim(token);      // extrai id, nome e role do payload JWT sem consultar o banco
            EditalDTO edital = editalRepository.getById(id);            // busca o edital no banco para validar status e data
            if (!userLogado.getRole().equals("FORNECEDOR")) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Necessário ser fornecedor para criar novo lance!"); // lança 403 se o usuário não for FORNECEDOR
            }
            if (!edital.getStatus().equals("ABERTO")) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Edital fechado para lances!"); // lança 400 se o status no banco já for FECHADO
            }
            if (LocalDateTime.now().isAfter(edital.getData_fechamento())) {
                throw new ResponseStatusException(           // lança 400 se o prazo de fechamento já passou, mesmo com status ainda "ABERTO" no banco
                        HttpStatusCode.valueOf(400),
                        "Edital fechado para lances!"
                );
            }

            lance.setData_lance(LocalDateTime.now());       // preenche a data do lance com o momento atual do servidor (não vem do front-end)
            lance.setId_edital(id);                         // preenche o id do edital a partir da URL (path variable)
            lance.setId_usuario(userLogado.getId());        // preenche o id do fornecedor extraído do JWT

            int rows = lanceRepository.novoLance(lance);   // persiste o lance no banco e armazena o número de linhas afetadas
            if (rows == 0) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(500), "Erro ao criar lance!"); // lança 500 se o INSERT não afetou nenhuma linha
            }
        } else {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token inválido!"); // lança 401 se o token for inválido ou expirado
        }
    }

    // Lista os lances de um edital aplicando regras de visibilidade: FECHADO = todos veem tudo; ABERTO = FORNECEDOR só vê os próprios
    public java.util.List<LanceDTO> listarLances(Long idEdital, String token) {
        if (!tokenService.validarToken(token)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token inválido!"); // lança 401 se o token for inválido ou expirado
        }

        UserDTO userLogado = tokenService.extrairClaim(token);    // extrai role e id do payload JWT para aplicar as regras de visibilidade
        EditalDTO edital = editalRepository.getById(idEdital);    // busca o edital para verificar status e data de fechamento
        if (edital == null) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(404), "Edital não encontrado!"); // lança 404 se o edital não existir
        }

        boolean isFechado = !"ABERTO".equals(edital.getStatus()) || LocalDateTime.now().isAfter(edital.getData_fechamento()); // considera fechado se status != "ABERTO" OU se o prazo já passou

        if (isFechado) {
            return lanceRepository.getLancesByEdital(idEdital); // edital fechado: todos os lances são visíveis para qualquer role (transparência pós-pregão)
        } else {
            if ("FORNECEDOR".equals(userLogado.getRole())) {
                return lanceRepository.getLancesByEditalAndUsuario(idEdital, userLogado.getId()); // edital aberto + FORNECEDOR: retorna apenas os próprios lances (sem vantagem competitiva)
            } else {
                return lanceRepository.getLancesByEdital(idEdital); // edital aberto + COMPRADOR: retorna todos os lances (o comprador monitora a disputa)
            }
        }
    }

    // Retorna todos os lances do fornecedor logado com o campo "vencedor" calculado para editais FECHADOS
    public java.util.List<com.bidding.system.bidding.model.MeuLanceDTO> getMeusLances(String token) {
        if (!tokenService.validarToken(token)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token inválido!"); // lança 401 se o token for inválido ou expirado
        }

        UserDTO userLogado = tokenService.extrairClaim(token); // extrai role e id do payload JWT
        if (!"FORNECEDOR".equals(userLogado.getRole())) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Apenas fornecedores podem visualizar seus lances."); // lança 403 se o usuário não for FORNECEDOR
        }

        java.util.List<com.bidding.system.bidding.model.MeuLanceDTO> meusLances = lanceRepository.getMeusLances(userLogado.getId()); // busca todos os lances do fornecedor com JOIN nos dados do edital

        for (com.bidding.system.bidding.model.MeuLanceDTO lance : meusLances) { // itera cada lance para calcular o campo "vencedor" em memória
            boolean isFechado = !"ABERTO".equals(lance.getStatusEdital()); // verifica pelo status do edital retornado pelo JOIN no repositório
            if (isFechado) {
                Double menorValor = lanceRepository.getMenorLanceByEdital(lance.getIdEdital()); // busca o menor valor de lance do edital para determinar o vencedor
                if (menorValor != null && lance.getValor() == menorValor) {
                    lance.setVencedor(true);  // marca como vencedor se o valor deste lance for igual ao menor valor do edital
                } else {
                    lance.setVencedor(false); // não é o menor valor: não é vencedor
                }
            } else {
                lance.setVencedor(false); // edital ainda ABERTO: não revela o vencedor enquanto o pregão está em andamento
            }
        }

        return meusLances; // retorna a lista com o campo "vencedor" preenchido para cada lance
    }
}