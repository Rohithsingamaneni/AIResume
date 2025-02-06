package com.ai_resume_sarcasmbe.airesume.controller;

import com.ai_resume_sarcasmbe.airesume.service.AIService;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ResumeController {
    public final Tika tika=new Tika();
    private final AIService aiService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> uploadResume(@RequestPart("file") MultipartFile file) {
        return Mono.fromCallable(() -> extractText(file))
                .subscribeOn(Schedulers.boundedElastic()) // Moves execution to a blocking thread pool
                .flatMap(aiService::analyzeResume)
                .onErrorReturn("Oops! AI got lazy. Try again later");
    }

    private String extractText(MultipartFile file) {
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        try (InputStream input = file.getInputStream()) {
            new AutoDetectParser().parse(input, handler, metadata);
            return handler.toString();
        } catch (IOException | SAXException | TikaException e) {
            return "Error processing the file. Please upload a valid document.";
        }
    }
}
