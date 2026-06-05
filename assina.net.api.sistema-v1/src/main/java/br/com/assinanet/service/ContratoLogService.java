package br.com.assinanet.service;

import br.com.assinanet.entity.Contrato;
import br.com.assinanet.entity.ContratoDocumento;
import br.com.assinanet.entity.ContratoLog;
import br.com.assinanet.repository.ContratoLogRepository;
import br.com.assinanet.util.CommonsUtil;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class ContratoLogService {

    static final String TAG_NAO_EXISTENTE = "dfasdfmasdofsadfkmsdafeowi$%$#%!";

    private final ContratoLogRepository contratoLogRepository;

    public ContratoLogService(ContratoLogRepository contratoLogRepository) {
        this.contratoLogRepository = contratoLogRepository;
    }

    public void SalvaLogsRemotos(List<ContratoLog> logs) {

        logs.forEach(log -> {
            if (CommonsUtil.semValor(log.getLogSistema())) {
                log.setLogSistema(false);
            }
        });

        contratoLogRepository.saveAll(logs);
    }

    public List<ContratoLog> getLog(Contrato contrato, ContratoDocumento documento) {
        List<ContratoLog> result;

        if (documento != null && documento.getId() != null) {
            result = this.contratoLogRepository.findByContratoAndContratoDocumento(contrato, documento);
        } else {
            result = this.contratoLogRepository.findByContratoAndLogSistema(contrato, false);
        }
        //reordena a lista por data de assinatura
        Collections.sort(result, new Comparator<ContratoLog>() {
            @Override
            public int compare(ContratoLog o1, ContratoLog o2) {
                return o1.getDataLog().compareTo(o2.getDataLog());
            }
        });

        return result;
    }

    public void salva(ContratoLog contratoLog) {

        if ((contratoLog.getContrato() == null || contratoLog.getContrato().getId() == null) && contratoLog.getLogSistema()) {
            return;
        }
        contratoLogRepository.save(contratoLog);
    }

}
