package br.com.assinanet.service;

import br.com.assinanet.entity.Arquivo;
import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.Contrato;
import br.com.assinanet.entity.ContratoDocumento;
import br.com.assinanet.entity.enums.SistemaTipoAtributoEnum;
import br.com.assinanet.entity.enums.StatusDocumentoEnum;
import br.com.assinanet.entity.enums.TipoArquivoEnum;
import br.com.assinanet.entity.exception.NegocioException;
import br.com.assinanet.models.ContratoParteDocumentoPendente;
import br.com.assinanet.repository.ContratoDocumentoRepository;
import br.com.assinanet.repository.ContratoParteDocumentoRepository;
import br.com.assinanet.repository.ContratoRepository;
import br.com.assinanet.response.ContratoDocumentoVisualizaResponse;
import br.com.assinanet.storage.Storage;
import br.com.assinanet.storage.StorageFactory;
import br.com.assinanet.storage.TipoStorage;
import br.com.assinanet.util.CommonsUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
@Service
public class ContratoDocumentoService {


    private final ContratoDocumentoRepository contratoDocumentoRepository;
    private final ContratoRepository contratoRepository;

    private final SistemaAtributoService sistemaAtributoService;
    private final ContratoDocumentoService contratoDocumentoService;

    private final ContratoParteDocumentoRepository contratoParteDocumentoRepository;

    public ContratoDocumentoService(ContratoDocumentoRepository contratoDocumentoRepository, ContratoRepository contratoRepository, SistemaAtributoService sistemaAtributoService,
                                    @Lazy ContratoDocumentoService contratoDocumentoService, ContratoParteDocumentoRepository contratoParteDocumentoRepository) {

        this.contratoDocumentoRepository = contratoDocumentoRepository;
        this.contratoRepository = contratoRepository;
        this.sistemaAtributoService = sistemaAtributoService;

        this.contratoDocumentoService = contratoDocumentoService;
        this.contratoParteDocumentoRepository = contratoParteDocumentoRepository;
    }


    public ContratoDocumento findByDocumentoOriginalSHA256(String hash) {
        return contratoDocumentoRepository.findByDocumentoOriginalSHA256(hash);
    }


    private Boolean ValidaDocumentoAssinado(ContratoDocumento contratoDocumento) {
        List<ContratoParteDocumentoPendente> pendentes = contratoParteDocumentoRepository.verificaAssinaturasPendentesDocumento(contratoDocumento);
        return CommonsUtil.semValor(pendentes);
    }


