package com.lalo9.clientes.externo;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;


@Component
public class ViaCepClient {

    private final RestClient restClient;

    public ViaCepClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://viacep.com.br/ws").build();
    }

    public record Endereco(String logradouro, String bairro, String localidade, String uf, Boolean erro) {
    }

    public Endereco buscar(String cep) {
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