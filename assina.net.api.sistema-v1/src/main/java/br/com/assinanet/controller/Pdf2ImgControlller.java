package br.com.assinanet.controller;

import br.com.assinanet.response.Response;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pdf2img")
@CrossOrigin(origins = "*")
public class Pdf2ImgControlller {

    @PostMapping
    public ResponseEntity<?> convertPdfToImage(@RequestBody byte[] pdfByte) throws Exception {

        List<byte[]> imgList = new ArrayList<>();
        Response<List<byte[]>> response = new Response<>();
        byte[] pdfFile = Base64.getDecoder().decode(new String(pdfByte).getBytes(StandardCharsets.UTF_8));

        try (final PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();
            for (int page = 0; page < pageCount; page++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 72, ImageType.RGB);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                ImageIO.write(image, "png", stream);
                imgList.add(stream.toByteArray());
            }
            response.setData(imgList);
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e);
        }
        return ResponseEntity.ok(response);
    }
}