    public ContratoDocumentoVisualizaResponse getArquivoEmDiretorio(Optional<ContratoDocumento> documento,
                                                                    String documentoSHA256,
                                                                    boolean originalBytes,
                                                                    boolean documentoOriginal) throws NoSuchAlgorithmException, NegocioException {

//        Optional<ContratoDocumento> documento = contratoDocumentoRepository.findById(idDocumento);

        ContratoDocumentoVisualizaResponse contratoDocumentoVisualizaResponse = null;
        if (documentoOriginal && !CommonsUtil.semValor(documento.get().getDocumentoOriginal())) {
            contratoDocumentoVisualizaResponse = new ContratoDocumentoVisualizaResponse(documento, documento.get().getDocumentoOriginal(), originalBytes);
            return contratoDocumentoVisualizaResponse;
        }
        if (!documentoOriginal && !CommonsUtil.semValor(documento.get().getDocumentoAssinado())) {
            contratoDocumentoVisualizaResponse = new ContratoDocumentoVisualizaResponse(documento, documento.get().getDocumentoAssinado(), originalBytes);
            return contratoDocumentoVisualizaResponse;
        }
        if (documentoOriginal
                && !CommonsUtil.semValor(documento.get().getDocumentoOriginalSHA256())
                && !documento.get().getDocumentoOriginalSHA256().endsWith(".bin")) {
            contratoDocumentoVisualizaResponse = new ContratoDocumentoVisualizaResponse(documento, documento.get().getDocumentoOriginal(), originalBytes);
            return contratoDocumentoVisualizaResponse;
        }

        if (!documentoOriginal
                && !CommonsUtil.semValor(documento.get().getDocumentoAssinadoSHA256())
                && !documento.get().getDocumentoAssinadoSHA256().endsWith(".bin")) {
            contratoDocumentoVisualizaResponse = new ContratoDocumentoVisualizaResponse(documento, documento.get().getDocumentoAssinado(), originalBytes);
            return contratoDocumentoVisualizaResponse;
        }

        String diretorioArquivo = retornaCaminhoArquivos(documento.get().getContrato().getCustodiante());
        Storage storage = retornaStorageFactory(documento.get().getContrato().getCustodiante());

        if ( !CommonsUtil.semValor( documento.get().getIdDiretorio())
                && ( !CommonsUtil.semValor( documento.get().getContrato().getIdDiretorio()))) {
            String path = Paths.get(diretorioArquivo, documento.get().getContrato().getCustodiante().getId().toString(), documento.get().getContrato().getIdDiretorio().toString(), documento.get().getIdDiretorio().toString()).toFile().getPath();
            //byte[] bytes = ArquivoUtil.getArquivo(path, documentoSHA256);

            if (documentoSHA256 != null) {
                Path fullPath = Paths.get(path, documentoSHA256).toFile().toPath();
                System.out.println("[CONTRATO_DOCUMENTO_STORAGE] Documento: "
                        + documento.get().getNomeDocumento()
                        + " | Caminho: " + fullPath);
                byte[] bytes = storage.downloadFile(fullPath.toString());

                contratoDocumentoVisualizaResponse = new ContratoDocumentoVisualizaResponse(documento, bytes, originalBytes);
            }
        }
        return contratoDocumentoVisualizaResponse;
    }

    public Storage retornaStorageFactory(Cliente cliente) {
        String tipoStorage = sistemaAtributoService.getString(SistemaTipoAtributoEnum.TIPO_STORAGE, cliente);
        if (CommonsUtil.semValor(tipoStorage)) {
            tipoStorage = sistemaAtributoService.getString(SistemaTipoAtributoEnum.TIPO_STORAGE, null);
        }
        if (CommonsUtil.semValor(tipoStorage)) {
            tipoStorage = "LOCAL";
        }
        switch (tipoStorage) {
            case "LOCAL":
                return StorageFactory.createStorage(TipoStorage.LOCAL);
            case "AZURE":
                String connectionString = getAtributoClienteOuGlobal(
                        SistemaTipoAtributoEnum.AZURE_STORAGE_CONNECTION_STRING,
                        cliente
                );
                String containerName = getAtributoClienteOuGlobal(
                        SistemaTipoAtributoEnum.AZURE_STORAGE_CONTAINER_NAME,
                        cliente
                );
                return StorageFactory.createStorage(TipoStorage.AZURE, connectionString, containerName);
            case "AWS":
                return StorageFactory.createStorage(TipoStorage.AWS);
        }
        return null;
    }

    private String getAtributoClienteOuGlobal(SistemaTipoAtributoEnum tipoAtributo, Cliente cliente) {
        String valor = sistemaAtributoService.getString(tipoAtributo, cliente);
        if (CommonsUtil.semValor(valor)) {
            valor = sistemaAtributoService.getString(tipoAtributo, null);
        }
        return valor;
    }

