package br.com.assinanet.service;

import br.com.assinanet.entity.*;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.repository.PapelRepository;
import br.com.assinanet.repository.PapelTipoClienteRepository;
import br.com.assinanet.repository.TipoDocumentoPapelRepository;
import br.com.assinanet.repository.TipoDocumentoRepository;
import br.com.assinanet.request.NovoPapelTipoDocumentoRequest;
import br.com.assinanet.response.ComboListResponse;
import br.com.assinanet.util.CommonsUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
@Service
public class PapelService {


    private final PapelRepository papelRepository;
    private final PapelTipoClienteRepository papelTipoClienteRepository;
    private final ClienteService clienteService;

    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final TipoDocumentoPapelRepository tipoDocumentoPapelRepository;

    public PapelService(PapelRepository papelRepository, PapelTipoClienteRepository papelTipoClienteRepository, ClienteService clienteService, TipoDocumentoRepository tipoDocumentoRepository, TipoDocumentoPapelRepository tipoDocumentoPapelRepository) {
        this.papelRepository = papelRepository;
        this.papelTipoClienteRepository = papelTipoClienteRepository;
        this.clienteService = clienteService;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.tipoDocumentoPapelRepository = tipoDocumentoPapelRepository;
    }

    public Page<Papel> findAll(Papel filtro, Pageable pageable) {
        return papelRepository.findAllManutencao(filtro.getCliente(), filtro.getCliente().getSegmento(), filtro.getNome(), pageable);
    }

    public Papel save(Papel papel) {


        if (!CommonsUtil.semValor(papel.getId())) {
            List<UUID> tipoClientesAtivos = papel.getPapelTipoClientes().stream().filter(x -> !CommonsUtil.semValor(x.getId())).map(PapelTipoCliente::getId).collect(Collectors.toList());

            if (CommonsUtil.semValor(tipoClientesAtivos)) {
                papelTipoClienteRepository.desativaTipoDocumentoExcluidas(papel);
            } else {
                papelTipoClienteRepository.desativaTipoDocumentoExcluidas(papel, tipoClientesAtivos);
            }
        }

        List<PapelTipoCliente> tipoClientes = new ArrayList<>(0);
        tipoClientes.addAll(papel.getPapelTipoClientes());


        papel.setPapelTipoClientes(null);

        final Papel papelSalvo = papelRepository.save(papel);

        tipoClientes.forEach(tipoCliente -> {
            tipoCliente.setPapel(papelSalvo);
        });

        papelTipoClienteRepository.saveAll(tipoClientes);

        papelSalvo.setPapelTipoClientes(tipoClientes);
//
//        Util.verifica(papel.getPapelTipoClientes()).ifPresent(tipoClientes ->
//                tipoClientes.forEach(tipoCliente -> {
//                    tipoCliente.setPapel(papel);
//                }));

        return papelSalvo;
    }

    public Papel findById(UUID id) {
        Papel retorno = papelRepository.getOne(id);
        return retorno;
    }

    public Papel findByIdentificacaoAndCliente(String identificacao, Cliente cliente) {
        return findByIdentificacaoAndCliente(identificacao, cliente, StatusEnum.ATIVO);
    }

    public Papel findByIdentificacaoAndCliente(String identificacao, Cliente cliente, StatusEnum status) {

        Cliente clienteSistema = clienteService.retornaClienteSistema();

        Papel retorno = null;
        Segmento segmento = null;

        if (cliente != null) {
            retorno = papelRepository.findByIdentificacaoAndCliente(identificacao, cliente, null, status);
            segmento = cliente.getSegmento();
        }

        if (clienteSistema != cliente && retorno == null) {
            retorno = papelRepository.findByIdentificacaoAndCliente(identificacao, clienteSistema, segmento, status);
        }
        return retorno;
    }

    public List<ComboListResponse> getListCombo(Cliente cliente) {

        List<ComboListResponse> result = new ArrayList<>(0);

        Cliente clienteSistema = clienteService.retornaClienteSistema();

        List<Papel> papeis = papelRepository.findByCliente(cliente, null);
        if (!CommonsUtil.mesmoValor(clienteSistema, cliente)) {
            //busca o cliente para pegar o tipo
            cliente = clienteService.findById(cliente.getId());
            papeis.addAll(papelRepository.findByCliente(clienteSistema, cliente.getSegmento()));
        }

        for (Papel papel : papeis) {
            result.add(new ComboListResponse(papel.getId(), papel.getIdentificacao(), papel.getNome()));
        }

        result.sort(Comparator.comparing(ComboListResponse::getLabel));

        return result;

    }

    public Papel novoPapelTipoDocumento(NovoPapelTipoDocumentoRequest novoPapelTipoDocumentoRequest) {
        TipoDocumento tipoDocumento = this.tipoDocumentoRepository.findById(novoPapelTipoDocumentoRequest.getIdTipoDocumento()).orElse(null);
        Papel papelNovo = null;
        if (tipoDocumento != null) {
            papelNovo = save(novoPapelTipoDocumentoRequest.getPapel());
            TipoDocumentoPapel tipoDocumentoPapel = new TipoDocumentoPapel(null, tipoDocumento, papelNovo, false, true);
            this.tipoDocumentoPapelRepository.save(tipoDocumentoPapel);
        }
        return papelNovo;
    }
}
