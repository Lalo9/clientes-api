package com.lalo9.clientes.cliente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.lalo9.clientes.externo.BrasilApiClient;
import com.lalo9.clientes.externo.ViaCepClient;

class ClienteControllerTest {

    private static final String CNPJ_VALIDO = "00000000000191";
    private static final String CEP = "01001000";

    private ClienteRepository repository;
    private ViaCepClient viaCep;
    private BrasilApiClient brasilApi;
    private ClienteController controller;

    @BeforeEach
    void preparar() {
        repository = mock(ClienteRepository.class);
        viaCep = mock(ViaCepClient.class);
        brasilApi = mock(BrasilApiClient.class);
        controller = new ClienteController(repository, viaCep, brasilApi);

        when(repository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));
        when(brasilApi.buscar(CNPJ_VALIDO))
                .thenReturn(new BrasilApiClient.Empresa("BANCO DO BRASIL SA"));
        when(viaCep.buscar(CEP))
                .thenReturn(new ViaCepClient.Endereco("Praça da Sé", "Sé", "São Paulo", "SP", null));
    }

    @Test
    void cadastraPreenchendoRazaoSocialEEndereco() {
        Cliente salvo = controller.cadastrar(new ClienteRequest("Banco do Brasil", CNPJ_VALIDO, CEP));

        assertEquals("BANCO DO BRASIL SA", salvo.getRazaoSocial());
        assertEquals("Praça da Sé", salvo.getLogradouro());
        assertEquals("São Paulo", salvo.getCidade());
        assertEquals("SP", salvo.getUf());
        verify(repository).save(any(Cliente.class));
    }

    @Test
    void recusaCnpjInvalidoSemChamarAsApis() {
        var erro = assertThrows(ResponseStatusException.class,
                () -> controller.cadastrar(new ClienteRequest("Teste", "11111111111111", CEP)));

        assertEquals(422, erro.getStatusCode().value());
        verifyNoInteractions(viaCep, brasilApi);
        verify(repository, never()).save(any(Cliente.class));
    }

    @Test
    void recusaCnpjJaCadastrado() {
        when(repository.existsByCnpj(CNPJ_VALIDO)).thenReturn(true);

        var erro = assertThrows(ResponseStatusException.class,
                () -> controller.cadastrar(new ClienteRequest("Outro", CNPJ_VALIDO, CEP)));

        assertEquals(409, erro.getStatusCode().value());
        verify(repository, never()).save(any(Cliente.class));
    }

    @Test
    void naoSalvaQuandoOCepNaoExiste() {
        when(viaCep.buscar("99999999"))
                .thenThrow(new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "CEP não encontrado"));

        var erro = assertThrows(ResponseStatusException.class,
                () -> controller.cadastrar(new ClienteRequest("Teste", CNPJ_VALIDO, "99999999")));

        assertEquals(422, erro.getStatusCode().value());
        verify(repository, never()).save(any(Cliente.class));
    }

    @Test
    void naoSalvaQuandoABrasilApiEstaForaDoAr() {
        when(brasilApi.buscar(CNPJ_VALIDO))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Não foi possível consultar a BrasilAPI agora"));

        var erro = assertThrows(ResponseStatusException.class,
                () -> controller.cadastrar(new ClienteRequest("Teste", CNPJ_VALIDO, CEP)));

        assertEquals(502, erro.getStatusCode().value());
        verify(repository, never()).save(any(Cliente.class));
    }

    @Test
    void editarRefazAConsultaDoEndereco() {
        Cliente existente = new Cliente();
        existente.setId(8L);
        existente.setCnpj(CNPJ_VALIDO);
        when(repository.findById(8L)).thenReturn(Optional.of(existente));
        when(viaCep.buscar("70040010"))
                .thenReturn(new ViaCepClient.Endereco("Quadra SBN Quadra 1", "Asa Norte", "Brasília", "DF", null));

        Cliente editado = controller.editar(8L, new ClienteRequest("Banco do Brasil", CNPJ_VALIDO, "70040010"));

        assertEquals("Brasília", editado.getCidade());
        assertEquals("DF", editado.getUf());
    }

    @Test
    void editarRecusaCnpjDeOutroCliente() {
        Cliente existente = new Cliente();
        existente.setId(9L);
        when(repository.findById(9L)).thenReturn(Optional.of(existente));
        when(repository.existsByCnpjAndIdNot(CNPJ_VALIDO, 9L)).thenReturn(true);

        var erro = assertThrows(ResponseStatusException.class,
                () -> controller.editar(9L, new ClienteRequest("Petrobras", CNPJ_VALIDO, CEP)));

        assertEquals(409, erro.getStatusCode().value());
        verify(repository, never()).save(any(Cliente.class));
    }

    @Test
    void editarClienteInexistenteDa404() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        var erro = assertThrows(ResponseStatusException.class,
                () -> controller.editar(999L, new ClienteRequest("X", CNPJ_VALIDO, CEP)));

        assertEquals(404, erro.getStatusCode().value());
    }
}