    public String retornaCaminhoArquivos(Cliente cliente) {
        String tipoStorage = sistemaAtributoService.getString(SistemaTipoAtributoEnum.TIPO_STORAGE, cliente);
        if (CommonsUtil.semValor(tipoStorage)) {
            tipoStorage = sistemaAtributoService.getString(SistemaTipoAtributoEnum.TIPO_STORAGE, null);
        }
        if (CommonsUtil.semValor(tipoStorage)) {
            tipoStorage = "LOCAL";
        }
        String diretorioArquivo = "";
        switch (tipoStorage) {
            case "LOCAL": {
                diretorioArquivo = sistemaAtributoService.getString(SistemaTipoAtributoEnum.DIRETORIO_ARQUIVOS, cliente);
                if (CommonsUtil.semValor(diretorioArquivo)) {
                    diretorioArquivo = sistemaAtributoService.getString(SistemaTipoAtributoEnum.DIRETORIO_ARQUIVOS, null);
                }
                if (CommonsUtil.sistemaWindows()) {
                    diretorioArquivo = "c:" + diretorioArquivo;
                }
                break;
            }
            case "AZURE": {
                diretorioArquivo = sistemaAtributoService.getString(SistemaTipoAtributoEnum.DIRETORIO_STORAGE_AZURE, cliente);
                if (CommonsUtil.semValor(diretorioArquivo)) {
                    diretorioArquivo = sistemaAtributoService.getString(SistemaTipoAtributoEnum.DIRETORIO_STORAGE_AZURE, null);
                }
                break;
            }
            case "AWS": {
                break;
            }
        }
        return diretorioArquivo;
    }

    public Arquivo persisteArquivoEmDiretorio(ContratoDocumento documento, TipoArquivoEnum tipoArquivo) throws NoSuchAlgorithmException, NegocioException {

        String diretorioArquivo = retornaCaminhoArquivos(documento.getContrato().getCustodiante());

        Arquivo arquivo = new Arquivo();
        arquivo.setCliente(documento.getContrato().getCustodiante());
        arquivo.setContrato(documento.getContrato());

        boolean salvarDocumento = false;

        if (documento.getContrato().getIdDiretorio() == null) {
            documento.getContrato().setIdDiretorio(UUID.randomUUID());
            contratoRepository.save(documento.getContrato());
        }

        if (documento.getIdDiretorio() == null) {
            documento.setIdDiretorio(UUID.randomUUID());
            contratoDocumentoRepository.save(documento);
        }

        String path = Paths.get(diretorioArquivo, documento.getContrato().getCustodiante().getId().toString(), documento.getContrato().getIdDiretorio().toString(), documento.getIdDiretorio().toString()).toFile().getPath();

        String hashArquivo;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedByetArray = new byte[0];
        byte[] hashedByteFile = null;
        String hashedEncoded = null;
        boolean documentoOriginal = false;
        boolean documentoAssinado = false;

        if (TipoArquivoEnum.ORIGNAL.equals(tipoArquivo) && documento.getDocumentoOriginal() != null) {
            hashedByteFile = documento.getDocumentoOriginal();

            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(hashedByteFile);
            hashedEncoded = String.format("%032x", new BigInteger(1, messageDigest.digest())) + ".bin";

            documentoOriginal = true;

        } else if (TipoArquivoEnum.ASSINADO.equals(tipoArquivo) && documento.getDocumentoAssinado() != null) {
            hashedByteFile = documento.getDocumentoAssinado();

            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(hashedByteFile);
            hashedEncoded = String.format("%032x", new BigInteger(1, messageDigest.digest())) + ".bin";

            documentoAssinado = true;

        }
        if (hashedByteFile != null) {
            Path fullPath = Paths.get(path, hashedEncoded).toFile().toPath();
            Storage storage = retornaStorageFactory(documento.getContrato().getCustodiante());
            try {
                storage.uploadFile(fullPath.toString(), hashedByteFile);
            } catch (RuntimeException e) {
                System.out.println("[CONTRATO_DOCUMENTO_UPLOAD] Falha ao gravar documento: "
                        + documento.getNomeDocumento() + " | Caminho: " + fullPath
                        + " | Erro: " + e.getMessage());
                throw new IllegalStateException(
                        "Não foi possível salvar o documento no armazenamento. O contrato não foi salvo.",
                        e
                );
            }

            if (documentoOriginal) {
                documento.setDocumentoOriginalSHA256(hashedEncoded);
                documento.setDocumentoOriginal(null);
            } else if (documentoAssinado) {
                documento.setDocumentoAssinadoSHA256(hashedEncoded);
                documento.setDocumentoAssinado(null);
            }
            salvarDocumento = true;
        }
        if (salvarDocumento) {
            if (!CommonsUtil.semValor(documento.getId())) {
                if (ValidaDocumentoAssinado(documento)) {
                    documento.setDocumentoAssinado();
                } else {
                    documento.setStatusDocumento(StatusDocumentoEnum.PARCIALMENTEASSINADO);
                }
                contratoDocumentoRepository.save(documento);
            }
            System.out.println("Documento: "+documento.getNomeDocumento()+" salvo no storagem com sucesso!");
        }

        return arquivo;
    }


