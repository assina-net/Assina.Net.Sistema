SET NOCOUNT ON;

DECLARE @ClienteSistema uniqueidentifier = '57AB12B5-18D5-47A9-9206-E88192AF5F7E';
DECLARE @UsuarioAdmin uniqueidentifier = 'B53CD91F-BD02-4031-BCEF-DCAC62A0421E';
DECLARE @PessoaSistema uniqueidentifier = (
    SELECT id_pessoa FROM dbo.cliente WHERE id = @ClienteSistema
);
DECLARE @PessoaAdmin uniqueidentifier = (
    SELECT id_pessoa FROM dbo.usuario WHERE id = @UsuarioAdmin
);

SELECT N'/*
 Bootstrap do cliente SISTEMA extraido da producao.
 Gerado em: ' + CONVERT(nvarchar(33), SYSDATETIMEOFFSET(), 127) + N'

 ATENCAO:
 - Este arquivo contem configuracoes sensiveis copiadas da producao.
 - Execute apenas na nova base vazia e mantenha o arquivo protegido.
 - Os UUIDs originais sao preservados.
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    -- Remove indices unicos criados por relacionamentos @OneToOne antigos.
    DECLARE @dropUniqueIndexes nvarchar(max) = N'''';

    SELECT @dropUniqueIndexes = @dropUniqueIndexes
        + CASE
            WHEN i.is_unique_constraint = 1 THEN
                N''ALTER TABLE '' + QUOTENAME(OBJECT_SCHEMA_NAME(i.object_id))
                + N''.'' + QUOTENAME(OBJECT_NAME(i.object_id))
                + N'' DROP CONSTRAINT '' + QUOTENAME(i.name) + N'';''
            ELSE
                N''DROP INDEX '' + QUOTENAME(i.name)
                + N'' ON '' + QUOTENAME(OBJECT_SCHEMA_NAME(i.object_id))
                + N''.'' + QUOTENAME(OBJECT_NAME(i.object_id)) + N'';''
          END
        + NCHAR(13) + NCHAR(10)
      FROM sys.indexes i
      JOIN sys.index_columns ic
        ON ic.object_id = i.object_id
       AND ic.index_id = i.index_id
       AND ic.key_ordinal > 0
      JOIN sys.columns c
        ON c.object_id = ic.object_id
       AND c.column_id = ic.column_id
     WHERE i.is_unique = 1
       AND i.is_primary_key = 0
       AND (SELECT COUNT(*)
              FROM sys.index_columns key_column
             WHERE key_column.object_id = i.object_id
               AND key_column.index_id = i.index_id
               AND key_column.key_ordinal > 0) = 1
       AND (
            (OBJECT_NAME(i.object_id) = N''cliente'' AND c.name = N''id_segmento'')
         OR (OBJECT_NAME(i.object_id) = N''papel_tipo_cliente'' AND c.name = N''id_segmento'')
         OR (OBJECT_NAME(i.object_id) = N''tipo_documento_papel'' AND c.name = N''id_papel'')
         OR (OBJECT_NAME(i.object_id) = N''tipo_documento_tipo_cliente'' AND c.name = N''id_segmento'')
       );

    IF @dropUniqueIndexes <> N''''
        EXEC sys.sp_executesql @dropUniqueIndexes;';

SELECT N'
    -- Planos'
