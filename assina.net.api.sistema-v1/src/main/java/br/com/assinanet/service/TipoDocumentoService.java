package br.com.assinanet.service;

import br.com.assinanet.entity.*;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.entity.enums.TipoPessoaEnum;
import br.com.assinanet.repository.*;
import br.com.assinanet.request.TipoDocumentoRequest;
import br.com.assinanet.response.ComboListResponse;
import br.com.assinanet.response.ContratoParteResponse;
import br.com.assinanet.util.CommonsUtil;
import br.com.assinanet.util.Util;
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
public class TipoDocumentoService {


    private final PapelService papelService;
    private final ClienteService clienteService;
    private final PessoaService pessoaService;
    private final SegmentoService segmentoService;

    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final TipoDocumentoPapelRepository tipoDocumentoPapelRepository;
    private final TipoDocumentoTipoClienteRepository tipoDocumentoTipoClienteRepository;
    private final TipoDocumentoPosicaoRepository tipoDocumentoPosicaoRepository;
    private final TipoDocumentoParteRepository tipoDocumentoParteRepository;
    private final TipoTelefoneRepository tipoTelefoneRepository;
    private final TipoDocumentoPartePapelRepository tipoDocumentoPartePapelRepository;


    public TipoDocumentoService(PapelService papelService, ClienteService clienteService,
                                PessoaService pessoaService, SegmentoService segmentoService, TipoDocumentoRepository tipoDocumentoRepository,
                                TipoDocumentoPapelRepository tipoDocumentoPapelRepository,
                                TipoDocumentoTipoClienteRepository tipoDocumentoTipoClienteRepository,
                                TipoDocumentoPosicaoRepository tipoDocumentoPosicaoRepository,
                                TipoDocumentoParteRepository tipoDocumentoParteRepository,
                                TipoTelefoneRepository tipoTelefoneRepository,
                                TipoDocumentoPartePapelRepository tipoDocumentoPartePapelRepository) {
        this.papelService = papelService;
        this.clienteService = clienteService;
        this.pessoaService = pessoaService;
        this.segmentoService = segmentoService;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.tipoDocumentoPapelRepository = tipoDocumentoPapelRepository;
        this.tipoDocumentoTipoClienteRepository = tipoDocumentoTipoClienteRepository;
        this.tipoDocumentoPosicaoRepository = tipoDocumentoPosicaoRepository;
        this.tipoDocumentoParteRepository = tipoDocumentoParteRepository;
        this.tipoTelefoneRepository = tipoTelefoneRepository;
        this.tipoDocumentoPartePapelRepository = tipoDocumentoPartePapelRepository;
    }

    public Page<TipoDocumento> findAll(TipoDocumento filtro, Pageable pageable) {
        return tipoDocumentoRepository.findAllManutencao(filtro.getCliente(), filtro.getCliente().getSegmento(), filtro.getNome(), filtro.getAssina(), pageable);

    }

