package br.com.assinanet.service;

import br.com.assinanet.entity.Plano;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.repository.PlanoRepository;
import br.com.assinanet.response.ComboListResponse;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class PlanoService {

    private final PlanoRepository planoRepository;

    public PlanoService(PlanoRepository planoRepository){
        this.planoRepository = planoRepository;
    }

    public Page<Plano> findAll(Plano filter, Pageable pageable){
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase();
        Example<Plano> example = Example.of(filter, matcher);
        return planoRepository.findAll(example, pageable);
    }

    public Plano save(Plano plano) {
        return planoRepository.save(plano);
    }

    public Plano findById(UUID id) {
        return planoRepository.getOne(id);
    }

    public List<ComboListResponse> getListCombo() {

        List<ComboListResponse> result = new ArrayList<>(0);

        Plano filtro = new Plano();
        filtro.setStatus(StatusEnum.ATIVO);
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase();
        Example<Plano> example = Example.of(filtro, matcher);
        List<Plano> planos =  planoRepository.findAll(example);

        for (Plano plano : planos) {
            result.add(new ComboListResponse(plano.getId(), plano.getNome(), plano.getNome()));
        }

        result.sort(Comparator.comparing(ComboListResponse::getLabel));

        return result;


    }

}