UNION ALL
SELECT
    N'    IF NOT EXISTS (SELECT 1 FROM dbo.plano WHERE id = ''' + CONVERT(nvarchar(36), id) + N''')
        INSERT INTO dbo.plano (id, nome, periodo, quantidade_documentos, status) VALUES (''' +
        CONVERT(nvarchar(36), id) + N''', ' +
        CASE WHEN nome IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(nome, N'''', N'''''') + N'''' END + N', ' +
        CASE WHEN periodo IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(periodo, N'''', N'''''') + N'''' END + N', ' +
        COALESCE(CONVERT(nvarchar(20), quantidade_documentos), N'NULL') + N', ' +
        CASE WHEN status IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(status, N'''', N'''''') + N'''' END + N');'
FROM dbo.plano
WHERE periodo = 'ILIMITADO'
   OR id = 'C526767A-8789-4C55-9E44-6C9B7FC83B52';

SELECT N'
    -- Segmentos'
UNION ALL
SELECT
    N'    IF NOT EXISTS (SELECT 1 FROM dbo.segmento WHERE id = ''' + CONVERT(nvarchar(36), id) + N''')
        INSERT INTO dbo.segmento (id, identificacao, nome, status) VALUES (''' +
        CONVERT(nvarchar(36), id) + N''', N''' +
        REPLACE(identificacao, N'''', N'''''') + N''', N''' +
        REPLACE(nome, N'''', N'''''') + N''', ' +
        CASE WHEN status IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(status, N'''', N'''''') + N'''' END + N');'
FROM dbo.segmento;

SELECT N'
    -- Pessoas-base. O cliente ainda nao existe, portanto id_cliente inicia nulo.'
UNION ALL
SELECT
    N'    IF NOT EXISTS (SELECT 1 FROM dbo.pessoa WHERE id = ''' + CONVERT(nvarchar(36), id) + N''')
        INSERT INTO dbo.pessoa (id, cpf_cnpj, email, nome_razao_social, tipo_pessoa, id_cliente) VALUES (''' +
        CONVERT(nvarchar(36), id) + N''', N''' +
        REPLACE(cpf_cnpj, N'''', N'''''') + N''', ' +
        CASE WHEN email IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(email, N'''', N'''''') + N'''' END + N', N''' +
        REPLACE(nome_razao_social, N'''', N'''''') + N''', ' +
        CASE WHEN tipo_pessoa IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(tipo_pessoa, N'''', N'''''') + N'''' END + N', NULL);'
FROM dbo.pessoa
WHERE id IN (@PessoaSistema, @PessoaAdmin);

SELECT N'
    -- Cliente SISTEMA'
UNION ALL
SELECT
    N'    IF NOT EXISTS (SELECT 1 FROM dbo.cliente WHERE id = ''' + CONVERT(nvarchar(36), id) + N''')
        INSERT INTO dbo.cliente (id, data_fim_contrato, data_inicio_contrato, status, id_pessoa, nao_mostrar, id_segmento, indicacao, id_plano) VALUES (''' +
        CONVERT(nvarchar(36), id) + N''', ' +
        CASE WHEN data_fim_contrato IS NULL THEN N'NULL' ELSE N'CONVERT(datetime2, ''' + CONVERT(nvarchar(33), data_fim_contrato, 126) + N''', 126)' END + N', ' +
        CASE WHEN data_inicio_contrato IS NULL THEN N'NULL' ELSE N'CONVERT(datetime2, ''' + CONVERT(nvarchar(33), data_inicio_contrato, 126) + N''', 126)' END + N', ' +
        CASE WHEN status IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(status, N'''', N'''''') + N'''' END + N', ''' +
        CONVERT(nvarchar(36), id_pessoa) + N''', ' +
        CASE WHEN nao_mostrar IS NULL THEN N'NULL' WHEN nao_mostrar = 1 THEN N'1' ELSE N'0' END + N', ' +
        CASE WHEN id_segmento IS NULL THEN N'NULL' ELSE N'''' + CONVERT(nvarchar(36), id_segmento) + N'''' END + N', ' +
        CASE WHEN indicacao IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(indicacao, N'''', N'''''') + N'''' END + N', ' +
        CASE WHEN id_plano IS NULL THEN N'NULL' ELSE N'''' + CONVERT(nvarchar(36), id_plano) + N'''' END + N');'
FROM dbo.cliente
WHERE id = @ClienteSistema;

SELECT
    N'
    UPDATE dbo.pessoa
       SET id_cliente = ''' + CONVERT(nvarchar(36), @ClienteSistema) + N'''
     WHERE id IN (''' + CONVERT(nvarchar(36), @PessoaSistema) + N''', ''' +
        CONVERT(nvarchar(36), @PessoaAdmin) + N''')
       AND (id_cliente IS NULL OR id_cliente <> ''' + CONVERT(nvarchar(36), @ClienteSistema) + N''');';

SELECT N'
    -- Usuario ADMIN'
UNION ALL
SELECT
    N'    IF NOT EXISTS (SELECT 1 FROM dbo.usuario WHERE id = ''' + CONVERT(nvarchar(36), id) + N''')
        INSERT INTO dbo.usuario (id, login, perfil, senha, status, id_pessoa, chave_privada, chave_publica, assinatura_pendente, chave_esqueceu_senha, validade_esqueceu_senha, token_assinatura, validade_token_assinatura, envio_email_tentativa_acesso, primeira_tentativa_acesso, quantidade_tentativa_acesso, id_contrato_parte_acesso) VALUES (''' +
        CONVERT(nvarchar(36), id) + N''', N''' +
        REPLACE(login, N'''', N'''''') + N''', ' +
        CASE WHEN perfil IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(perfil, N'''', N'''''') + N'''' END + N', N''' +
        REPLACE(senha, N'''', N'''''') + N''', ' +
        CASE WHEN status IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(status, N'''', N'''''') + N'''' END + N', ' +
        CASE WHEN id_pessoa IS NULL THEN N'NULL' ELSE N'''' + CONVERT(nvarchar(36), id_pessoa) + N'''' END + N', ' +
        CASE WHEN chave_privada IS NULL THEN N'NULL' ELSE CONVERT(nvarchar(max), chave_privada, 1) END + N', ' +
        CASE WHEN chave_publica IS NULL THEN N'NULL' ELSE CONVERT(nvarchar(max), chave_publica, 1) END + N', ' +
        CASE WHEN assinatura_pendente IS NULL THEN N'NULL' WHEN assinatura_pendente = 1 THEN N'1' ELSE N'0' END + N', ' +
        CASE WHEN chave_esqueceu_senha IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(chave_esqueceu_senha, N'''', N'''''') + N'''' END + N', ' +
        CASE WHEN validade_esqueceu_senha IS NULL THEN N'NULL' ELSE N'CONVERT(datetime2, ''' + CONVERT(nvarchar(33), validade_esqueceu_senha, 126) + N''', 126)' END + N', ' +
        CASE WHEN token_assinatura IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(token_assinatura, N'''', N'''''') + N'''' END + N', ' +
        CASE WHEN validade_token_assinatura IS NULL THEN N'NULL' ELSE N'CONVERT(datetime2, ''' + CONVERT(nvarchar(33), validade_token_assinatura, 126) + N''', 126)' END + N', ' +
        CASE WHEN envio_email_tentativa_acesso IS NULL THEN N'NULL' WHEN envio_email_tentativa_acesso = 1 THEN N'1' ELSE N'0' END + N', ' +
        CASE WHEN primeira_tentativa_acesso IS NULL THEN N'NULL' ELSE N'CONVERT(datetime2, ''' + CONVERT(nvarchar(33), primeira_tentativa_acesso, 126) + N''', 126)' END + N', ' +
        COALESCE(CONVERT(nvarchar(20), quantidade_tentativa_acesso), N'NULL') + N', ' +
        CASE WHEN id_contrato_parte_acesso IS NULL THEN N'NULL' ELSE N'''' + CONVERT(nvarchar(36), id_contrato_parte_acesso) + N'''' END + N');'
FROM dbo.usuario
WHERE id = @UsuarioAdmin;

SELECT N'
    -- Vinculo ADMIN / SISTEMA'
UNION ALL
SELECT
    N'    IF NOT EXISTS (SELECT 1 FROM dbo.usuario_cliente WHERE id = ''' + CONVERT(nvarchar(36), id) + N''')
        INSERT INTO dbo.usuario_cliente (id, perfil, status, id_cliente, id_usuario) VALUES (''' +
        CONVERT(nvarchar(36), id) + N''', ' +
        CASE WHEN perfil IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(perfil, N'''', N'''''') + N'''' END + N', ' +
        CASE WHEN status IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(status, N'''', N'''''') + N'''' END + N', ''' +
        CONVERT(nvarchar(36), id_cliente) + N''', ''' +
        CONVERT(nvarchar(36), id_usuario) + N''');'
FROM dbo.usuario_cliente
WHERE id_cliente = @ClienteSistema
  AND id_usuario = @UsuarioAdmin;

SELECT N'
    -- Tipos de endereco'
UNION ALL
SELECT
    N'    IF NOT EXISTS (SELECT 1 FROM dbo.tipo_endereco WHERE id = ''' + CONVERT(nvarchar(36), id) + N''')
        INSERT INTO dbo.tipo_endereco (id, identificacao, nome, status, id_cliente) VALUES (''' +
        CONVERT(nvarchar(36), id) + N''', N''' +
        REPLACE(identificacao, N'''', N'''''') + N''', N''' +
        REPLACE(nome, N'''', N'''''') + N''', ' +
        CASE WHEN status IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(status, N'''', N'''''') + N'''' END + N', ''' +
        CONVERT(nvarchar(36), id_cliente) + N''');'
FROM dbo.tipo_endereco
WHERE id_cliente = @ClienteSistema;

SELECT N'
    -- Tipos de telefone'
UNION ALL
SELECT
    N'    IF NOT EXISTS (SELECT 1 FROM dbo.tipo_telefone WHERE id = ''' + CONVERT(nvarchar(36), id) + N''')
        INSERT INTO dbo.tipo_telefone (id, identificacao, nome, status, id_cliente) VALUES (''' +
        CONVERT(nvarchar(36), id) + N''', N''' +
        REPLACE(identificacao, N'''', N'''''') + N''', N''' +
        REPLACE(nome, N'''', N'''''') + N''', ' +
        CASE WHEN status IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(status, N'''', N'''''') + N'''' END + N', ''' +
        CONVERT(nvarchar(36), id_cliente) + N''');'
FROM dbo.tipo_telefone
WHERE id_cliente = @ClienteSistema;

SELECT N'
    -- Telefones das pessoas-base'
UNION ALL
SELECT
    N'    IF NOT EXISTS (SELECT 1 FROM dbo.pessoa_telefone WHERE id = ''' + CONVERT(nvarchar(36), id) + N''')
        INSERT INTO dbo.pessoa_telefone (id, complemento, data_atualizacao, data_cadastramento, numero, status, id_pais, id_pessoa, id_tipo_telefone) VALUES (''' +
        CONVERT(nvarchar(36), id) + N''', ' +
        CASE WHEN complemento IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(complemento, N'''', N'''''') + N'''' END + N', ' +
        CASE WHEN data_atualizacao IS NULL THEN N'NULL' ELSE N'CONVERT(datetime2, ''' + CONVERT(nvarchar(33), data_atualizacao, 126) + N''', 126)' END + N', ' +
        CASE WHEN data_cadastramento IS NULL THEN N'NULL' ELSE N'CONVERT(datetime2, ''' + CONVERT(nvarchar(33), data_cadastramento, 126) + N''', 126)' END + N', ' +
        CASE WHEN numero IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(numero, N'''', N'''''') + N'''' END + N', ' +
        CASE WHEN status IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(status, N'''', N'''''') + N'''' END + N', ' +
        CASE WHEN id_pais IS NULL THEN N'NULL' ELSE N'''' + CONVERT(nvarchar(36), id_pais) + N'''' END + N', ' +
        CASE WHEN id_pessoa IS NULL THEN N'NULL' ELSE N'''' + CONVERT(nvarchar(36), id_pessoa) + N'''' END + N', ' +
        CASE WHEN id_tipo_telefone IS NULL THEN N'NULL' ELSE N'''' + CONVERT(nvarchar(36), id_tipo_telefone) + N'''' END + N');'
FROM dbo.pessoa_telefone
WHERE id_pessoa IN (@PessoaSistema, @PessoaAdmin);

SELECT N'
    -- Papeis do SISTEMA'
UNION ALL
SELECT
    N'    IF NOT EXISTS (SELECT 1 FROM dbo.papel WHERE id = ''' + CONVERT(nvarchar(36), id) + N''')
        INSERT INTO dbo.papel (id, assina, identificacao, nome, status, id_cliente) VALUES (''' +
        CONVERT(nvarchar(36), id) + N''', ' +
        CASE WHEN assina IS NULL THEN N'NULL' WHEN assina = 1 THEN N'1' ELSE N'0' END + N', ' +
        CASE WHEN identificacao IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(identificacao, N'''', N'''''') + N'''' END + N', N''' +
        REPLACE(nome, N'''', N'''''') + N''', ' +
        CASE WHEN status IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(status, N'''', N'''''') + N'''' END + N', ''' +
        CONVERT(nvarchar(36), id_cliente) + N''');'
FROM dbo.papel
WHERE id_cliente = @ClienteSistema;

SELECT N'
    -- Papeis por segmento'
UNION ALL
SELECT
    N'    IF NOT EXISTS (SELECT 1 FROM dbo.papel_tipo_cliente WHERE id = ''' + CONVERT(nvarchar(36), ptc.id) + N''')
        INSERT INTO dbo.papel_tipo_cliente (id, id_papel, id_segmento) VALUES (''' +
        CONVERT(nvarchar(36), ptc.id) + N''', ''' +
        CONVERT(nvarchar(36), ptc.id_papel) + N''', ' +
        CASE WHEN ptc.id_segmento IS NULL THEN N'NULL' ELSE N'''' + CONVERT(nvarchar(36), ptc.id_segmento) + N'''' END + N');'
FROM dbo.papel_tipo_cliente ptc
WHERE ptc.id_papel IN (
    SELECT id FROM dbo.papel WHERE id_cliente = @ClienteSistema
);

SELECT N'
    -- Tipos de documento do SISTEMA'
UNION ALL
SELECT
    N'    IF NOT EXISTS (SELECT 1 FROM dbo.tipo_documento WHERE id = ''' + CONVERT(nvarchar(36), id) + N''')
        INSERT INTO dbo.tipo_documento (id, assina, status, identificacao, nome, id_cliente, ordem, qrcode, validacao_on_line) VALUES (''' +
        CONVERT(nvarchar(36), id) + N''', ' +
        CASE WHEN assina IS NULL THEN N'NULL' WHEN assina = 1 THEN N'1' ELSE N'0' END + N', ' +
        CASE WHEN status IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(status, N'''', N'''''') + N'''' END + N', ' +
        CASE WHEN identificacao IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(identificacao, N'''', N'''''') + N'''' END + N', N''' +
        REPLACE(nome, N'''', N'''''') + N''', ''' +
        CONVERT(nvarchar(36), id_cliente) + N''', ' +
        COALESCE(CONVERT(nvarchar(20), ordem), N'NULL') + N', ' +
        CASE WHEN qrcode IS NULL THEN N'NULL' WHEN qrcode = 1 THEN N'1' ELSE N'0' END + N', ' +
        CASE WHEN validacao_on_line IS NULL THEN N'NULL' WHEN validacao_on_line = 1 THEN N'1' ELSE N'0' END + N');'
FROM dbo.tipo_documento
WHERE id_cliente = @ClienteSistema;

SELECT N'
    -- Papeis permitidos por tipo de documento'
UNION ALL
SELECT
    N'    IF NOT EXISTS (SELECT 1 FROM dbo.tipo_documento_papel WHERE id = ''' + CONVERT(nvarchar(36), tdp.id) + N''')
        INSERT INTO dbo.tipo_documento_papel (id, id_tipo_documento, id_papel, certificate, token) VALUES (''' +
        CONVERT(nvarchar(36), tdp.id) + N''', ' +
        CASE WHEN tdp.id_tipo_documento IS NULL THEN N'NULL' ELSE N'''' + CONVERT(nvarchar(36), tdp.id_tipo_documento) + N'''' END + N', ''' +
        CONVERT(nvarchar(36), tdp.id_papel) + N''', ' +
        CASE WHEN tdp.certificate IS NULL THEN N'NULL' WHEN tdp.certificate = 1 THEN N'1' ELSE N'0' END + N', ' +
        CASE WHEN tdp.token IS NULL THEN N'NULL' WHEN tdp.token = 1 THEN N'1' ELSE N'0' END + N');'
FROM dbo.tipo_documento_papel tdp
WHERE tdp.id_tipo_documento IN (
        SELECT id FROM dbo.tipo_documento WHERE id_cliente = @ClienteSistema
    )
  AND tdp.id_papel IN (
        SELECT id FROM dbo.papel WHERE id_cliente = @ClienteSistema
    );

SELECT N'
    -- Tipos de documento por segmento'
UNION ALL
SELECT
    N'    IF NOT EXISTS (SELECT 1 FROM dbo.tipo_documento_tipo_cliente WHERE id = ''' + CONVERT(nvarchar(36), tdtc.id) + N''')
        INSERT INTO dbo.tipo_documento_tipo_cliente (id, id_tipo_documento, id_segmento) VALUES (''' +
        CONVERT(nvarchar(36), tdtc.id) + N''', ''' +
        CONVERT(nvarchar(36), tdtc.id_tipo_documento) + N''', ' +
        CASE WHEN tdtc.id_segmento IS NULL THEN N'NULL' ELSE N'''' + CONVERT(nvarchar(36), tdtc.id_segmento) + N'''' END + N');'
FROM dbo.tipo_documento_tipo_cliente tdtc
WHERE tdtc.id_tipo_documento IN (
    SELECT id FROM dbo.tipo_documento WHERE id_cliente = @ClienteSistema
);

SELECT N'
    -- Definicoes de parametros'
UNION ALL
SELECT
    N'    IF NOT EXISTS (SELECT 1 FROM dbo.sistema_tipo_atributo WHERE id = ''' + CONVERT(nvarchar(36), id) + N''')
        INSERT INTO dbo.sistema_tipo_atributo (id, tipo_atributo, descricao, tipo_valor) VALUES (''' +
        CONVERT(nvarchar(36), id) + N''', ' +
        CASE WHEN tipo_atributo IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(tipo_atributo, N'''', N'''''') + N'''' END + N', ' +
        CASE
            WHEN tipo_atributo = 'TERMO_DE_USO_SISTEMA' THEN N'N''Caminho do termo de uso do sistema no storage'''
            WHEN tipo_atributo = 'TERMO_DE_PRIVACIDADE_SISTEMA' THEN N'N''Caminho do termo de privacidade do sistema no storage'''
            WHEN descricao IS NULL THEN N'NULL'
            ELSE N'N''' + REPLACE(descricao, N'''', N'''''') + N''''
        END + N', ' +
        CASE
            WHEN tipo_atributo IN ('TERMO_DE_USO_SISTEMA', 'TERMO_DE_PRIVACIDADE_SISTEMA') THEN N'N''TEXT_SIMPLE'''
            WHEN tipo_valor IS NULL THEN N'NULL'
            ELSE N'N''' + REPLACE(tipo_valor, N'''', N'''''') + N''''
        END + N');'
FROM dbo.sistema_tipo_atributo
WHERE tipo_atributo NOT IN (
    'EMAIL_CHAVE_ACESSO_PENDENTE',
    'EMAIL_CONTRATO_ASSINADO_PARTE'
);

SELECT N'
    -- Valores, templates e credenciais do SISTEMA'
UNION ALL
SELECT
    N'    IF NOT EXISTS (SELECT 1 FROM dbo.sistema_atributo WHERE id = ''' + CONVERT(nvarchar(36), sa.id) + N''')
        INSERT INTO dbo.sistema_atributo (id, data_alteracao, valor_atributo, id_cliente, id_tipo_atributo, status) VALUES (''' +
        CONVERT(nvarchar(36), sa.id) + N''', ' +
        CASE WHEN sa.data_alteracao IS NULL THEN N'NULL' ELSE N'CONVERT(datetime2, ''' + CONVERT(nvarchar(33), sa.data_alteracao, 126) + N''', 126)' END + N', ' +
        CASE
            WHEN sta.tipo_atributo = 'TERMO_DE_USO_SISTEMA'
                THEN N'N''assina.net/termos/assina.net-termo-uso-sistema.pdf'''
            WHEN sta.tipo_atributo = 'TERMO_DE_PRIVACIDADE_SISTEMA'
                THEN N'N''assina.net/termos/assina.net-termo-privacidade-sistema.pdf'''
            WHEN sa.valor_atributo IS NULL THEN N'NULL'
            ELSE N'N''' +
                REPLACE(
                    REPLACE(
                        REPLACE(sa.valor_atributo, N'''', N''''''),
                        NCHAR(13), N''' + NCHAR(13) + N'''
                    ),
                    NCHAR(10), N''' + NCHAR(10) + N'''
                ) + N''''
        END + N', ' +
        CASE WHEN sa.id_cliente IS NULL THEN N'NULL' ELSE N'''' + CONVERT(nvarchar(36), sa.id_cliente) + N'''' END + N', ''' +
        CONVERT(nvarchar(36), sa.id_tipo_atributo) + N''', ' +
        CASE WHEN sa.status IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(sa.status, N'''', N'''''') + N'''' END + N');'
FROM dbo.sistema_atributo sa
JOIN dbo.sistema_tipo_atributo sta ON sta.id = sa.id_tipo_atributo
WHERE (sa.id_cliente = @ClienteSistema
    OR sa.id_cliente IS NULL)
  AND sta.tipo_atributo NOT IN (
      'EMAIL_CHAVE_ACESSO_PENDENTE',
      'EMAIL_CONTRATO_ASSINADO_PARTE'
  );

SELECT N'
    -- Aceites de termos do ADMIN'
UNION ALL
SELECT
    N'    IF NOT EXISTS (SELECT 1 FROM dbo.usuario_termo WHERE id = ''' + CONVERT(nvarchar(36), id) + N''')
        INSERT INTO dbo.usuario_termo (id, data_aceite, id_sistema_atributo, id_usuario, ip) VALUES (''' +
        CONVERT(nvarchar(36), id) + N''', ' +
        CASE WHEN data_aceite IS NULL THEN N'NULL' ELSE N'CONVERT(datetime2, ''' + CONVERT(nvarchar(33), data_aceite, 126) + N''', 126)' END + N', ''' +
        CONVERT(nvarchar(36), id_sistema_atributo) + N''', ''' +
        CONVERT(nvarchar(36), id_usuario) + N''', ' +
        CASE WHEN ip IS NULL THEN N'NULL' ELSE N'N''' + REPLACE(ip, N'''', N'''''') + N'''' END + N');'
FROM dbo.usuario_termo
WHERE id_usuario = @UsuarioAdmin
  AND id_sistema_atributo IN (
      SELECT id
        FROM dbo.sistema_atributo
       WHERE id_cliente = @ClienteSistema
          OR id_cliente IS NULL
  );

SELECT N'
    COMMIT TRANSACTION;
    PRINT ''Bootstrap do SISTEMA concluido com sucesso.'';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO';