    public TipoDocumentoRequest save(TipoDocumentoRequest tipoDocumentoRequest) {

        TipoDocumento tipoDocumento = tipoDocumentoRequest.getTipoDocumento();

        tipoDocumento = save(tipoDocumento);

        Cliente cliente = clienteService.findById(tipoDocumentoRequest.getCliente().getId());
        Cliente clienteSistema = clienteService.retornaClienteSistema();
        TipoTelefone tipoTelefoneCelular = tipoTelefoneRepository.findByIdentificacaoAndCliente("CELULAR", clienteSistema);

        List<TipoDocumentoParte> itensExcluir;

        if (CommonsUtil.semValor(tipoDocumentoRequest.getPartes())) {
            itensExcluir = tipoDocumentoParteRepository.desativaTipoDocumentoParteExcluidas(tipoDocumento);
        } else {
            List<UUID> listTipoDocumentoParteAtivos = new ArrayList<>(0);
            for (ContratoParteResponse parte : tipoDocumentoRequest.getPartes()) {
                if (parte.getId() != null) {
                    listTipoDocumentoParteAtivos.add(parte.getId());
                    for (ContratoParteResponse contato : parte.getContatos()) {
                        listTipoDocumentoParteAtivos.add(contato.getId());
                    }
                }
            }
            itensExcluir = tipoDocumentoParteRepository.desativaTipoDocumentoParteExcluidas(tipoDocumento, listTipoDocumentoParteAtivos);
        }
        if (!CommonsUtil.semValor(itensExcluir))
            tipoDocumentoParteRepository.deleteAll(itensExcluir);

        if (!CommonsUtil.semValor(tipoDocumentoRequest.getPartes())) {
            List<TipoDocumentoParte> tiposDocumentoParte = new ArrayList<>(0);

            for (ContratoParteResponse parte : tipoDocumentoRequest.getPartes()) {
                TipoDocumentoParte tipoDocumentoParte = criaTipoDocumentoParte(tipoDocumento, cliente, tipoTelefoneCelular, tiposDocumentoParte, parte, null);

                if (!CommonsUtil.semValor(parte.getContatos()))
                    for (ContratoParteResponse contato : parte.getContatos()) {
                        criaTipoDocumentoParte(tipoDocumento, cliente, tipoTelefoneCelular, tiposDocumentoParte, contato, tipoDocumentoParte.getPessoa());
                    }
            }

            tipoDocumentoParteRepository.saveAll(tiposDocumentoParte);
        }

        return tipoDocumentoRequest;

    }

    private TipoDocumentoParte criaTipoDocumentoParte(TipoDocumento tipoDocumento, Cliente cliente, TipoTelefone tipoTelefoneCelular,
                                                      List<TipoDocumentoParte> tiposDocumentoParte, ContratoParteResponse parte, Pessoa pessoaPJ) {
        TipoDocumentoParte tipoDocumentoParte = null;

        if (!CommonsUtil.semValor(parte.getId())) {
            tipoDocumentoParte = tipoDocumentoParteRepository.findById(parte.getId()).orElse(null);
        } else {
            tipoDocumentoParte = new TipoDocumentoParte();
            tipoDocumentoParte.setTipoDocumento(tipoDocumento);
            tipoDocumentoParte.setCliente(cliente);
            tipoDocumentoParte.setPessoaJuridica(pessoaPJ);
        }

        ContratoParte contratoParte = new ContratoParte(parte);
        Pessoa pessoa = pessoaService.findByCpfCnpjAndCliente(contratoParte, cliente);
        if (pessoa == null) {
            pessoa = new Pessoa();
            pessoa.setCpfCnpj(parte.getCpfCnpj());
            pessoa.setTipoPessoa(parte.getTipoPessoa());
            pessoa.setCliente(cliente);
        }
        pessoa.setNomeRazaoSocial(parte.getNomeRazaoSocial());
        pessoa.setEmail(parte.getEmail());

        pessoaService.salvaTelefoneCelular(pessoa, tipoTelefoneCelular, contratoParte);
        if (!CommonsUtil.semValor(tipoDocumentoParte.getId())) {
            if (CommonsUtil.semValor(parte.getPapel())) {
                tipoDocumentoPartePapelRepository.desativaTipoDocumentoPapelExcluidas(tipoDocumentoParte);
            } else {
                List<UUID> tipoDocumentoPapelAtivos = parte.getPapel().stream().filter(x -> !CommonsUtil.semValor(x.getId())).map(ContratoPartePapel::getId).collect(Collectors.toList());
                tipoDocumentoPartePapelRepository.desativaTipoDocumentoPapelExcluidas(tipoDocumentoParte, tipoDocumentoPapelAtivos);
            }
        }

        for (ContratoPartePapel contratoPartePapel : contratoParte.getPapel()) {
            if (contratoPartePapel.getId() == null) {
                TipoDocumentoPartePapel tipoDocumentoPartePapel = new TipoDocumentoPartePapel();
                tipoDocumentoPartePapel.setTipoDocumentoParte(tipoDocumentoParte);
                tipoDocumentoPartePapel.setPapel(contratoPartePapel.getPapel());
                tipoDocumentoParte.getPapel().add(tipoDocumentoPartePapel);
            }
        }
//                if (pessoa.getId() == null) {
//                    pessoa = pessoaService.save(pessoa);
//                }

        tipoDocumentoParte.setPessoa(pessoa);

        tiposDocumentoParte.add(tipoDocumentoParte);

        return tipoDocumentoParte;
    }

