package br.com.assinanet.util;

import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CompactadorUtil {

    //Constantes
    private static final int TAMANHO_BUFFER = 5120; // 5kb

    public static void compactarZip(String nomeArquivoZip, ArrayList<File> arquivos) throws IOException {
        int cont;
        byte[] dados = new byte[TAMANHO_BUFFER];

        final FileOutputStream destino = new FileOutputStream(new File(nomeArquivoZip));
        final ZipOutputStream saida = new ZipOutputStream(new BufferedOutputStream(destino));

        try {
            for (File arquivo : arquivos) {
                final FileInputStream streamDeEntrada = new FileInputStream(arquivo);
                final BufferedInputStream origem = new BufferedInputStream(streamDeEntrada, TAMANHO_BUFFER);
                final ZipEntry entry = new ZipEntry(arquivo.getName());
                saida.putNextEntry(entry);

                while ((cont = origem.read(dados, 0, TAMANHO_BUFFER)) != -1) {
                    saida.write(dados, 0, cont);
                }
                origem.close();
            }

            saida.close();
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public static byte[] compactarZipByte(Map<String, byte[]> arquivos) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        for (Map.Entry<String, byte[]> reporte : arquivos.entrySet()) {
            ZipEntry entry = new ZipEntry(reporte.getKey());
            entry.setSize(reporte.getValue().length);
            zos.putNextEntry(entry);
            zos.write(reporte.getValue());
        }
        zos.closeEntry();
        zos.close();
        return baos.toByteArray();
    }

    public static byte[] compactarZipByte(String nome, byte[] arquivo) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);


        ZipEntry entry = new ZipEntry(nome);
        entry.setSize(arquivo.length);
        zos.putNextEntry(entry);
        zos.write(arquivo);

        zos.closeEntry();
        zos.close();
        return baos.toByteArray();
    }

    public static void descompactarZip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    public static byte[] extrairArquivorZip( byte[] arquivoZip) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteArrayInputStream arquivo = new ByteArrayInputStream(arquivoZip);
        ZipInputStream zipIn = new ZipInputStream(arquivo);
        ZipEntry entry = zipIn.getNextEntry();

        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();

        return bos.toByteArray();
    }


    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
}