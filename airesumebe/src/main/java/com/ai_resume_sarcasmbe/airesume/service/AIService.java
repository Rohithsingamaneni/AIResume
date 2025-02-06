package com.ai_resume_sarcasmbe.airesume.service;

import com.ai_resume_sarcasmbe.airesume.config.WebClientConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIService {

    private final WebClient webClient ;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @PostConstruct
    public void checkApiKey() {
        System.out.println("Gemini API Key: " + geminiApiKey);
    }



    public Mono<String> analyzeResume(String resumeText) {
        
        String sarcasticPrompt = "Oh wow, another resume! Let's see what groundbreaking skills and experience this one brings. "
                + "Be brutally honest and give the most sarcastic feedback possible: " + resumeText;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", sarcasticPrompt))
                ))
        );

       
        System.out.println("Request Payload: " + requestBody);

        return webClient.post()
                .uri("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)  
                .retrieve()
                .bodyToMono(Map.class)  
                .map(response -> {
                    
                    System.out.println("Raw API Response: " + response);

                    
                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                    if (candidates != null && !candidates.isEmpty()) {
                        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            return (String) parts.get(0).get("text"); 
                        }
                    }
                    return "No sarcastic remarks generated... I guess the resume is just too perfect.";
                })
                .doOnSuccess(response -> System.out.println("Extracted Response: " + response))
                .doOnError(error -> System.err.println("Error parsing Gemini API response: " + error.getMessage()));
    }

//    private String formatSarcasticResponse(String aiResponse){
//        return "AI Review: "+aiResponse+
//                "\n\nP.S. AI is watching. 👀 Maybe consider a new career path?";
//    }
}
