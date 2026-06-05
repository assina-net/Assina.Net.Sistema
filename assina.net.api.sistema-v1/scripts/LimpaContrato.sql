/*
72818
TESTE5556
TESTE
TESTE EMAIL
TESTE CONTRATO REPETIDO
TESTE EMAIL PARAMETROS
TESTE
*/



 declare @id uniqueidentifier 

 select @id = id
 from contrato
 where  identificador = '72818'


 select *
 from contrato
 where @id = id


 begin tran

 delete from contrato_log where id_contrato_documento in ( select id from contrato_documento where id_contrato= @id ) 
 delete from contrato_parte_documento where id_contrato_documento in ( select id from contrato_documento where id_contrato= @id ) 
 delete from contrato_documento_papel where id_contrato_documento in ( select id from contrato_documento where id_contrato= @id ) 
 delete from contrato_documento where id_contrato= @id
 delete from contrato_parte_papel where id_contrato_parte in ( select id from contrato_parte where id_contrato= @id)
 delete from contrato_parte where id_contrato= @id
 delete from contrato_log where id_contrato= @id
 delete from contrato where id= @id

 -- commit
 -- rollback