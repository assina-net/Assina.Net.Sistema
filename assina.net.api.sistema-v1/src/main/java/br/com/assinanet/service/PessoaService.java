package br.com.assinanet.service;

import br.com.assinanet.entity.*;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.models.CarimboTempoModel;
import br.com.assinanet.repository.*;
import br.com.assinanet.request.ContratoParteAssinaturaRequest;
import br.com.assinanet.util.CommonsUtil;
import br.com.assinanet.util.DataUtil;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Service
public class PessoaService {

    private final PessoaRepository repository;
    private final PessoaEnderecoRepository pessoaEnderecoRepository;
    private final PaisRepository paisRepository;
    private final TipoTelefoneRepository tipoTelefoneRepository;
    private final TipoEnderecoRepository tipoEnderecoRepository;


    private final GeoLocalizationService geoLocalizationService;
    private final ClienteService clienteService;

    public PessoaService(PessoaRepository repository, PessoaEnderecoRepository pessoaEnderecoRepository,
                         PaisRepository paisRepository, TipoTelefoneRepository tipoTelefoneRepository,
                         TipoEnderecoRepository tipoEnderecoRepository, GeoLocalizationService geoLocalizationService, ClienteService clienteService) {
        this.repository = repository;
        this.pessoaEnderecoRepository = pessoaEnderecoRepository;
        this.paisRepository = paisRepository;
        this.tipoTelefoneRepository = tipoTelefoneRepository;
        this.tipoEnderecoRepository = tipoEnderecoRepository;
        this.geoLocalizationService = geoLocalizationService;
        this.clienteService = clienteService;
    }

    public Pessoa save(Pessoa pessoa) {
        return repository.save(pessoa);
    }

    public Pessoa findById(UUID id) {
        return repository.getOne(id);
    }

    public Pessoa findByCpfCnpj(String cpfCnpj) {
        return repository.findByCpfCnpj(cpfCnpj);
    }

    public Pessoa findByCpfCnpjAndCliente(ContratoParte contratoParte, UUID clienteID) {
        Cliente cliente = new Cliente();
        cliente.setId(clienteID);
        return findByCpfCnpjAndCliente(contratoParte,  cliente);
    }

    public Pessoa findByCpfCnpjAndCliente(ContratoParte contratoParte,  Cliente cliente) {
        return repository.findByCpfCnpjAndTipoPessoaAndCliente(contratoParte.getCpfCnpj(), contratoParte.getTipoPessoa(), cliente);
    }

    public void CriaPessoaPeloContratoParte(List<ContratoParte> partes, Cliente cliente, Pessoa pessoaJuridica, Map<String, Pessoa> pessoas) {

        //busacando dados salvos
        //List<ContratoParte> partes = getPartes(contrato);
        if (pessoas == null) {
            pessoas = new HashMap<>(0);
        }

        Pessoa pessoa = null;

        Cliente clienteSistema = clienteService.retornaClienteSistema();
        TipoTelefone tipoTelefoneCelular = tipoTelefoneRepository.findByIdentificacaoAndCliente("CELULAR", clienteSistema);

        for (ContratoParte parte : partes) {
            pessoa = pessoas.get(parte.getCpfCnpj());
            if (pessoa == null) {
                pessoa = findByCpfCnpjAndCliente(parte, cliente.getId());
                if (pessoa == null) {
                    pessoa = new Pessoa(parte, cliente);
                    pessoas.put(pessoa.getCpfCnpj(), pessoa);
                }
            }

            if (pessoaJuridica == null) {
                pessoa.setPapel(CriaPessoaPeloContratoPartePapel(pessoa, null, parte));
            }

            pessoa.setNomeRazaoSocial(parte.getNomeRazaoSocial());
            pessoa.setEmail(parte.getEmail());

            if (!CommonsUtil.semValor(parte.getCelular())) {
                salvaTelefoneCelular(pessoa, tipoTelefoneCelular, parte);
            }


            if (pessoaJuridica != null) {
                PessoaJuridicaPessoaFisica pessoaJuridicaPessoaFisica = null;
                for (PessoaJuridicaPessoaFisica p : pessoaJuridica.getPessoasFisica()) {
                    if (CommonsUtil.mesmoValor(p.getPessoaFisica().getCpfCnpj(), pessoa.getCpfCnpj())) {
                        pessoaJuridicaPessoaFisica = p;
                        break;
                    }
                }


                if (pessoaJuridicaPessoaFisica == null) {
                    pessoaJuridicaPessoaFisica = new PessoaJuridicaPessoaFisica();
                    pessoaJuridicaPessoaFisica.setPessoaFisica(pessoa);
                    pessoaJuridicaPessoaFisica.setPessoaJuridica(pessoaJuridica);
                    pessoaJuridica.getPessoasFisica().add(pessoaJuridicaPessoaFisica);
                }

                pessoaJuridicaPessoaFisica.setPapel(CriaPessoaPeloContratoPartePapel(null, pessoaJuridicaPessoaFisica, parte));

            }

            List<ContratoParte> contatos = parte.getContatos();

            if (!CommonsUtil.semValor(contatos)) {
                CriaPessoaPeloContratoParte(contatos, cliente, pessoa, pessoas);
            }

            pessoa = repository.save(pessoa);


        }


    }

