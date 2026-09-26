package br.ifpb.project.denguemaps.pdmreportms.crypto;

/**
 * Contrato de criptografia do CPF do cidadão anônimo.
 *
 * <p>O CPF bruto NUNCA é persistido. Esta interface garante que apenas
 * o dado criptografado seja armazenado no banco de dados.
 *
 * <p>Em desenvolvimento: chave AES lida de variável de ambiente {@code CPF_AES_KEY}.
 * Em produção: substituir a implementação por uma que busque via Secret Manager (GCP/AWS).
 *
 * <p>Princípio aplicado: DIP — o serviço depende da interface, não da implementação AES.
 */
public interface CpfCryptoService {

    /**
     * Criptografa o CPF usando AES-256-GCM.
     *
     * @param cpf CPF em texto puro (11 dígitos numéricos)
     * @return String Base64 contendo IV + ciphertext + tag de autenticação GCM
     */
    String encrypt(String cpf);

    /**
     * Descriptografa um CPF previamente criptografado.
     * Uso restrito: apenas para autoridades de saúde com permissão explícita.
     *
     * @param encrypted String Base64 retornada pelo {@link #encrypt(String)}
     * @return CPF em texto puro
     */
    String decrypt(String encrypted);
}
