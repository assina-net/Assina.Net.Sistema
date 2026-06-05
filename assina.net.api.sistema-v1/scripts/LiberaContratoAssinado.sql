
begin tran

-- commit

declare @id uniqueidentifier , 
        @cpf_cnpj varchar(20) = '',  -- '71481885804', null
		@status_contrato varchar(50) =  'GERANDOASSINATURAS',  -- 'LIBERADOASSINATURA' NAOLIBERADOASSINTAURA  ,
		@identificador varchar(50)= 'TROCA DE MARCA DO ITEM 3'
-- TESTE LIBERAÇÃO 01_B'
		

select @id = id
from contrato
where   identificador like '%' + @identificador + '%'

 select *
 from contrato
 where  id = @id
 
select *
from contrato_documento
where  id_contrato = @id

select *
from contrato_parte
where  id_contrato = @id
and ( cpf_cnpj = @cpf_cnpj or @cpf_cnpj is null or @cpf_cnpj = '')




update contrato_documento set
	documento_assinado = null,		
	dados_origem = SUBSTRING( dados_origem, 1, CHARINDEX('"assinaturas"',dados_origem )) + 'assinaturas":[]}',
	status_documento = 'NAOASSINADO'
where  id_contrato = @id



 update contrato set
 status_contrato = @status_contrato 
 where  id = @id


