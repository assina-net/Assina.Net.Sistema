
/* 

delete from sistema_atributo

delete from sistema_tipo_atributo

*/

if ( exists ( Select name From sys.objects Where type = 'P' and name = 'saTipoAtributo' ) ) 
begin
	drop procedure saTipoAtributo ;
end
GO

create procedure saTipoAtributo ( @tipo_atributo varchar(255), @descricao varchar(255), @tipo_valor varchar(255) )
as
begin

	if (  not exists ( select * from sistema_tipo_atributo where tipo_atributo = @tipo_atributo  ) )  
	begin
		insert into sistema_tipo_atributo ( id, tipo_atributo, descricao, tipo_valor ) values ( newid(), @tipo_atributo, @descricao, @tipo_valor );
	end

end
GO

exec saTipoAtributo  'EMAIL_SENDER_HOST', null,null ;
exec saTipoAtributo  'EMAIL_SENDER_PORT', null,null;
exec saTipoAtributo  'EMAIL_SENDER_USERNAME', null,null;
exec saTipoAtributo  'EMAIL_SENDER_PASSWORD', null,null;
exec saTipoAtributo  'EMAIL_SENDER_AUTHENTICATE', null,null;
exec saTipoAtributo  'EMAIL_SENDER_TLS_ENABLE', null,null;
exec saTipoAtributo  'EMAIL_SENDER_TLS_REQUIRED', null,null;
exec saTipoAtributo  'EMAIL_SENDER_SSL_ENABLE', null,null;
exec saTipoAtributo  'EMAIL_SENDER_PROTOCOL', null,null;

exec saTipoAtributo  'EMAIL_NOVO_USUARIO', null,null;
exec saTipoAtributo  'EMAIL_ESQUECEU_SENHA', null,null;
exec saTipoAtributo  'EMAIL_TOKEN_ASSINATURA', null,null;
exec saTipoAtributo  'EMAIL_CONTRATO_ASSINADO', null,null;
exec saTipoAtributo  'EMAIL_CHAVE_ACESSO', null,null;
exec saTipoAtributo  'EMAIL_CHAVE_ACESSO_PENDENTE', null,null;
exec saTipoAtributo  'EMAIL_CONTRATO_ASSINADO_PARTE', null,null;
exec saTipoAtributo  'CONSULTA_CONTRATO_MOSTRAR_PARTES', null,null;
exec saTipoAtributo  'CHAVE_ACESSO_GEOLOCALIZACAO_IPSTACK', null,null

exec saTipoAtributo  'CAMPOS_UPPERCASE', null,null;
exec saTipoAtributo  'ASSINAR_VIA_EMAIL', null,null;
exec saTipoAtributo  'ASSINAR_VIA_CERTIFICADO', null,null;

exec saTipoAtributo  'TERMO_DE_USO_SISTEMA', 'Termo de uso do sistema', 'PDF';
exec saTipoAtributo  'TERMO_DE_PRIVACIDADE_SISTEMA', 'Termo de privacidade do sistema', 'PDF'

exec saTipoAtributo  'PRAZO_ENVIO_EMAILS_AUTOMATICO', 'Prazo para parar o envio automático de e-mail', 'INTEGER'

select * from sistema_tipo_atributo

/*

update   sistema_tipo_atributo set tipo_valor = 'TEXT_SIMPLE'
where id = 'BD263D6D-D1AA-410E-BE08-663FF1C6C14D'

*/

if ( exists ( Select name From sys.objects Where type = 'P' and name = 'saAtributo' ) ) 
begin
	drop  procedure saAtributo;
end
GO

create procedure saAtributo ( @tipo_atributo varchar(255), @valor_atributo varchar(max), @id_cliente uniqueidentifier = null )
as
begin
	  declare @id_tipo_atributo uniqueidentifier;

	 select  @id_tipo_atributo = id from sistema_tipo_atributo where tipo_atributo = @tipo_atributo

	if (  not exists ( select * from sistema_atributo 
	                   where id_tipo_atributo = @id_tipo_atributo and
	                        ( id_cliente = @id_cliente or ( id_cliente is null and @id_cliente is null) ) ) )  
	begin
		insert into sistema_atributo ( id, id_tipo_atributo, valor_atributo, id_cliente, data_alteracao ) values ( newid(), @id_tipo_atributo, @valor_atributo, @id_cliente, getdate() );
	end
	else
	begin
			update sistema_atributo set
			       valor_atributo = @valor_atributo
		    where id_tipo_atributo = @id_tipo_atributo and
	              ( id_cliente = @id_cliente or ( id_cliente is null and @id_cliente is null) )  
	end

