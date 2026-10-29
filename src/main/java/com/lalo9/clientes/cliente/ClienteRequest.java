package com.lalo9.clientes.cliente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "O CNPJ é obrigatório")
        @Pattern(regexp = "\\d{14}", message = "O CNPJ deve ter 14 dígitos, só números")
        String cnpj,

        @NotBlank(message = "O CEP é obrigatório")
        @Pattern(regexp = "\\d{8}", message = "O CEP deve ter 8 dígitos, só números")
        String cep
) {
}