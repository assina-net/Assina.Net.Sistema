package br.com.assinanet.repository;

import br.com.assinanet.entity.Pais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Repository
public interface PaisRepository extends JpaRepository<Pais, UUID> {


    Pais findByCodigoPais(String codigoPais);


}
