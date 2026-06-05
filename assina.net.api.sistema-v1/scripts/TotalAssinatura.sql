select pess.nome_razao_social, pape.nome, count(*) total_papel
 from  contrato cont
 inner join cliente clie on cont.id_cliente = clie.id
 inner join pessoa pess on clie.id_pessoa = pess.id
 inner join contrato_documento codo on  cont.id = codo.id_contrato 
 inner join contrato_parte_documento copd on codo.id = copd.id_contrato_documento
 inner join contrato_parte_papel copp on copd.id_contrato_parte_papel = copp.id 
 inner join contrato_parte copa on copp.id_contrato_parte = copa.id and copa.status_assinatura = 'ASSINADO' and copa.status = 'ATIVO'
 inner join papel pape on copp.id_papel = pape.id
 where convert( varchar, copd.data_assinatura, 112) between '20201201' and '20201206'
 and cont.status_contrato in ( 'ASSINADO', 'GERANDOASSINATURAS' )
 group by pess.nome_razao_social, pape.nome
 order by pess.nome_razao_social, pape.nome



 select pess.nome_razao_social, count(*) total_contrato
 from  contrato cont
 inner join cliente clie on cont.id_cliente = clie.id
 inner join pessoa pess on clie.id_pessoa = pess.id
 where cont.status_contrato in ( 'ASSINADO', 'GERANDOASSINATURAS' )
 and cont.id in 
 ( 
	select codo.id_contrato 
    from contrato_documento codo 
	inner join contrato_parte_documento copd on codo.id = copd.id_contrato_documento
	where convert( varchar, copd.data_assinatura, 112) between '20201201' and '20201206'	
 )
 group by pess.nome_razao_social
 order by pess.nome_razao_social
 
