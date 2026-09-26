-- ============================================================================
-- MIGRAÇÃO V2 — Documentação da criptografia AES-256-GCM do CPF
-- ============================================================================
-- A coluna cpf_hash armazena o CPF do cidadão anônimo criptografado com
-- AES-256-GCM. O CPF bruto NUNCA é persistido neste banco.
--
-- Formato: Base64-URL(IV[12 bytes] || ciphertext || GCM-tag[16 bytes])
-- Chave gerenciada via variável de ambiente CPF_AES_KEY (dev) ou
-- Secret Manager GCP (produção).
--
-- Para descriptografar (uso restrito — autoridades de saúde com permissão):
--   Chamar CpfCryptoService.decrypt() no pdm-report-ms.
-- ============================================================================

COMMENT ON COLUMN tb_reports.cpf_hash IS
    'CPF do cidadão anônimo criptografado com AES-256-GCM (Base64-URL). '
    'O CPF bruto nunca é armazenado. Chave gerenciada pelo Secret Manager GCP em produção.';
