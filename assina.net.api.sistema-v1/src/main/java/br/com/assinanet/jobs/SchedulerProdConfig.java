package br.com.assinanet.jobs;

import br.com.assinanet.service.ContratoService;
import br.com.assinanet.service.EmailService;
import br.com.assinanet.util.CommonsUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import jakarta.transaction.Transactional;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 * Data: 03/08/2019 - 11:56
 */


@EnableScheduling
@Configuration
@Profile("prod")
public class SchedulerProdConfig {

    private final EmailService emailService;

    public SchedulerProdConfig(ContratoService contratoService, EmailService emailService) {
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 */1 * * *", zone = CommonsUtil.TIME_ZONE)
    public void EnviarEmailAssinaturaPendente() {
        emailService.ReenviarEmailAssinaturaDisponivel();
        // Código que realiza a consulta de fluxo de vendas
    }


    @Scheduled(cron = "0 */10 * * * *", zone = CommonsUtil.TIME_ZONE)
    @Transactional
    public void EnviarEmailsPendentesComErro() {
        emailService.EnviarEmailsPendentesComErro();
    }


    @Scheduled(cron = "0 */1 * * * *", zone = CommonsUtil.TIME_ZONE)
    @Transactional
    public void EnviarEmailsProblemaChaveAcesso() {
        emailService.EnviaProblemaChaveAcessoEmail();
    }

}