    public TipoDocumento save(TipoDocumento tipoDocumento) {
        if (!CommonsUtil.semValor(tipoDocumento.getId())) {

            List<UUID> tipoClientesAtivos = tipoDocumento.getTipoDocumentoTipoClientes().stream().filter(x -> !CommonsUtil.semValor(x.getId())).map(TipoDocumentoTipoCliente::getId).collect(Collectors.toList());

            List<UUID> papeisAtivos = tipoDocumento.getPapeis().stream().filter(x -> !CommonsUtil.semValor(x.getId())).map(TipoDocumentoPapel::getId).collect(Collectors.toList());

            List<UUID> posicoesAtivas = tipoDocumento.getPosicoesAssinatura().stream().filter(x -> !CommonsUtil.semValor(x.getId())).map(TipoDocumentoPosicao::getId).collect(Collectors.toList());

            if (CommonsUtil.semValor(tipoClientesAtivos)) {
                tipoDocumentoTipoClienteRepository.desativaTipoDocumentoExcluidas(tipoDocumento);
            } else {
                tipoDocumentoTipoClienteRepository.desativaTipoDocumentoExcluidas(tipoDocumento, tipoClientesAtivos);
            }

            if (CommonsUtil.semValor(papeisAtivos)) {
                tipoDocumentoPapelRepository.desativaTipoDocumentoPapelExcluidas(tipoDocumento);
            } else {
                tipoDocumentoPapelRepository.desativaTipoDocumentoPapelExcluidas(tipoDocumento, papeisAtivos);
            }

            if (CommonsUtil.semValor(posicoesAtivas)) {
                tipoDocumentoPosicaoRepository.desativaTipoDocumentoPosicoesExcluidas(tipoDocumento);
            } else {
                tipoDocumentoPosicaoRepository.desativaTipoDocumentoPosicoesExcluidas(tipoDocumento, posicoesAtivas);
            }

        }


        Util.verifica(tipoDocumento.getTipoDocumentoTipoClientes()).ifPresent(tipoClientes ->
        {
            for (TipoDocumentoTipoCliente tipoCliente : tipoClientes) {
                tipoCliente.setTipoDocumento(tipoDocumento);
                if (tipoCliente.getSegmento().getId() != null) {
                    Segmento segmento = segmentoService.findById(tipoCliente.getSegmento().getId());
                    tipoCliente.setSegmento(segmento);
                }
            }
        });

        Util.verifica(tipoDocumento.getPapeis()).ifPresent(papeis ->
        {
            for (TipoDocumentoPapel papel : papeis) {
                papel.setTipoDocumento(tipoDocumento);
                if (papel.getPapel().getCliente() == null) {
                    papel.getPapel().setCliente(tipoDocumento.getCliente());

                    //cria o papel com o mesmo tipo de cliente do documento
                    if (!CommonsUtil.semValor(tipoDocumento.getTipoDocumentoTipoClientes())) {
                        papel.getPapel().setPapelTipoClientes(new ArrayList<>(0));
                        for (TipoDocumentoTipoCliente t : tipoDocumento.getTipoDocumentoTipoClientes()) {
                            PapelTipoCliente papelTipoCliente = new PapelTipoCliente();
                            papelTipoCliente.setPapel(papel.getPapel());
                            papelTipoCliente.setSegmento(t.getSegmento());
                            papel.getPapel().getPapelTipoClientes().add(papelTipoCliente);
                        }
                    }
                }

                if (CommonsUtil.semValor(papel.getPapel().getAssina())) {
                    papel.getPapel().setAssina(tipoDocumento.getAssina());
                }

                if (!CommonsUtil.semValor(papel.getPapel().getId())) {
                    papel.setPapel(papelService.findById(papel.getPapel().getId()));
                } else {
                    papel.getPapel().setStatus(StatusEnum.ATIVO);
                }

            }
        });

        Util.verifica(tipoDocumento.getPosicoesAssinatura()).ifPresent(posicoes ->
        {
            for (TipoDocumentoPosicao posicao : posicoes) {
                posicao.setTipoDocumento(tipoDocumento);
            }
        });

        return tipoDocumentoRepository.save(tipoDocumento);
    }

