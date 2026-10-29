package com.lalo9.clientes;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of("sistema", "clientes-api", "situacao", "no ar");
    }
}