package com.example.webflux.service.llmclient;

import com.example.webflux.model.llmclient.LlmType;
import com.example.webflux.model.llmclient.gemini.request.GeminiChatRequestDto;
import com.example.webflux.model.llmclient.gemini.response.GeminiChatResponseDto;
import com.example.webflux.model.llmclient.gpt.response.GptChatResponseDto;
import com.example.webflux.model.user.chat.LlmChatRequestDto;
import com.example.webflux.model.user.chat.LlmChatResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiWebClientService implements LlmWebClientService {

    private final WebClient webClient;

    @Value("${llm.gemini.key}")
    private String geminiApiKey;

    @Override
    public Mono<LlmChatResponseDto> getChatCompletion(LlmChatRequestDto requestDto) {
        GeminiChatRequestDto geminiChatRequestDto = new GeminiChatRequestDto(requestDto);
        return webClient.post()
                .uri("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey)
                .bodyValue(geminiChatRequestDto)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            //map사용시 Mono안에 Mono.Error 발생하므로 flatMap
                            .flatMap(body -> {
                                log.error("Error Response: {}", body);
                                return Mono.error(new RuntimeException("API 요청 실패 : " + body));
                            });
                })) //에러 처리
                .bodyToMono(GeminiChatResponseDto.class)
                .map(LlmChatResponseDto::new)
                ;
    }

    @Override
    public LlmType getLlmType() {
        return LlmType.GEMINI;
    }
}
