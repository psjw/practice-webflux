package com.example.webflux.service.llmclient;

import com.example.webflux.model.llmclient.LlmType;
import com.example.webflux.model.llmclient.gpt.requset.GptChatRequestDto;
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
public class GptWebClientService implements LlmWebClientService {
    private final WebClient webClient;
    @Value("${llm.gpt.key}")
    private String gptApiKey;

    @Override
    public Mono<LlmChatResponseDto> getChatCompletion(LlmChatRequestDto requestDto) {
        GptChatRequestDto gptChatRequestDto = new GptChatRequestDto(requestDto);
        return webClient.post().uri("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + gptApiKey)
                .bodyValue(gptChatRequestDto)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            //map사용시 Mono안에 Mono.Error 발생하므로 flatMap
                            .flatMap(body -> {
                                log.error("Error Response: {}", body);
                                return Mono.error(new RuntimeException("API 요청 실패 : " + body));
                            });
                })) //에러 처리
                .bodyToMono(GptChatResponseDto.class)
                .map(LlmChatResponseDto::new)
                ;
    }

    @Override
    public LlmType getLlmType() {
        return LlmType.GPT;
    }
}