    public TipoDocumento findById(UUID id) {
        return tipoDocumentoRepository.getOne(id);
    }

    public List<TipoDocumento> findByIdIn(List<UUID> IdList) {
        return tipoDocumentoRepository.findByIdIn(IdList);
    }


    public List<ContratoParteResponse> findPartes(Cliente cliente, TipoDocumento tipoDocumento) {
        List<ContratoParteResponse> parte = new ArrayList<ContratoParteResponse>(0);
        List<TipoDocumentoParte> listTipoDocumentoParte = tipoDocumentoRepository.findPartes(cliente, tipoDocumento);
        List<TipoDocumentoParte> listTipoDocumentoPartePJ = listTipoDocumentoParte.stream().filter(t -> t.getPessoa().getTipoPessoa() == TipoPessoaEnum.JURIDICA).collect(Collectors.toList());

        for (TipoDocumentoParte documentoParte : listTipoDocumentoPartePJ) {
            ContratoParteResponse contratoParteResponse = new ContratoParteResponse(documentoParte, documentoParte.getPessoa());
            contratoParteResponse.setContatos(new ArrayList<>(0));
            for (TipoDocumentoParte TipoDocumentoPartePF : listTipoDocumentoParte.stream().filter(t -> t.getPessoaJuridica() == documentoParte.getPessoa()).collect(Collectors.toList())) {
                if (TipoDocumentoPartePF != null) {
                    contratoParteResponse.getContatos().add(new ContratoParteResponse(TipoDocumentoPartePF, TipoDocumentoPartePF.getPessoa()));
                    listTipoDocumentoParte.remove(TipoDocumentoPartePF);
                }
            }
            parte.add(contratoParteResponse);
        }

        List<TipoDocumentoParte> listTipoDocumentoPartePF = listTipoDocumentoParte.stream().filter(t -> t.getPessoa().getTipoPessoa() == TipoPessoaEnum.FISICA).collect(Collectors.toList());
        listTipoDocumentoPartePF.forEach(tipoDocumentoParte -> {
            parte.add(new br.com.assinanet.response.ContratoParteResponse(tipoDocumentoParte, tipoDocumentoParte.getPessoa()));
        });
        return parte;
    }


    public TipoDocumento findByIdentificacaoAndCliente(String identificacao, Cliente cliente) {
        return findByIdentificacaoAndCliente(identificacao, cliente, "ATIVO");
    }

    public TipoDocumento findByIdentificacaoAndCliente(String identificacao, Cliente cliente, String status) {

        Cliente clienteSistema = clienteService.retornaClienteSistema();

        TipoDocumento retorno = tipoDocumentoRepository.findByIdentificacaoAndCliente(identificacao, cliente, null, status);

        if (clienteSistema != cliente && retorno == null) {
            retorno = tipoDocumentoRepository.findByIdentificacaoAndCliente(identificacao, clienteSistema, cliente.getSegmento(), status);
        }

        return retorno;


    }

    public void delete(UUID id) {
        tipoDocumentoRepository.deleteById(id);
    }

    public List<ComboListResponse> getListCombo(Cliente cliente) {

        List<ComboListResponse> result = new ArrayList<>(0);

        Cliente clienteSistema = clienteService.retornaClienteSistema();

        List<TipoDocumento> tiposDocumento = tipoDocumentoRepository.findByCliente(cliente, null);

        if (!CommonsUtil.mesmoValor(clienteSistema, cliente)) {
            tiposDocumento.addAll(tipoDocumentoRepository.findByCliente(clienteSistema, cliente.getSegmento()));
        }

        for (TipoDocumento tipoDocumento : tiposDocumento) {
            result.add(new ComboListResponse(tipoDocumento));
        }

        result.sort(Comparator.comparing(ComboListResponse::getLabel));

        return result;


    }
}
