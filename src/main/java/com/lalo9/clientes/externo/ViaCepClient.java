package com.lalo9.clientes.externo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ViaCepClient {

    private static final Logger log = LoggerFactory.getLogger(ViaCepClient.class);

    private final RestClient restClient;

    public ViaCepClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://viacep.com.br/ws").build();
    }

    public record Endereco(String logradouro, String bairro, String localidade, String uf, Boolean erro) {
    }

    @Cacheable("ceps")
    public Endereco buscar(String cep) {
        log.info("Consultando o ViaCEP para o CEP {}", cep);
        try {
            Endereco endereco = restClient.get()
                    .uri("/{cep}/json/", cep)
                    .retrieve()
                    .body(Endereco.class);
            if (endereco == null || Boolean.TRUE.equals(endereco.erro())) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "CEP não encontrado");
            }
            return endereco;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Não foi possível consultar o ViaCEP agora");
        }
    }
}