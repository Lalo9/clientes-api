package com.lalo9.clientes.cliente;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CnpjValidadorTest {

    @Test
    void aceitaCnpjsValidos() {
        assertTrue(CnpjValidador.valido("00000000000191"));
        assertTrue(CnpjValidador.valido("33000167000101"));
        assertTrue(CnpjValidador.valido("60746948000112"));
    }

    @Test
    void recusaDigitosVerificadoresErrados() {
        assertFalse(CnpjValidador.valido("00000000000192"));
        assertFalse(CnpjValidador.valido("33000167000102"));
    }

    @Test
    void recusaTodosOsDigitosIguais() {
        assertFalse(CnpjValidador.valido("11111111111111"));
    }

    @Test
    void recusaFormatoInvalido() {
        assertFalse(CnpjValidador.valido("123"));
        assertFalse(CnpjValidador.valido("0000000000019a"));
        assertFalse(CnpjValidador.valido(null));
    }
}