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
public class SchedulerConfig {
    private final ContratoService contratoService;
    private final EmailService emailService;


    public SchedulerConfig(ContratoService contratoService, EmailService emailService) {
        this.contratoService = contratoService;
        this.emailService = emailService;
    }


    @Scheduled(cron = "0 */3 * * * *", zone = CommonsUtil.TIME_ZONE)
    //@Scheduled(cron = "0 */1 * * * *", zone = CommonsUtil.TIME_ZONE)
    @Transactional
    public void GerarContratosAssinadosPendentes() {
        contratoService.GerarContratosAssinadosPendentes();
    }

    @Scheduled(cron = "0 0 2 * * *", zone = CommonsUtil.TIME_ZONE)
    @Transactional
    public void cancelarContratosPendentes() {
        contratoService.cancelarContratosPendentes();
    }

    //Bonatte - 2023-11-05 - Desativado pois ja finalizou a migracao
    //@Scheduled( cron = "0 */1 * * * *", zone = CommonsUtil.TIME_ZONE)
    //@Transactional
    public void MigrarDocumentoParaStorage() {
        contratoService.MigrarDocumentoParaStorage();
    }

}
