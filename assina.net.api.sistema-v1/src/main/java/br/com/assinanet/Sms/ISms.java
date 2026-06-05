package br.com.assinanet.Sms;

import java.io.IOException;

public interface ISms {
     boolean enviaSMS(SmsConteudo smsConteudo) throws IOException, InterruptedException;
}