package com.lalo9.clientes.externo;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class BrasilApiClient {

    private final RestClient restClient;

    public BrasilApiClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://brasilapi.com.br/api").build();
    }

    public record Empresa(@JsonProperty("razao_social") String razaoSocial) {
    }

    public Empresa buscar(String cnpj) {
        try {
            Empresa empresa = restClient.get()
                    .uri("/cnpj/v1/{cnpj}", cnpj)
                    .retrieve()
                    .body(Empresa.class);
            if (empresa == null) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "CNPJ não encontrado");
            }
            return empresa;
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "CNPJ não encontrado");
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Não foi possível consultar a BrasilAPI agora");
        }
    }
}