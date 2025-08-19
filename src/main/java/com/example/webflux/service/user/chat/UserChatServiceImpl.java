package com.example.webflux.service.user.chat;

import com.example.webflux.model.llmclient.LlmType;
import com.example.webflux.model.user.chat.LlmChatRequestDto;
import com.example.webflux.model.user.chat.LlmChatResponseDto;
import com.example.webflux.model.user.chat.UserChatRequestDto;
import com.example.webflux.model.user.chat.UserChatResponseDto;
import com.example.webflux.service.llmclient.LlmWebClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserChatServiceImpl implements UserChatService{
    //    private final LlmWebClientService llmWebClientService;
    private final Map<LlmType, LlmWebClientService> llmWebClientServiceMap;
    @Override
    public Mono<UserChatResponseDto> getOneShotChat(UserChatRequestDto userChatRequestDto) {
        // 이것만 존재하면 Mono의 흐름안에서 관리가 안됨  -> defer 사용 -> 간단하고 짧은 소스는 Mono흐름에 넣지 않아도 됨
        //LlmChatRequestDto llmChatRequestDto = new LlmChatRequestDto(userChatRequestDto, "요청에 적절히 응답해주세요.");

        LlmChatRequestDto llmChatRequestDto = new LlmChatRequestDto(userChatRequestDto, "요청에 적절히 응답해주세요.");
        Mono<LlmChatResponseDto> chatCompletionMono = llmWebClientServiceMap.get(llmChatRequestDto.getLlmModel().getLlmType())
                .getChatCompletion(llmChatRequestDto);
        return chatCompletionMono.map(UserChatResponseDto::new);
    }
}
