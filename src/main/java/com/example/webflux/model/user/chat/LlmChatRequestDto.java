package com.example.webflux.model.user.chat;

import com.example.webflux.model.llmclient.LlmModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LlmChatRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -8789430620915714497L;

    private String userRequest;
    /*
    systemPrompt가 userRequest에 포함 되는 내용보다 더 높은 강제성과 우선순위를 가집니다.
     */
    private String systemPrompt;
    private boolean useJson; //LLM이 JSON형식으로 응답
    private LlmModel llmModel;

    public LlmChatRequestDto(UserChatRequestDto userChatRequestDto, String systemPrompt) {
        this.llmModel = userChatRequestDto.getLlmModel();
        this.systemPrompt = systemPrompt;
        this.userRequest = userChatRequestDto.getRequest();
    }
}
