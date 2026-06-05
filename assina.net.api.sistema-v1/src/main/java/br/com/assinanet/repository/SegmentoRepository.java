package br.com.assinanet.repository;

import br.com.assinanet.entity.Pais;
import br.com.assinanet.entity.Segmento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Repository
public interface SegmentoRepository extends JpaRepository<Segmento, UUID> {


    Segmento findByIdentificacao(String identificacao);

}
