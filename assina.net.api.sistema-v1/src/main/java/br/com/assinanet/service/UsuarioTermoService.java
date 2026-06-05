package br.com.assinanet.service;

import br.com.assinanet.entity.SistemaAtributo;
import br.com.assinanet.entity.Usuario;
import br.com.assinanet.entity.UsuarioTermo;
import br.com.assinanet.entity.enums.SistemaTipoAtributoCategoriaEnum;
import br.com.assinanet.entity.enums.SistemaTipoAtributoEnum;
import br.com.assinanet.repository.UsuarioTermoRepository;
import br.com.assinanet.request.TermoAceiteRequest;
import br.com.assinanet.util.DataUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
@Service
public class UsuarioTermoService {

    private final UsuarioTermoRepository usuarioTermoRepository;

    public UsuarioTermoService(UsuarioTermoRepository usuarioTermoRepository) {
        this.usuarioTermoRepository = usuarioTermoRepository;
    }



    public Boolean termosPendentes(Usuario usuario) {

        List<SistemaTipoAtributoEnum> termos = SistemaTipoAtributoEnum.getSistemaAtributoPorCategoria(SistemaTipoAtributoCategoriaEnum.TERMOS);

        return usuarioTermoRepository.termosPendentes(usuario, termos) > 0;

    }

    public List<SistemaAtributo> termosPendentesAssinatura(Usuario usuario) {

        List<SistemaTipoAtributoEnum> termos = SistemaTipoAtributoEnum.getSistemaAtributoPorCategoria(SistemaTipoAtributoCategoriaEnum.TERMOS);

        return usuarioTermoRepository.termosPendentesAssinatura(usuario, termos);

    }

    public boolean termosAceite(TermoAceiteRequest termoAceiteRequest) {
        boolean salvou = false;

        List<UsuarioTermo> termosAceito = new ArrayList<>(0);

        termoAceiteRequest.getTermos().forEach(termo -> {
            UsuarioTermo usuarioTermo = new UsuarioTermo();
            SistemaAtributo sistemaAtributo = new SistemaAtributo();
            sistemaAtributo.setId(termo.getId());
            usuarioTermo.setSistemaAtributo(sistemaAtributo);
            usuarioTermo.setUsuario(termoAceiteRequest.getUsuario());
            usuarioTermo.setDataAceite(DataUtil.getCarimboTempo().getDataCarimboTempo());
            usuarioTermo.setIp( termoAceiteRequest.getIp());
            termosAceito.add(usuarioTermo);
        });


        try {
            usuarioTermoRepository.saveAll(termosAceito);
            salvou = true;
        } catch (Exception e) {
        }

        return salvou;

    }




}

