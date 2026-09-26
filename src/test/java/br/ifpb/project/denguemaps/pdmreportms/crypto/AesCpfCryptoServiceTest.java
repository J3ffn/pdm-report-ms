package br.ifpb.project.denguemaps.pdmreportms.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AesCpfCryptoService — AES-256-GCM")
class AesCpfCryptoServiceTest {

    // Chave de 32 bytes em Base64 (válida para AES-256)
    private static final String TEST_KEY_B64 = Base64.getEncoder()
            .encodeToString("test-key-32bytes-for-aes-256-ok!".getBytes());

    private AesCpfCryptoService service;

    @BeforeEach
    void setUp() {
        service = new AesCpfCryptoService(TEST_KEY_B64);
    }

    @Test
    @DisplayName("encrypt() → decrypt() deve retornar o CPF original")
    void roundTripDeveRetornarCpfOriginal() {
        String cpf = "12345678900";
        String encrypted = service.encrypt(cpf);
        String decrypted = service.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(cpf);
    }

    @Test
    @DisplayName("O CPF original não deve aparecer no token criptografado")
    void cpfNaoDeveAparecerNaFormaCriptografada() {
        String cpf = "12345678900";
        String encrypted = service.encrypt(cpf);

        assertThat(encrypted).doesNotContain(cpf);
    }

    @Test
    @DisplayName("Dois encrypts do mesmo CPF devem produzir tokens DIFERENTES (IV aleatório)")
    void mesmoCpfDeveProducirTokensDiferentes() {
        String cpf = "12345678900";
        String enc1 = service.encrypt(cpf);
        String enc2 = service.encrypt(cpf);

        assertThat(enc1).isNotEqualTo(enc2);

        // Mas ambos devem decriptar para o mesmo CPF
        assertThat(service.decrypt(enc1)).isEqualTo(cpf);
        assertThat(service.decrypt(enc2)).isEqualTo(cpf);
    }

    @Test
    @DisplayName("Chave com tamanho incorreto deve lançar IllegalStateException na inicialização")
    void chaveCurtaDeveFalharNaInicializacao() {
        String shortKey = Base64.getEncoder().encodeToString("short-key".getBytes()); // < 32 bytes

        assertThatThrownBy(() -> new AesCpfCryptoService(shortKey))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }

    @Test
    @DisplayName("Token corrompido deve lançar IllegalStateException na descriptografia")
    void tokenCorrompidoDeveFalharNaDescriptografia() {
        assertThatThrownBy(() -> service.decrypt("token-invalido-xpto"))
                .isInstanceOf(IllegalStateException.class);
    }
}
