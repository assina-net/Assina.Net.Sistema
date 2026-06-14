declare @id_cliente_sistema uniqueidentifier;

select top 1 @id_cliente_sistema = cliente.id
from cliente
join segmento on segmento.id = cliente.id_segmento
where segmento.identificacao = 'SISTEMA';

if @id_cliente_sistema is null
    throw 50001, 'Cliente do segmento SISTEMA nao encontrado.', 1;

declare @atributos table (
    tipo_atributo varchar(255) not null,
    descricao varchar(255) not null,
    tipo_valor varchar(255) not null,
    valor_atributo nvarchar(max) not null
);

insert into @atributos (tipo_atributo, descricao, tipo_valor, valor_atributo)
values
    (
        'AZURE_STORAGE_CONNECTION_STRING',
        'Connection string do Azure Blob Storage',
        'TEXT_SIMPLE',
        '<INFORME_A_CONNECTION_STRING>'
    ),
    (
        'AZURE_STORAGE_CONTAINER_NAME',
        'Nome do container do Azure Blob Storage',
        'TEXT_SIMPLE',
        'assinanet-container1'
    );

declare @tipo_atributo varchar(255);
declare @descricao varchar(255);
declare @tipo_valor varchar(255);
declare @valor_atributo nvarchar(max);
declare @id_tipo_atributo uniqueidentifier;

declare atributos_cursor cursor local fast_forward for
select tipo_atributo, descricao, tipo_valor, valor_atributo
from @atributos;

open atributos_cursor;
fetch next from atributos_cursor
into @tipo_atributo, @descricao, @tipo_valor, @valor_atributo;

while @@fetch_status = 0
begin
    set @id_tipo_atributo = null;

    select @id_tipo_atributo = id
    from sistema_tipo_atributo
    where tipo_atributo = @tipo_atributo;

    if @id_tipo_atributo is null
    begin
        set @id_tipo_atributo = newid();

        insert into sistema_tipo_atributo (
            id,
            tipo_atributo,
            descricao,
            tipo_valor
        )
        values (
            @id_tipo_atributo,
            @tipo_atributo,
            @descricao,
            @tipo_valor
        );
    end;

    if exists (
        select 1
        from sistema_atributo
        where id_tipo_atributo = @id_tipo_atributo
          and id_cliente = @id_cliente_sistema
    )
    begin
        if @tipo_atributo <> 'AZURE_STORAGE_CONNECTION_STRING'
           or @valor_atributo <> '<INFORME_A_CONNECTION_STRING>'
        begin
            update sistema_atributo
            set valor_atributo = @valor_atributo,
                data_alteracao = getdate(),
                status = 'ATIVO'
            where id_tipo_atributo = @id_tipo_atributo
              and id_cliente = @id_cliente_sistema;
        end;
    end
    else
    begin
        if @tipo_atributo = 'AZURE_STORAGE_CONNECTION_STRING'
           and @valor_atributo = '<INFORME_A_CONNECTION_STRING>'
            throw 50002, 'Informe a connection string antes de executar o script.', 1;

        insert into sistema_atributo (
            id,
            id_tipo_atributo,
            valor_atributo,
            id_cliente,
            data_alteracao,
            status
        )
        values (
            newid(),
            @id_tipo_atributo,
            @valor_atributo,
            @id_cliente_sistema,
            getdate(),
            'ATIVO'
        );
    end;

    fetch next from atributos_cursor
    into @tipo_atributo, @descricao, @tipo_valor, @valor_atributo;
end;

close atributos_cursor;
deallocate atributos_cursor;
