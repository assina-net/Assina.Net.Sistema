package br.com.assinanet.service;

import br.com.assinanet.entity.SistemaAtributo;
import br.com.assinanet.entity.Usuario;
import br.com.assinanet.entity.UsuarioTermo;
import br.com.assinanet.entity.enums.SistemaTipoAtributoCategoriaEnum;
import br.com.assinanet.entity.enums.SistemaTipoAtributoEnum;
import br.com.assinanet.repository.UsuarioTermoRepository;
import br.com.assinanet.request.TermoAceiteRequest;
import br.com.assinanet.storage.Storage;
import br.com.assinanet.util.DataUtil;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;
import rufus.lzstring4java.LZString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
@Service
public class UsuarioTermoService {

    private final UsuarioTermoRepository usuarioTermoRepository;
    private final ContratoDocumentoService contratoDocumentoService;

    public UsuarioTermoService(UsuarioTermoRepository usuarioTermoRepository,
                               ContratoDocumentoService contratoDocumentoService) {
        this.usuarioTermoRepository = usuarioTermoRepository;
        this.contratoDocumentoService = contratoDocumentoService;
    }



    public Boolean termosPendentes(Usuario usuario) {

        List<SistemaTipoAtributoEnum> termos = SistemaTipoAtributoEnum.getSistemaAtributoPorCategoria(SistemaTipoAtributoCategoriaEnum.TERMOS);

        return usuarioTermoRepository.termosPendentes(usuario, termos) > 0;

    }

    public List<SistemaAtributo> termosPendentesAssinatura(Usuario usuario) {

        List<SistemaTipoAtributoEnum> termos = SistemaTipoAtributoEnum.getSistemaAtributoPorCategoria(SistemaTipoAtributoCategoriaEnum.TERMOS);

        return usuarioTermoRepository.termosPendentesAssinatura(usuario, termos);

    }

    public String carregaDocumentoTermo(SistemaAtributo termo) {
        String valor = termo.getValorAtributo();
        if (!isCaminhoStorage(valor)) {
            return valor;
        }

        Storage storage = contratoDocumentoService.retornaStorageFactory(termo.getCliente());
        if (storage == null) {
            throw new IllegalStateException("Storage nao configurado para carregar o termo: " + valor);
        }

        System.out.println("[TERMO_STORAGE_DOWNLOAD] Tipo: "
                + termo.getTipoAtributo().getTipoAtributo()
                + " | Caminho: " + valor);
        byte[] documento = storage.downloadFile(valor);
        return LZString.compressToUTF16(Base64.encodeBase64String(documento));
    }

    private boolean isCaminhoStorage(String valor) {
        if (valor == null) {
            return false;
        }
        String caminho = valor.trim().replace('\\', '/').toLowerCase();
        return caminho.endsWith(".pdf")
                && (caminho.startsWith("assina.net/")
                || caminho.startsWith("/assina.net/"));
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