end
GO



exec saAtributo  'EMAIL_SENDER_HOST', 'email-ssl.com.br' ;
exec saAtributo  'EMAIL_SENDER_PORT', '465';
exec saAtributo  'EMAIL_SENDER_USERNAME', 'murta@assina.net';
exec saAtributo  'EMAIL_SENDER_PASSWORD', 'Amurta@123';
exec saAtributo  'EMAIL_SENDER_AUTHENTICATE', 'true';
exec saAtributo  'EMAIL_SENDER_TLS_ENABLE' , 'false' ;
exec saAtributo  'EMAIL_SENDER_TLS_REQUIRED', 'false';
exec saAtributo  'EMAIL_SENDER_SSL_ENABLE', 'true';
exec saAtributo  'EMAIL_SENDER_PROTOCOL', 'smtp';

exec saAtributo  'CONSULTA_CONTRATO_MOSTRAR_PARTES', 'true';

exec saAtributo 'CHAVE_ACESSO_GEOLOCALIZACAO_IPSTACK', '831a25229740b2bdc1c79b652b9d1c4c'




exec saAtributo  'EMAIL_NOVO_USUARIO', '<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>


<div align="rigth" style="color:#696969;">
    <div>
        Ol�, <span th:text="${parte}"></span><br>
        <br><br>Este � o usu�rio criando no portal de assinaturas Assina.net.
        <br><br><br>
        <div align="center">
            <div style="width: 329px;background-color: mediumseagreen;color: white;height: 44px;border-radius: 10px;display:table">
                <table style="display: inline;vertical-align: middle;text-decoration: none;background-color: mediumseagreen;color: white;">
                    <tr>
                        <td>Usuario:</td>
                        <td  th:text="${usuario}"></td>
                    </tr>
                    <tr>
                        <td>Senha:</td>
                        <td th:text="${senha}">}</td>
                    </tr>
                </table>

            </div>
        </div>

        <br><br>Este � um servi�o de assinatura com prote��o legal e validade jur�dica que <span
            th:text="${empresa}"></span> utiliza para concretizar seus neg�cios sem o uso de papel impresso.

        <br><br>Atenciosamente

        <br><br> Equipe <strong>Assina</strong><span style="color:#008000;">.net</span>
        <br><br><br><br>
        <div style="width:100%; border-top: 1px solid mediumseagreen;"></div>
        <div align="center">
            <span style="font-size:24px;"><strong>Assina</strong><span style="color:#008000;">.net</span></span><br/>
            <span style="color:#696969;"><span style="font-size:11px;">Assina.net Gest&atilde;o e Assinatura de Documentos</span></span><br />
			<span style="color:#696969;"><span style="font-size:11px;"><span th:text="${dataEnvio}"></span></span></span></p>
        </div>
    </div>

</div>
</body>
</html>';


exec saAtributo  'EMAIL_TOKEN_ASSINATURA', '<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>


<div align="rigth" style="color:#696969;">
    <div>
        Ol�, <span th:text="${parte}"></span><br>
        <br><br>Essa � a chave de valida��o para que voc� consiga assinar a opera��o <span th:text="${assunto}"></span>,
        solicitada por <span th:text="${empresa}"></span>:


        <br><br><br>
        <div align="center">
            <div style="width: 329px;background-color: mediumseagreen;color: white;height: 44px;border-radius: 10px;display:table">
                <span style="display: table-cell;vertical-align: middle;text-decoration: none;color: white;"
                      th:text="${token}"></span>
            </div>
        </div>

        <br><br>Este � um servi�o de assinatura com prote��o legal e validade jur�dica que <span
            th:text="${empresa}"></span> utiliza para concretizar seus neg�cios sem o uso de papel impresso.

        <br><br>Atenciosamente

        <br><br> Equipe <strong>Assina</strong><span style="color:#008000;">.net</span>
        <br><br><br><br>
        <div style="width:100%; border-top: 1px solid mediumseagreen;"></div>
        <div align="center">
            <span style="font-size:24px;"><strong>Assina</strong><span style="color:#008000;">.net</span></span><br/>
            <span style="color:#696969;"><span style="font-size:11px;">Assina.net Gest&atilde;o e Assinatura de Documentos</span></span><br />
			<span style="color:#696969;"><span style="font-size:11px;"><span th:text="${dataEnvio}"></span></span></span></p>
        </div>
    </div>

