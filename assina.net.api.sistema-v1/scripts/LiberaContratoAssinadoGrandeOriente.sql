
begin tran

-- commit
-- rollback
declare @id uniqueidentifier , 
        @cpf_cnpj varchar(20) = '',  -- '71481885804', null
		@status_contrato varchar(50) =  'GERANDOASSINATURAS',  -- 'LIBERADOASSINATURA' NAOLIBERADOASSINTAURA  ,
		@identificador varchar(50)= 'TESTE ASS'
-- TESTE LIBERAÇÃO 01_B'
		

select @id = idp
from contrato
where  identificador =  @identificador

 select *
 from contrato
 where  id = @id
/* 
select *
from contrato_log
where  id_contrato = @id

select *
from contrato_documento
where  id_contrato = @id

select *
from contrato_parte
where  id_contrato = @id
and ( cpf_cnpj = @cpf_cnpj or @cpf_cnpj is null or @cpf_cnpj = '')


delete from contrato_parte_documento 
where id_contrato_parte_papel in 
(   
	select copp.id
	from contrato_parte copa
	inner join contrato_parte_papel  copp on ( copa.id = copp.id_contrato_parte )
	where  id_contrato = @id
	and (cpf_cnpj = @cpf_cnpj or @cpf_cnpj is null or @cpf_cnpj = '')
)

delete
from contrato_log
where  id_contrato = @id
and ( log like '%assinou%' or
log like '%assinado%' or
log_sistema = 1 )

*/

update contrato_documento set
	documento_assinado = null	
	--dados_origem = SUBSTRING( dados_origem, 1, CHARINDEX('"assinaturas"',dados_origem )) + 'assinaturas":[]}',
	--status_documento = 'NAOASSINADO'
where  id_contrato = @id

/*
update contrato_documento set	
	dados_origem = null
where  id_contrato = @id
and dados_origem = 'assinaturas":[]}'



update contrato_parte set
       --status_assinatura = 'NAOASSINADO'  ,
	   chave_acesso = case when @status_contrato =  'LIBERADOASSINATURA' then chave_acesso  else null end,
	   token_assinatura = case when @status_contrato =  'LIBERADOASSINATURA' then token_assinatura  else null end,
	   status_assinatura = case when @status_contrato =  'LIBERADOASSINATURA' then 'NAOASSINADO'  else 'NAOLIBERADO' end
where  id_contrato = @id
and( cpf_cnpj = @cpf_cnpj or @cpf_cnpj is null or @cpf_cnpj = '')
*/

 update contrato set
 status_contrato = @status_contrato 
 where  id = @id