    public void salvaTelefoneCelular(Pessoa pessoa, TipoTelefone tipoTelefoneCelular, ContratoParte parte) {
        if (CommonsUtil.semValor(pessoa.getPessoaTelefone())) {
            pessoa.setPessoaTelefone(new ArrayList<>(0));
        }

        PessoaTelefone pessoaTelefone = pessoa.getPessoaTelefone().stream().filter(t -> t.getTipoTelefone() == tipoTelefoneCelular).findFirst().orElse(new PessoaTelefone());
        pessoaTelefone.setNumero(parte.getCelular());
        pessoaTelefone.setStatus(StatusEnum.ATIVO);
        pessoaTelefone.setDataAtualizacao(DataUtil.getCarimboTempo().getDataCarimboTempo());
        if (CommonsUtil.semValor(pessoaTelefone.getDataCadastramento())) {
            pessoaTelefone.setDataCadastramento(pessoaTelefone.getDataAtualizacao());
        }

        if (pessoaTelefone.getTipoTelefone() == null) {
            pessoaTelefone.setTipoTelefone(tipoTelefoneCelular);
        }
        if (pessoaTelefone.getId() == null) {
            pessoaTelefone.setPessoa(pessoa);
            pessoa.getPessoaTelefone().add(pessoaTelefone);
        }
    }

    private List<PessoaFisicaPapel> CriaPessoaPeloContratoPartePapel(Pessoa pessoaFisica,
                                                                     PessoaJuridicaPessoaFisica pessoaJuridicaPessoaFisica, ContratoParte parte) {

        List<PessoaFisicaPapel> lstPapel = null;
        if (!CommonsUtil.semValor(pessoaFisica)) {
            lstPapel = pessoaFisica.getPapel();
        } else if (!CommonsUtil.semValor(pessoaJuridicaPessoaFisica)) {
            lstPapel = pessoaJuridicaPessoaFisica.getPapel();
        }

        if (!CommonsUtil.semValor(parte.getPapel())) {
            if (CommonsUtil.semValor(lstPapel)) {
                lstPapel = new ArrayList<>(0);
            }

            for (ContratoPartePapel papelParte : parte.getPapel()) {

                PessoaFisicaPapel pessoaFisicaPapel = null;
                for (PessoaFisicaPapel p : lstPapel) {
                    if (CommonsUtil.mesmoValor(p.getPapel().getId(), papelParte.getPapel().getId())) {
                        pessoaFisicaPapel = p;
                        break;
                    }
                }

                if (pessoaFisicaPapel == null) {
                    pessoaFisicaPapel = new PessoaFisicaPapel();
                    //pessoaFisicaPapel.setDuplicatas(parte.getDuplicatas());
                    pessoaFisicaPapel.setPapel(papelParte.getPapel());
                    pessoaFisicaPapel.setPessoaFisica(pessoaFisica);
                    pessoaFisicaPapel.setPessoaJuridicaPessoaFisica(pessoaJuridicaPessoaFisica);
                    lstPapel.add(pessoaFisicaPapel);

//                    if (pessoaFisica != null) {
//                        if (pessoaFisica.getPapel() == null) {
//                            pessoaFisica.setPapel(new ArrayList<>(0));
//                        }
//                        pessoaFisica.getPapel().add(pessoaFisicaPapel);
//                    }
//                    if (pessoaJuridicaPessoaFisica != null) {
//                        if (pessoaJuridicaPessoaFisica.getPapel() == null) {
//                            pessoaJuridicaPessoaFisica.setPapel(new ArrayList<>(0));
//                        }
//                        pessoaJuridicaPessoaFisica.getPapel().add(pessoaFisicaPapel);
//                    }
                }
            }
        }

        return lstPapel;
    }