</div>
</body>
</html>';

exec saAtributo  'EMAIL_CONTRATO_ASSINADO', '<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>


<div align="rigth" style="color:#696969;">
    <div>
        Ol�, <span th:text="${parte}"></span><br>
        <br><br>A opera��o <span th:text="${assunto}"></span>, solicitada por <span th:text="${empresa}"></span> acaba
        de ser assinada por todos os respons�veis e agora est� vigente no
        portal de assinaturas Assina.net.


        <br><br>Para acessar nossa plataforma, clique no link abaixo:
        <br><br><br>
        <div align="center">
            <div style="width: 329px;background-color: mediumseagreen;color: white;height: 44px;border-radius: 10px;display:table">			
                <a style="display: table-cell;vertical-align: middle;text-decoration: none;color: white;"
                  th:href="@{${acessoURL}}">Visualize suas opera��es aqui!</a></div>
        </div>
    </div>

    <br><br>Este � um servi�o de assinatura com prote��o legal e validade jur�dica que <span
        th:text="${empresa}"></span> utiliza para concretizar seus neg�cios sem o uso de papel impresso.

    <br><br>Atenciosamente

    <br><br> Equipe <strong>Assina</strong><span style="color:#008000;">.net</span>
    <br><br><br><br>
    <div style="width:100%; border-top: 1px solid mediumseagreen;"></div>
    <div align="center">
        <span style="font-size:24px;"><strong>Assina</strong><span style="color:#008000;">.net</span></span><br/>
        <span style="color:#696969;"><span style="font-size:11px;">Assina.net Gest&atilde;o e Assinatura de Documentos</span></span><br />
		<span style="color:#696969;"><span style="font-size:11px;"><span th:text="${dataEnvio}"></span></span></span></p>
    </div>
</div>

</div>
</body>
</html>';

exec saAtributo  'EMAIL_CHAVE_ACESSO', '<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>


<div align="rigth" style="color:#696969;">
    <div>
        Ol�, <span th:text="${parte}"></span><br>
        <br><br>A <span th:text="${empresa}"></span> solicita sua assinatura para valida��o dos documentos de <span
            th:text="${assunto}"></span> atrav�s do portal de assinatura Assina.net.

        <br><br>Clique no link abaixo para acessar a plataforma e realizar a assinatura.
        <br><br><br>
        <div align="center">
            <div style="width: 329px;background-color: mediumseagreen;color: white;height: 44px;border-radius: 10px;display:table">
                <a style="display: table-cell;vertical-align: middle;text-decoration: none;color: white;"
                   th:href="@{${acessoURL}}">Visualize e assine seus documentos aqui!</a></div>
        </div>
    </div>

    <br><br>Este � um servi�o de assinatura com prote��o legal e validade jur�dica que <span
        th:text="${empresa}"></span> utiliza para concretizar seus neg�cios sem o uso de papel impresso.

    <br><br>Atenciosamente

    <br><br> Equipe <strong>Assina</strong><span style="color:#008000;">.net</span>
    <br><br><br><br>
    <div style="width:100%; border-top: 1px solid mediumseagreen;"></div>
    <div align="center">
        <span style="font-size:24px;"><strong>Assina</strong><span style="color:#008000;">.net</span></span><br />
        <span style="color:#696969;"><span style="font-size:11px;">Assina.net Gest&atilde;o e Assinatura de Documentos</span></span><br />
		<span style="color:#696969;"><span style="font-size:11px;"><span th:text="${dataEnvio}"></span></span></span></p>
    </div>
</div>

</div>
</body>
</html>';




exec saAtributo  'EMAIL_CHAVE_ACESSO_PENDENTE', '<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>


