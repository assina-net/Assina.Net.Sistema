package br.com.assinanet.controller;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.Pessoa;
import br.com.assinanet.entity.Plano;
import br.com.assinanet.entity.Segmento;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.entity.enums.TipoPessoaEnum;
import br.com.assinanet.request.IdRequest;
import br.com.assinanet.response.ClienteResponse;
import br.com.assinanet.response.ComboListResponse;
import br.com.assinanet.response.Response;
import br.com.assinanet.response.base.ResponseComCombos;
import br.com.assinanet.service.*;
import br.com.assinanet.util.Util;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
@RestController
@RequestMapping("/api/v1/cliente")
@CrossOrigin(origins = "*")
public class ClienteController {

   // private final UsuarioService usuarioService;
    //private final UsuarioClienteService usuarioClienteService;
    private final ClienteService clienteService;
    private final PessoaEnderecoService pessoaEnderecoService;
    private final PessoaTelefoneService pessoaTelefoneService;
    private final SegmentoService segmentoService;

    private final PlanoService planoService;

    public ClienteController(//UsuarioService usuarioService,
                             //UsuarioClienteService usuarioClienteService,
                             ClienteService clienteService, //PasswordEncoder passwordEncoder,
                             PessoaEnderecoService pessoaEnderecoService,
                             PessoaTelefoneService pessoaTelefoneService,
                             SegmentoService segmentoService,
                             PlanoService planoService) {
       // this.usuarioService = usuarioService;
        //this.usuarioClienteService = usuarioClienteService;
        this.clienteService = clienteService;
        this.pessoaEnderecoService = pessoaEnderecoService;
        this.pessoaTelefoneService = pessoaTelefoneService;
        this.segmentoService = segmentoService;
        this.planoService = planoService;
        System.out.println("Construtor ClienteController - cliente: " + clienteService);
    }

    @PostMapping
    public ResponseEntity<Response<Cliente>> create(@RequestBody Cliente cliente) {
        Response<Cliente> response = new Response<>();

        try {
            cliente = clienteService.salva(cliente);
            response.setData(cliente);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<Response<Cliente>> update(@RequestBody @Valid Cliente cliente) {
        Response<Cliente> response = new Response<>();

        try {
            cliente = clienteService.salva(cliente);
            response.setData(cliente);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "findById")
    public ResponseEntity<Response<ClienteResponse>> findById(@RequestBody IdRequest idRequest) {
        Response<ClienteResponse> response = new Response<>();
        Cliente cliente = clienteService.findById(idRequest.getId());
        if (!Util.verifica(cliente).isPresent()) {
            response.getErrors().add("Registro não encontrado, id:" + idRequest.getId());
            //return ResponseEntity.badRequest().body(response);
        }
        ClienteResponse ClienteResponse = new ClienteResponse();

        if (cliente.getSegmento() == null)
            cliente.setSegmento(new Segmento());

        if(cliente.getPlano() == null)
            cliente.setPlano(new Plano());

        ClienteResponse.setCliente(cliente);
        BuscaCombos(ClienteResponse.getListCombos(), idRequest, false);
        response.setData(ClienteResponse);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "new")
    public ResponseEntity<Response<ClienteResponse>> carregaCombos(@RequestBody IdRequest idRequest) {
        Response<ClienteResponse> response = new Response<>();

        ClienteResponse clienteResponse = new ClienteResponse();

        clienteResponse.setCliente(new Cliente());
        clienteResponse.getCliente().setStatus(StatusEnum.ATIVO);
        clienteResponse.getCliente().setNaoMostrar(false);
        clienteResponse.getCliente().setSegmento(new Segmento());
        clienteResponse.getCliente().setPlano(new Plano());

        Cliente clienteSistema = clienteService.retornaClienteSistema();

        clienteResponse.getCliente().setPessoa(new Pessoa());
        clienteResponse.getCliente().getPessoa().setCliente(clienteSistema);
        clienteResponse.getCliente().getPessoa().setTipoPessoa(TipoPessoaEnum.JURIDICA);


        BuscaCombos(clienteResponse.getListCombos(), idRequest, false);
        response.setData(clienteResponse);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping()
    public ResponseEntity<Response<String>> delete(@RequestBody IdRequest idRequest) {
        Response<String> response = new Response<>();
        Cliente cliente = clienteService.findById(idRequest.getId());
        if (!Util.verifica(cliente).isPresent()) {
            response.getErrors().add("Registro não encontrado, id:" + idRequest.getId());
            return ResponseEntity.badRequest().body(response);
        }
        cliente.setStatus(StatusEnum.INATIVO);
        clienteService.salva(cliente);
        return ResponseEntity.ok(new Response<>());
    }

    @PostMapping(value = "buscarFiltro")
    public ResponseEntity<Response<Page<Cliente>>> findAll(@RequestBody Cliente filtro, Pageable pageable) {
        Response<Page<Cliente>> response = new Response<>();
        Page<Cliente> clientes = clienteService.findAll(filtro, pageable);
        response.setData(clientes);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "buscarCombos")
    public ResponseEntity<Response<ResponseComCombos>> buscarCombos() {
        Response<ResponseComCombos> response = new Response<>();
        ResponseComCombos listCombos = new ResponseComCombos();

        BuscaCombos(listCombos.getListCombos(), null, true);

        response.setData(listCombos);
        return ResponseEntity.ok(response);
    }

    private void BuscaCombos(Map<String, List<ComboListResponse>> clienteResponse, IdRequest idRequest, boolean telaListagem) {
        Cliente cliente = new Cliente();
        if (idRequest != null)
            cliente.setId(idRequest.getIdCliente());

        clienteResponse.put("TipoCliente", segmentoService.getListCombo());
        clienteResponse.put("TipoPlano", planoService.getListCombo());
        if (!telaListagem) {
            clienteResponse.put("TipoPessoa", TipoPessoaEnum.getListCombo());
            clienteResponse.put("TipoEndereco", pessoaEnderecoService.getListCombo(cliente));
            clienteResponse.put("TipoTelefone", pessoaTelefoneService.getListCombo(cliente));
        }

    }



}