    public PessoaEndereco PreencheEnderecoPessoa(ContratoParteAssinaturaRequest contratoParteAssinatura, Usuario usuario) {
        PessoaEndereco pessoaEndereco = null;

        GeoLocalizationService.GeoLocalzation geoLocalzation = geoLocalizationService.GeoLocalization(contratoParteAssinatura.getIpCliente());

        if (geoLocalzation == null) {
            List<PessoaEndereco> enderecos = pessoaEnderecoRepository.findByPessoa(usuario.getPessoa());
            enderecos = enderecos.stream().sorted(Comparator.comparing(PessoaEndereco::getDataAtualizacao).reversed()).collect(Collectors.toList());
            PessoaEndereco PessoaEnderecoUltimo = enderecos.stream().findFirst().orElse(null);
            return PessoaEnderecoUltimo;
        }

        pessoaEndereco = new PessoaEndereco();
        pessoaEndereco.setPessoa(usuario.getPessoa());
        pessoaEndereco.setPais(BuscaPais(geoLocalzation));
        pessoaEndereco.setMunicipio(geoLocalzation.getCity());
        pessoaEndereco.setEstado(geoLocalzation.getRegion_code());
        pessoaEndereco.setCep(geoLocalzation.getZip());
        CarimboTempoModel carimboTempoModel = DataUtil.getCarimboTempo();
        pessoaEndereco.setDataCadastramento(carimboTempoModel.getDataCarimboTempo());
        pessoaEndereco.setDataAtualizacao(carimboTempoModel.getDataCarimboTempo());
        pessoaEndereco.setTipoEndereco(tipoEnderecoRepository.findByIdentificacao("RESIDENCIAL"));

        pessoaEndereco = pessoaEnderecoRepository.save(pessoaEndereco);

        return pessoaEndereco;
    }

    private Pais BuscaPais(GeoLocalizationService.GeoLocalzation geoLocalzation) {
        Pais pais = paisRepository.findByCodigoPais(geoLocalzation.getCountry_code());
        if (pais == null) {

            pais = new Pais();
            pais.setNome(geoLocalzation.getCountry_name());
            pais.setCodigoPais(geoLocalzation.getCountry_code());
            pais.setContinenteCodigo(geoLocalzation.getContinent_code());
            pais.setContinente(geoLocalzation.getContinent_name());
            pais.setCodigoTelefonePais(CommonsUtil.integerValue(geoLocalzation.getLocation().getCalling_code()));
            pais.setGeonameId(new BigInteger(geoLocalzation.getLocation().getGeoname_id()));
            pais.setCapital(geoLocalzation.getLocation().getCapital());
            pais.setBandeira(geoLocalzation.getLocation().getCountry_flag());
            pais.setBandriaEmoji(geoLocalzation.getLocation().getCountry_flag_emoji());
            pais.setBandeiraEmojiUnicode(geoLocalzation.getLocation().getCountry_flag_emoji_unicode());

            pais.setIdimoas(new ArrayList<>(0));
            for (GeoLocalizationService.Languages languages : geoLocalzation.getLocation().getLanguages()) {
                pais.getIdimoas().add(new PaisIdiomas(null, pais, languages.getCode(), languages.getName()));
            }

            pais = paisRepository.save(pais);
        }


        return pais;
    }


}

