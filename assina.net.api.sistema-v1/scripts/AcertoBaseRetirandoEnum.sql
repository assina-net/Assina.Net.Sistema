
select  tido.*,  codo.id_tipo_documento, codo.tipo_documento
from contrato_documento codo
join tipo_documento tido on  codo.tipo_documento = tido.identificacao
where id_tipo_documento is null

begin tran

update contrato_documento  set
 id_tipo_documento = tido.id
from contrato_documento codo
join tipo_documento tido on  codo.tipo_documento = tido.identificacao
where id_tipo_documento is null

-- commit

select * from tipo_documento


select * from papel

update papel set
id_cliente = ( select id from cliente where tipo_cliente = 'SISTEMA' ),
status = 'ATIVO',
assina = 1

update tipo_documento set
id_cliente = ( select id from cliente where tipo_cliente = 'SISTEMA' )



/*
insert into papel( id, id_cliente, identificacao, nome, assina, status )
select newid(), id_cliente, identificacao, nome, assina, status
from 
( 
select  distinct clie.id id_cliente , codp.papel identificacao, codp.papel nome, 1 assina , 'ATIVO'  status
from Contrato_Documento_Papel codp
join cliente clie on clie.tipo_cliente = 'SISTEMA'
) lista
*/

select  *
from Contrato_Documento_Papel codo
join papel pape on papel = pape.identificacao
where id_papel is null

begin tran


alter table contrato_documento_papel alter column papel varchar(255) null


update Contrato_Documento_Papel  set
 id_papel = pape.id
from Contrato_Documento_Papel codo
join  papel pape on papel = pape.identificacao
where id_papel is null

-- commit




select  *
from Contrato_Parte_Papel codo
join papel pape on papel = pape.identificacao
where id_papel is null

begin tran

alter table contrato_parte_papel alter column papel varchar(255) null

update Contrato_Parte_Papel  set
 id_papel = pape.id
from Contrato_Parte_Papel codo
join  papel pape on papel = pape.identificacao
where id_papel is null

-- commit


select  *
from Pessoa_Fisica_Papel codo
join papel pape on papel = pape.identificacao
where id_papel is null

begin tran

update Pessoa_Fisica_Papel  set
 id_papel = pape.id
from Pessoa_Fisica_Papel codo
join  papel pape on papel = pape.identificacao
where id_papel is null

-- commit	





