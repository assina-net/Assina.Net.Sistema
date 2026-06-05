package br.com.assinanet.service;

import br.com.assinanet.entity.EmailFila;
import br.com.assinanet.repository.EmailFilaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Service
public class EmailFilaService {

    private final EmailFilaRepository repository;

    public EmailFilaService(EmailFilaRepository repository) {
        this.repository = repository;

    }

    public EmailFila save(EmailFila emailFila) {
        return repository.save(emailFila);
    }

    public EmailFila findById(UUID id) {
        return repository.getOne(id);
    }

    public List<EmailFila> findByEnviadoOrderByDataUltimoEnvio(Boolean enviado) {
        return repository.findByEnviadoOrderByDataUltimoEnvio(false);
    }

}

