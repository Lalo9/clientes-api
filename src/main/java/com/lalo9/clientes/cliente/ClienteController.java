package com.lalo9.clientes.cliente;

import java.util.List;

import com.lalo9.clientes.externo.BrasilApiClient;
import com.lalo9.clientes.externo.ViaCepClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteRepository repository;
private final ViaCepClient viaCep;
private final BrasilApiClient brasilApi;

public ClienteController(ClienteRepository repository, ViaCepClient viaCep, BrasilApiClient brasilApi) {
    this.repository = repository;
    this.viaCep = viaCep;
    this.brasilApi = brasilApi;
}

    @PostMapping
@ResponseStatus(HttpStatus.CREATED)
public Cliente cadastrar(@Valid @RequestBody ClienteRequest request) {
    if (repository.existsByCnpj(request.cnpj())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um cliente com este CNPJ");
    }
    Cliente cliente = new Cliente();
    cliente.setNome(request.nome());
    cliente.setCnpj(request.cnpj());
    cliente.setCep(request.cep());
    preencherDadosExternos(cliente);
    return repository.save(cliente);
}

private void preencherDadosExternos(Cliente cliente) {
    var empresa = brasilApi.buscar(cliente.getCnpj());
    var endereco = viaCep.buscar(cliente.getCep());
    cliente.setRazaoSocial(empresa.razaoSocial());
    cliente.setLogradouro(endereco.logradouro());
    cliente.setBairro(endereco.bairro());
    cliente.setCidade(endereco.localidade());
    cliente.setUf(endereco.uf());
}

    @GetMapping
    public List<Cliente> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Cliente buscar(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
    }

    @PutMapping("/{id}")
    public Cliente editar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
        cliente.setNome(request.nome());
        cliente.setCnpj(request.cnpj());
        cliente.setCep(request.cep());
        return repository.save(cliente);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado");
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}