INSERT [dbo].[papel_tipo_cliente] ([id], [tipo_cliente], [id_papel])
VALUES (N'4eefad55-4300-4b96-82ea-02bd93f0a577', N'FIDC', N'14d57347-afba-4de3-bb62-8cbe18a496c9'),
       (N'44d506c9-a241-43a2-8fe8-2dd071c124e4', N'CORRETORASEGUROS', N'cd609d0a-3b15-4610-ae2e-2f3253b53a14'),
	   (N'bb25baeb-a8fa-4174-a341-31a2051d3d71', N'FACTORING', N'ebff2d5e-eaf4-41ae-9b31-aa977f47eb3d'),
	   (N'197d7b1d-97af-46fb-bde2-44f234266e75', N'FIDC', N'164c0b25-3d19-4af4-8554-09d05db6e9f0'),
	   (N'f742029e-e1ab-4a0f-a54b-5212e3b28eda', N'FACTORING', N'5f813c44-cf7b-47ca-b8bb-6b7628089abd'),
	   (N'cb3f728c-c219-49cc-9572-541684106b97', N'FACTORING', N'164c0b25-3d19-4af4-8554-09d05db6e9f0'),
	   (N'00d4fa36-b521-4464-8761-5864cda4434b', N'FACTORING', N'14d57347-afba-4de3-bb62-8cbe18a496c9'),
	   (N'd8e24733-e18a-470c-9a96-7aa12dbf26d6', N'CORRETORASEGUROS', N'7d932008-cebe-4390-a4c4-bc51582b89d0'),
	   (N'76099790-3324-4f63-9569-7e60ac06be03', N'FACTORING', N'5ea55bdf-8062-40c8-b7f3-8e9025fcdf0f'),
	   (N'f07dc65c-6853-4293-8b4f-943e77ad1498', N'FIDC', N'ebff2d5e-eaf4-41ae-9b31-aa977f47eb3d'),
	   (N'3aab5346-664b-4daf-ac20-99f94973a8ea', N'FACTORING', N'13f3f6a3-04e6-4378-a38e-82821222d1a2'),
	   (N'00947380-1573-44fb-80f9-9f072b65257a', N'FACTORING', N'652d795e-a4e5-49ec-87be-fa94b0eb0921'),
	   (N'31ed560c-cd3a-4d4b-a201-a02f6e516ed9', N'FIDC', N'5ea55bdf-8062-40c8-b7f3-8e9025fcdf0f'),
	   (N'00683e95-9569-47a1-8eaa-be01e653d84a', N'FIDC', N'b174f3c3-0719-4e2f-b7aa-83dd701c5076'),
	   (N'3a2aec4c-7203-4899-9764-cc90c7101ffd', N'FIDC', N'652d795e-a4e5-49ec-87be-fa94b0eb0921'),
	   (N'fba81c76-2d85-4320-84bf-e21b5416de69', N'FIDC', N'5f813c44-cf7b-47ca-b8bb-6b7628089abd'),
	   (N'e44760d9-ddca-4c5c-b15c-e6862b296f0f', N'FIDC', N'13f3f6a3-04e6-4378-a38e-82821222d1a2'),
	   (N'0f61e6fe-ff3d-4403-96db-f938a655b4eb', N'FACTORING', N'b174f3c3-0719-4e2f-b7aa-83dd701c5076')
GO


INSERT [dbo].[tipo_documento_tipo_cliente] ([id], [tipo_cliente], [id_tipo_documento])
VALUES (N'6263eb2a-c8ae-4914-8b5c-0dff49d2f672', N'FACTORING', N'2337b534-eccc-4376-a125-a6243fe2f3bf'),
	   (N'f6e2f635-ea4c-4e93-b274-2f859e844a0a', N'FIDC', N'080d1115-3549-4855-a801-5767b51c7289'),
	   (N'949aff8f-a8ee-4443-b8e1-36afbd169f3b', N'CORRETORASEGUROS', N'f555a993-1de7-4778-a641-d51b4c9cace1'),
	   (N'd0269153-6f03-4164-90ba-6a8567cef45a', N'CONTRATOSIMPLES', N'88b08b18-1046-4744-8e10-6236e3d68984'),
	   (N'25eaefe5-d683-405a-a1f7-6c517063c447', N'FACTORING', N'16f59443-14de-4e91-988e-4652530393ec'),
	   (N'07ac00f0-cd03-473e-aa5d-6cfdf74e3e75', N'CORRETORASEGUROS', N'ddc357d6-ea87-4b30-b640-c4e9a55a1dbe'),
	   (N'b6dea8c3-ca76-4251-9b25-77cc7a74ca82', N'FACTORING', N'080d1115-3549-4855-a801-5767b51c7289'),
	   (N'bc692706-2651-4c42-a0c1-aeda6a8e4d9b', N'FIDC', N'c2bf2409-e37f-45b9-9b68-f1e34c8f07a9'),
	   (N'351065d3-b3be-4a92-9544-bb0da2e23433', N'FIDC', N'2337b534-eccc-4376-a125-a6243fe2f3bf'),
	   (N'c74f335a-0eb0-4bd4-a886-dae70a6b8a99', N'FACTORING', N'c2bf2409-e37f-45b9-9b68-f1e34c8f07a9'),
	   (N'50c095bf-316c-41f3-b743-ebcfc9ccdc71', N'FIDC', N'16f59443-14de-4e91-988e-4652530393ec')
GO