    public Arquivo persisteArquivoEmDiretorioMigra(ContratoDocumento documento, TipoArquivoEnum tipoArquivo) throws NoSuchAlgorithmException, NegocioException {

        String diretorioArquivo = retornaCaminhoArquivos(documento.getContrato().getCustodiante());

        Arquivo arquivo = new Arquivo();
        arquivo.setCliente(documento.getContrato().getCustodiante());
        arquivo.setContrato(documento.getContrato());

        boolean salvarDocumento = false;

        if (documento.getContrato().getIdDiretorio() == null) {
            documento.getContrato().setIdDiretorio(UUID.randomUUID());
            contratoRepository.saveAndFlush(documento.getContrato());
        }

        if (documento.getIdDiretorio() == null) {
            documento.setIdDiretorio(UUID.randomUUID());
            contratoDocumentoRepository.saveAndFlush(documento);
        }

        String path = Paths.get(diretorioArquivo, documento.getContrato().getCustodiante().getId().toString(), documento.getContrato().getIdDiretorio().toString(), documento.getIdDiretorio().toString()).toFile().getPath();

        String hashArquivo;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedByetArray = new byte[0];
        byte[] hashedByteFile = null;
        String hashedEncoded = null;
        boolean documentoOriginal = false;
        boolean documentoAssinado = false;

        if (TipoArquivoEnum.ORIGNAL.equals(tipoArquivo) && documento.getDocumentoOriginal() != null) {
            hashedByteFile = documento.getDocumentoOriginal();

            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(hashedByteFile);
            hashedEncoded = String.format("%032x", new BigInteger(1, messageDigest.digest())) + ".bin";

            documentoOriginal = true;

        } else if (TipoArquivoEnum.ASSINADO.equals(tipoArquivo) && documento.getDocumentoAssinado() != null) {
            hashedByteFile = documento.getDocumentoAssinado();

            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(hashedByteFile);
            hashedEncoded = String.format("%032x", new BigInteger(1, messageDigest.digest())) + ".bin";

            documentoAssinado = true;

        }
        if (hashedByteFile != null) {
            Path fullPath = Paths.get(path, hashedEncoded).toFile().toPath();
            Storage storage = retornaStorageFactory(documento.getContrato().getCustodiante());
            try {
                storage.uploadFile(fullPath.toString(), hashedByteFile);
            } catch (RuntimeException e) {
                System.out.println("[CONTRATO_DOCUMENTO_UPLOAD] Falha ao migrar documento: "
                        + documento.getNomeDocumento() + " | Caminho: " + fullPath
                        + " | Erro: " + e.getMessage());
                throw new IllegalStateException(
                        "Não foi possível migrar o documento para o armazenamento.",
                        e
                );
            }

            if (documentoOriginal) {
                documento.setDocumentoOriginalSHA256(hashedEncoded);
                documento.setDocumentoOriginal(null);
            } else if (documentoAssinado) {
                documento.setDocumentoAssinadoSHA256(hashedEncoded);
                documento.setDocumentoAssinado(null);
            }
            salvarDocumento = true;
        }
        if (salvarDocumento) {
            if (!CommonsUtil.semValor(documento.getId())) {
                contratoDocumentoRepository.saveAndFlush(documento);
            }
            System.out.println("Documento: "+documento.getNomeDocumento()+" salvo no storagem com sucesso!");
        }

        return arquivo;
    }

}
