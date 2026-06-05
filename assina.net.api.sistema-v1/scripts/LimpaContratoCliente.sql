
declare @id uniqueidentifier 

select @id = clie.id
from cliente clie
inner join pessoa pess on clie.id_pessoa = pess.id
where nome_razao_social like 'APORE%'

select  pess.*
from cliente clie
inner join pessoa pess on clie.id_pessoa = pess.id
where clie.id =  @id



begin tran

DECLARE vendor_cursor CURSOR FOR   
SELECT id
FROM contrato
where id_cliente = @id
  
OPEN vendor_cursor  
  
FETCH NEXT FROM vendor_cursor   
INTO @id
  
WHILE @@FETCH_STATUS = 0  
BEGIN  

	select *
	from contrato
	where @id = id

	delete from contrato_log where id_contrato_documento in ( select id from contrato_documento where id_contrato= @id ) 
	delete from contrato_parte_documento where id_contrato_documento in ( select id from contrato_documento where id_contrato= @id ) 
	delete from contrato_documento_papel where id_contrato_documento in ( select id from contrato_documento where id_contrato= @id ) 
	delete from contrato_documento where id_contrato= @id
	delete from contrato_parte_papel where id_contrato_parte in ( select id from contrato_parte where id_contrato= @id)
	delete from contrato_parte where id_contrato= @id
	delete from contrato_log where id_contrato= @id
	delete from contrato where id= @id

    FETCH NEXT FROM vendor_cursor   
    INTO @id
END   
CLOSE vendor_cursor;  
DEALLOCATE vendor_cursor;  




 -- commit
 -- rollback