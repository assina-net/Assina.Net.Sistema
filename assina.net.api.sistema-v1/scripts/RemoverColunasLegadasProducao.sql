/*
 Remove colunas que nao possuem mais propriedades nas entidades JPA atuais.

 ATENCAO:
 - Execute primeiro em uma copia restaurada da producao.
 - Faca backup antes da execucao em producao.
 - A operacao e destrutiva e nao deve ser executada enquanto uma versao antiga
   da API que ainda utilize essas colunas estiver ativa.
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF EXISTS (
        SELECT 1
          FROM sys.index_columns ic
          JOIN sys.columns c
            ON c.object_id = ic.object_id
           AND c.column_id = ic.column_id
         WHERE (OBJECT_NAME(c.object_id) = N'pessoa' AND c.name = N'celular')
            OR (OBJECT_NAME(c.object_id) = N'cliente' AND c.name = N'tipo_cliente')
            OR (OBJECT_NAME(c.object_id) = N'papel_tipo_cliente' AND c.name = N'tipo_cliente')
            OR (OBJECT_NAME(c.object_id) = N'tipo_documento' AND c.name IN (N'tipo_documento', N'nao_assina'))
            OR (OBJECT_NAME(c.object_id) = N'tipo_documento_papel' AND c.name = N'papel')
            OR (OBJECT_NAME(c.object_id) = N'tipo_documento_tipo_cliente' AND c.name = N'tipo_cliente')
    )
        THROW 50001, 'Existem indices associados as colunas legadas. Revise-os antes de executar o DROP.', 1;

    IF EXISTS (
        SELECT 1
          FROM sys.check_constraints cc
         WHERE (OBJECT_NAME(cc.parent_object_id) = N'pessoa' AND cc.definition LIKE N'%[[]celular[]]%')
            OR (OBJECT_NAME(cc.parent_object_id) = N'cliente' AND cc.definition LIKE N'%[[]tipo_cliente[]]%')
            OR (OBJECT_NAME(cc.parent_object_id) = N'papel_tipo_cliente' AND cc.definition LIKE N'%[[]tipo_cliente[]]%')
            OR (OBJECT_NAME(cc.parent_object_id) = N'tipo_documento'
                AND (cc.definition LIKE N'%[[]tipo_documento[]]%' OR cc.definition LIKE N'%[[]nao_assina[]]%'))
            OR (OBJECT_NAME(cc.parent_object_id) = N'tipo_documento_papel' AND cc.definition LIKE N'%[[]papel[]]%')
            OR (OBJECT_NAME(cc.parent_object_id) = N'tipo_documento_tipo_cliente' AND cc.definition LIKE N'%[[]tipo_cliente[]]%')
    )
        THROW 50002, 'Existem CHECK constraints associadas as colunas legadas. Revise-as antes de executar o DROP.', 1;

    DECLARE @sql nvarchar(max) = N'';

    SELECT @sql = @sql
        + N'ALTER TABLE ' + QUOTENAME(OBJECT_SCHEMA_NAME(dc.parent_object_id))
        + N'.' + QUOTENAME(OBJECT_NAME(dc.parent_object_id))
        + N' DROP CONSTRAINT ' + QUOTENAME(dc.name) + N';' + NCHAR(13) + NCHAR(10)
      FROM sys.default_constraints dc
      JOIN sys.columns c
        ON c.object_id = dc.parent_object_id
       AND c.column_id = dc.parent_column_id
     WHERE (OBJECT_NAME(c.object_id) = N'pessoa' AND c.name = N'celular')
        OR (OBJECT_NAME(c.object_id) = N'cliente' AND c.name = N'tipo_cliente')
        OR (OBJECT_NAME(c.object_id) = N'papel_tipo_cliente' AND c.name = N'tipo_cliente')
        OR (OBJECT_NAME(c.object_id) = N'tipo_documento' AND c.name IN (N'tipo_documento', N'nao_assina'))
        OR (OBJECT_NAME(c.object_id) = N'tipo_documento_papel' AND c.name = N'papel')
        OR (OBJECT_NAME(c.object_id) = N'tipo_documento_tipo_cliente' AND c.name = N'tipo_cliente');

    IF @sql <> N''
        EXEC sys.sp_executesql @sql;

    IF COL_LENGTH(N'dbo.pessoa', N'celular') IS NOT NULL
        ALTER TABLE dbo.pessoa DROP COLUMN celular;

    IF COL_LENGTH(N'dbo.cliente', N'tipo_cliente') IS NOT NULL
        ALTER TABLE dbo.cliente DROP COLUMN tipo_cliente;

    IF COL_LENGTH(N'dbo.papel_tipo_cliente', N'tipo_cliente') IS NOT NULL
        ALTER TABLE dbo.papel_tipo_cliente DROP COLUMN tipo_cliente;

    IF COL_LENGTH(N'dbo.tipo_documento', N'tipo_documento') IS NOT NULL
        ALTER TABLE dbo.tipo_documento DROP COLUMN tipo_documento;

    IF COL_LENGTH(N'dbo.tipo_documento', N'nao_assina') IS NOT NULL
        ALTER TABLE dbo.tipo_documento DROP COLUMN nao_assina;

    IF COL_LENGTH(N'dbo.tipo_documento_papel', N'papel') IS NOT NULL
        ALTER TABLE dbo.tipo_documento_papel DROP COLUMN papel;

    IF COL_LENGTH(N'dbo.tipo_documento_tipo_cliente', N'tipo_cliente') IS NOT NULL
        ALTER TABLE dbo.tipo_documento_tipo_cliente DROP COLUMN tipo_cliente;

    COMMIT TRANSACTION;
    PRINT 'Colunas legadas removidas com sucesso.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
