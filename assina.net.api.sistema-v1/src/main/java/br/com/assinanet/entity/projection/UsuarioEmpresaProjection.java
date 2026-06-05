package br.com.assinanet.entity.projection;

import br.com.assinanet.entity.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.util.UUID;

/**
 * @author Samuel Oliveira - samuel@swconsultoria.com.br
 */
@Projection(name = "usuarioProjection", types = {Usuario.class})
public interface UsuarioEmpresaProjection {

    UUID getId();

    @Value("#{target.nome}")
    String getNome();
}
