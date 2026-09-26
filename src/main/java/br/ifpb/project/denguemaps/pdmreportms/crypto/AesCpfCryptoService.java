package br.ifpb.project.denguemaps.pdmreportms.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Implementação de criptografia de CPF usando AES-256-GCM.
 *
 * <p>Formato do valor persistido: Base64(IV[12 bytes] + ciphertext + tag[16 bytes])
 *
 * <p>A chave AES (32 bytes) é lida da variável de ambiente {@code CPF_AES_KEY} como Base64.
 * Para gerar uma chave de desenvolvimento:
 * <pre>openssl rand -base64 32</pre>
 *
 * <p>Em produção, substituir a injeção de {@code CPF_AES_KEY} por uma chamada ao Secret Manager.
 * O restante da classe permanece inalterado (DIP ativo).
 *
 * <p>Princípio KISS: sem dependências externas — apenas {@code javax.crypto} da JDK.
 */
@Service
public class AesCpfCryptoService implements CpfCryptoService {

    private static final Logger log = LoggerFactory.getLogger(AesCpfCryptoService.class);

    private static final String ALGORITHM   = "AES/GCM/NoPadding";
    private static final int    IV_LENGTH   = 12;   // bytes — padrão recomendado para GCM
    private static final int    TAG_BITS    = 128;  // bits — tag de autenticação GCM

    private final SecretKeySpec secretKey;
    private final SecureRandom  random = new SecureRandom();

    /**
     * Construtor: falha rápido se a chave estiver ausente ou com tamanho incorreto.
     * Isso evita que a aplicação suba em estado inválido.
     */
    public AesCpfCryptoService(@Value("${app.crypto.cpf-aes-key}") String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                "CPF_AES_KEY deve ser uma chave AES-256 (32 bytes / 44 chars Base64). " +
                "Gere com: openssl rand -base64 32"
            );
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
        log.info("[CpfCrypto] AES-256-GCM inicializado com sucesso.");
    }

    @Override
    public String encrypt(String cpf) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);  // IV aleatório por chamada — garante que mesmo CPF gere cipher diferente

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_BITS, iv));

            byte[] ciphertext = cipher.doFinal(cpf.getBytes());

            // Concatena IV + ciphertext (tag já está embutida no ciphertext pelo GCM)
            byte[] result = new byte[IV_LENGTH + ciphertext.length];
            System.arraycopy(iv,         0, result, 0,         IV_LENGTH);
            System.arraycopy(ciphertext, 0, result, IV_LENGTH, ciphertext.length);

            return Base64.getUrlEncoder().withoutPadding().encodeToString(result);
        } catch (Exception e) {
            log.error("[CpfCrypto] Falha ao criptografar CPF", e);
            throw new IllegalStateException("Falha na criptografia do CPF", e);
        }
    }

    @Override
    public String decrypt(String encrypted) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(encrypted);

            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, IV_LENGTH);

            byte[] ciphertext = new byte[decoded.length - IV_LENGTH];
            System.arraycopy(decoded, IV_LENGTH, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_BITS, iv));

            return new String(cipher.doFinal(ciphertext));
        } catch (Exception e) {
            log.error("[CpfCrypto] Falha ao descriptografar CPF", e);
            throw new IllegalStateException("Falha na descriptografia do CPF", e);
        }
    }
}
