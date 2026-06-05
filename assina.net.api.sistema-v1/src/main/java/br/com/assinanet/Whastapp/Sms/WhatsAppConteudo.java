package br.com.assinanet.Whastapp.Sms;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WhatsAppConteudo {

     String api;
     String token;
     String remetente;
     String conteudo;
     String destinatarios;
     String data;

     public String getDestinatarios() {
          return  this.destinatarios;
     }

     public void setDestinatarios(String destinatarios) {
          if ( destinatarios.length() <= 11)
               destinatarios = "55" + destinatarios;
          this.destinatarios = destinatarios;
     }
}
