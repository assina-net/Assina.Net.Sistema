package br.com.assinanet.service;

import br.com.assinanet.entity.Segmento;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.repository.SegmentoRepository;
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

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
@Service
public class SegmentoService {


    private final SegmentoRepository segmentoRepository;


    public SegmentoService(SegmentoRepository segmentoRepository) {
        this.segmentoRepository = segmentoRepository;

    }

    public Page<Segmento> findAll(Segmento filtro, Pageable pageable) {
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase();
        Example<Segmento> example = Example.of(filtro, matcher);
        return segmentoRepository.findAll(example, pageable);
    }

    public Segmento save(Segmento segmento) {
        return segmentoRepository.save(segmento);
    }

    public Segmento findById(UUID id) {
        Segmento retorno = segmentoRepository.getOne(id);
        return retorno;
    }

    public Segmento findByIdentificacao(String identificacao){
        Segmento retorno = segmentoRepository.findByIdentificacao(identificacao);
        return retorno;
    }

    public List<ComboListResponse> getListCombo() {

        List<ComboListResponse> result = new ArrayList<>(0);

        Segmento filtro = new Segmento();
        filtro.setStatus(StatusEnum.ATIVO);
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase();
        Example<Segmento> example = Example.of(filtro, matcher);
        List<Segmento> segmentos =  segmentoRepository.findAll(example);

        for (Segmento segmento : segmentos) {
            result.add(new ComboListResponse(segmento.getId(), segmento.getIdentificacao(), segmento.getNome()));
        }

        result.sort(Comparator.comparing(ComboListResponse::getLabel));

        return result;


    }

}
