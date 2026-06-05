package com.bidding.system.bidding;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest // sobe o contexto completo da aplicação Spring antes de executar os testes, verificando se todos os beans são criados sem erros
class BiddingApplicationTests {

    @Test
    void contextLoads() {
        // não possui asserções propositalmente: se o contexto do Spring falhar ao inicializar (ex: @Autowired não resolvido, properties inválidas), o teste falha automaticamente
    }

}
