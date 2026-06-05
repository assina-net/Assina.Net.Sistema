select LEN( documento_assinado ), LEN( documento_original ),   
dados_origem, SUBSTRING( dados_origem, 1, CHARINDEX('"assinaturas"',dados_origem )) + 'assinaturas":[]}'
 from contrato_documento
 where nome_documento like '82525%'


 select LEN( documento_assinado ), LEN( documento_original ),   
dados_origem, SUBSTRING( dados_origem, 1, CHARINDEX('"assinaturas"',dados_origem )) + 'assinaturas":[]}'
 from contrato_documento
 where nome_documento like '%aditi%'

 

select *
 from contrato_parte_papel
 where id_contrato_parte in ( 
 '7B1A60B0-4D07-4639-8954-2539596FC44E',
'01241B3A-D5CC-4A94-8EAC-0F17CA8D321B'
 )


 select *
  from cliente
  where id= 'FE9D3DFE-B9AA-425A-85A8-31E7B4F3F6B9'


  select *
   from pessoa
   where id = '835E0DDF-5C04-4A41-AD44-475A56609CCB'

 select status_assinatura,  *
from contrato_parte
where  id_contrato = 'B218D04E-06E2-4B65-8DE2-284FE0AF5D93'
and cpf_cnpj = '71481885804'
order by duplicatas desc



 select id_contrato_parte_assinando,  *
from contrato_documento
where  id_contrato = 'B218D04E-06E2-4B65-8DE2-284FE0AF5D93'
and id= '9B83A72C-325A-4D93-982F-805B599A2962'



update contrato_documento set
 id_contrato_parte_assinando = null
where  id_contrato = 'B218D04E-06E2-4B65-8DE2-284FE0AF5D93'
and id= '9B83A72C-325A-4D93-982F-805B599A2962'


select status_contrato, *
from contrato
where  id = 'B218D04E-06E2-4B65-8DE2-284FE0AF5D93'


select count(*)
from contrato_log
where  id_contrato = 'B218D04E-06E2-4B65-8DE2-284FE0AF5D93'


select DATEDIFF( MINUTE , min(data_log) , max(data_log))
from contrato_log
where  id_contrato = 'B218D04E-06E2-4B65-8DE2-284FE0AF5D93'
and not id_contrato_documento is null



select *
 from contrato_parte_papel
 where id_contrato_parte in (
 '01241B3A-D5CC-4A94-8EAC-0F17CA8D321B','DA9C9400-D7DE-457C-90C2-1493C51DDDD9','7B1A60B0-4D07-4639-8954-2539596FC44E','26D31943-37A6-4334-8E50-3B3377FF0D4B','B3061ADF-55A7-4540-B924-3C00132A78AE'
 )