<div align="rigth" style="color:#696969;">
    <div>
        Ol�, <span th:text="${parte}"></span><br>
        <br><br>Esse � um lembrete de que a assinatura da opera��o <span th:text="${assunto}"></span>, solicitada por  <span th:text="${empresa}"></span>, continua na plataforma Assina.net.


        <br><br>Clique no link abaixo para acessar o portal de assinaturas para torn�-lo v�lido agora mesmo!"
        <br><br><br>
        <div align="center">
            <div style="width: 329px;background-color: mediumseagreen;color: white;height: 44px;border-radius: 10px;display:table">
                <a style="display: table-cell;vertical-align: middle;text-decoration: none;color: white;"
                   th:href="@{${acessoURL}}">Visualize e assine seus documentos aqui!</a></div>
        </div>
    </div>

    <br><br>Este � um servi�o de assinatura com prote��o legal e validade jur�dica que <span
        th:text="${empresa}"></span> utiliza para concretizar seus neg�cios sem o uso de papel impresso.

    <br><br>Atenciosamente

    <br><br> Equipe <strong>Assina</strong><span style="color:#008000;">.net</span>
    <br><br><br><br>
    <div style="width:100%; border-top: 1px solid mediumseagreen;"></div>
    <div align="center">
        <span style="font-size:24px;"><strong>Assina</strong><span style="color:#008000;">.net</span></span><br />
        <span style="color:#696969;"><span style="font-size:11px;">Assina.net Gest&atilde;o e Assinatura de Documentos</span></span><br />
		<span style="color:#696969;"><span style="font-size:11px;"><span th:text="${dataEnvio}"></span></span></span></p>
    </div>
</div>

</div>
</body>
</html>';


exec saAtributo  'EMAIL_CONTRATO_ASSINADO_PARTE', '<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>


<div align="rigth" style="color:#696969;">
    <div>
        Ol�, <span th:text="${parte}"></span><br>
        <br><br>A opera��o <span th:text="${assunto}"></span>, solicitada por  <span th:text="${empresa}"></span> acaba de ser assinada por <span th:text="${parte}"></span> no
        portal de assinaturas Assina.net.


        <br><br>Para acessar nossa plataforma, clique no link abaixo:"
        <br><br><br>
        <div align="center">
            <div style="width: 329px;background-color: mediumseagreen;color: white;height: 44px;border-radius: 10px;display:table">
                <a style="display: table-cell;vertical-align: middle;text-decoration: none;color: white;"
                   href="@{${acessoURL}}">Visualize seus documentos aqui!</a></div>
        </div>
    </div>

    <br><br>Este � um servi�o de assinatura com prote��o legal e validade jur�dica que <span
        th:text="${empresa}"></span> utiliza para concretizar seus neg�cios sem o uso de papel impresso.

    <br><br>Atenciosamente

    <br><br> Equipe <strong>Assina</strong><span style="color:#008000;">.net</span>
    <br><br><br><br>
    <div style="width:100%; border-top: 1px solid mediumseagreen;"></div>
    <div align="center">
        <span style="font-size:24px;"><strong>Assina</strong><span style="color:#008000;">.net</span></span><br />
        <span style="color:#696969;"><span style="font-size:11px;">Assina.net Gest&atilde;o e Assinatura de Documentos</span></span><br />
		<span style="color:#696969;"><span style="font-size:11px;"><span th:text="${dataEnvio}"></span></span></span></p>
    </div>
</div>

</div>
</body>
</html>';



exec saAtributo  'EMAIL_ESQUECEU_SENHA', '<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>


<div align="rigth" style="color:#696969;">
    <div>
        Ol�, <span th:text="${nome}"></span><br>
        <br><br>Foi solicitado uma altera��o de senha para o seu usu�rio.

		<br><br>O link � v�lido por at� 30 minutos.
        <br><br><br>
        <div align="center">
            <div style="width: 329px;background-color: mediumseagreen;color: white;height: 44px;border-radius: 10px;display:table">
                <a style="display: table-cell;vertical-align: middle;text-decoration: none;color: white;"
                   th:href="@{${acessoURL}}">Clique aqui para alterar sua senha.</a></div>
        </div>

        <br><br>Este � um servi�o de assinatura com prote��o legal e validade jur�dica para concretizar seus neg�cios sem o uso de papel impresso.

        <br><br>Atenciosamente

        <br><br> Equipe <strong>Assina</strong><span style="color:#008000;">.net</span>
        <br><br><br><br>
        <div style="width:100%; border-top: 1px solid mediumseagreen;"></div>
        <div align="center">
            <span style="font-size:24px;"><strong>Assina</strong><span style="color:#008000;">.net</span></span><br/>
            <span style="color:#696969;"><span style="font-size:11px;">Assina.net Gest&atilde;o e Assinatura de Documentos</span></span><br />
			<span style="color:#696969;"><span style="font-size:11px;"><span th:text="${dataEnvio}"></span></span></span></p>
        </div>
    </div>

</div>
</body>
</html>';


select * from sistema_atributo
