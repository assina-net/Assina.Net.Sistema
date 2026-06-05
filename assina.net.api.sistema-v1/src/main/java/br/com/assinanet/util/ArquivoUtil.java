package br.com.assinanet.util;

import br.com.assinanet.entity.exception.NegocioException;

import javax.swing.filechooser.FileSystemView;
import javax.validation.constraints.AssertTrue;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * @author Samuel Oliveira
 */
public final class ArquivoUtil {


    private static final String LABEL_ARQUIVO = "Arquivo ";

    /**
     * Construtor privado para garantir o Singleton.
     */
    private ArquivoUtil() {

    }

    /**
     * Apaga Todos os Arquivos da Pasta
     */
    public static void apagaArquivos(String pasta) throws NegocioException {


        try {
            try (Stream<Path> entries = Files.walk(Paths.get(pasta))) {
                entries.forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            throw new NegocioException("Erro ao apagar arquivos da pasta " + pasta);
        }
    }

    /**
     * Renomeia o arquivo
     *
     * @param origem
     * @param destino
     * @return
     * @throws Exception
     */
    public static void renomearArquivo(String origem, String destino) throws NegocioException {
        File f = new File(origem);
        File fd = new File(destino);
        if (!f.exists()) {
            throw new NegocioException(LABEL_ARQUIVO + origem + " não encontrado.");
        }
        if (fd.exists()) {
            try {
                Files.delete(fd.toPath());
            } catch (Exception e) {
                throw new NegocioException(LABEL_ARQUIVO + destino + " não pode ser deletado: " + e.getMessage());
            }
        }
        try {
            Files.move(f.toPath(), fd.toPath());
        } catch (Exception e) {
            throw new NegocioException(LABEL_ARQUIVO + origem + " não pode ser copiado para " + destino + ": " + e.getMessage());
        }
    }

    /**
     * retorna o nome do arquivo no diretorio
     * ex: C:\\pasta\\arquivo.txt  retorna = arquivo.txt
     *
     * @param diretorio
     * @return
     */
    public static String obterNomeArquivoNoDiretorio(String diretorio) {
        return new File(diretorio).getName();
    }

    /**
     * Transfere o arquivo de origem para o destino informado
     *
     * @param arquivoOrigem
     * @param destino
     * @throws IOException
     */
    @SuppressWarnings("resource")
    public static void copiarArquivo(String arquivoOrigem, String destino) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel destinationChannel = null;
        try {
            sourceChannel = new FileInputStream(new File(arquivoOrigem)).getChannel();
            destinationChannel = new FileOutputStream(new File(destino)).getChannel();
            sourceChannel.transferTo(0, sourceChannel.size(),
                    destinationChannel);
        } finally {
            if (sourceChannel != null && sourceChannel.isOpen())
                sourceChannel.close();
            if (destinationChannel != null && destinationChannel.isOpen())
                destinationChannel.close();
        }
    }

    /**
     * Apaga o Arquivo
     *
     * @param caminhoArquivo
     * @throws IOException
     */
    public static void apagarArquivo(String caminhoArquivo) throws NegocioException {
        try {
            Path rootPath = Paths.get(caminhoArquivo);
            Files.delete(rootPath);

        } catch (Exception e) {
            throw new NegocioException("Erro ao apagar arquivo: " + e.getMessage());
        }
    }

    /**
     * Cria um arquivo com os dados passados com salto de linha
     *
     * @throws Exception
     */
    public static void criarPastaArquivo(String pasta, String arquivo, byte[] conteudo) throws NegocioException {
        try {
            criarPasta(pasta);

            Path path = Paths.get(pasta, arquivo).toFile().toPath();
//            Files.write(path, conteudo);
            try {
                File savedXmlFile = new File(path.toString());
                OutputStream os = new FileOutputStream(savedXmlFile);
                os.write(conteudo);
                os.flush();
                os.close();
            } catch (IOException e) {
                throw new NegocioException("Erro ao criar arquivo " + e.getMessage());
            }

        } catch (IOException e) {
            throw new NegocioException("Erro ao criar arquivo " + e.getMessage());
        }
    }

    public static byte[] getArquivo(String pasta, String arquivo) throws NegocioException {
        //if (arquivo != null && !arquivo.endsWith(".bin")) {
        //    arquivo = arquivo+".bin";
        //}
        Path path = Paths.get(pasta, arquivo).toFile().toPath();
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new NegocioException("Erro ao ler arquivo " + e.getMessage());
        }

    }

    /**
     * Cria a pasta se não existir
     *
     * @param pasta
     */
    public static void criarPasta(String pasta) throws IOException {
        File folder = new File(pasta);
        if (!folder.exists()) {
            Files.createDirectories(Path.of(folder.getAbsolutePath()));
        }
    }

    /**
     * Cria um arquivo com os dados passados
     *
     * @throws Exception
     */
    public static void criarPastaArquivo(String pasta, String arquivo, String conteudo) throws NegocioException {
        try {
            criarPasta(pasta);
            Files.write(Paths.get(pasta + "/" + arquivo), conteudo.getBytes());
        } catch (Exception e) {
            throw new NegocioException("Erro ao criar arquivo " + e.getMessage());
        }
    }

    /**
     * Cria um arquivo com os dados passados
     *
     * @throws Exception
     */
    public static Properties leArquivoTef(File caminho) throws NegocioException {

        Properties propreties = new Properties();
        try {
            try (InputStream arquivoIn = new FileInputStream(caminho)) {
                propreties.load(arquivoIn);
            }
        } catch (Exception e) {
            throw new NegocioException("Erro ao lêr arquivo " + e.getMessage());
        }

        return propreties;
    }

    /**
     * Retorna o caminho do desktop
     *
     * @return
     */
    public static String getHomeDirectory() {
        File f = FileSystemView.getFileSystemView().getHomeDirectory();
        return f.getAbsolutePath();
    }

    /**
     * Retorna o caminho absoluta da aplicação
     *
     * @return
     */
    static String getAbsolutePath() {
        return new File("").getAbsolutePath();
    }

    /**
     * Monta o Map para download
     *
     * @param name
     * @param caminho
     * @return
     * @throws IOException
     */
    public static Map<String, byte[]> getMapDownload(String name, String caminho) throws IOException {
        Map<String, byte[]> map = new HashMap<>();
        map.put(name, Files.readAllBytes(Paths.get(caminho)));
        return map;
    }

    /**
     * Verifica se o caminho informado existe
     *
     * @param caminho
     * @return true se existir e false caso contratio.
     */
    public static boolean isArquivoExistente(String caminho) {
        File file = new File(caminho);
        return file.exists();
    }

    /**
     * Calcula o tamanho do arquivo em bytes
     *
     * @param arquivo
     * @return
     */
    public static double calculaTamanhoArquivoEmMegaBytes(File arquivo) {
        if (arquivo != null && arquivo.exists()) {
            long tamanhoArquivoEmBytes = arquivo.length();
            double mega = 1024.0 * 1024;
            return tamanhoArquivoEmBytes / mega;
        }
        return 0;
    }

}