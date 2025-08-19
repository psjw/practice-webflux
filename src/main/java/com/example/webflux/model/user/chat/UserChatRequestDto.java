package com.example.webflux.model.user.chat;

import com.example.webflux.model.llmclient.LlmModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserChatRequestDto implements Serializable {
    //Serializable 다른 I/O(네트워크, 파일)로 전달 할떄 역직렬화 해서 하지만 요즘은 JSON이라 상관없음
    @Serial
    private static final long serialVersionUID = 4995931142869792388L;

    private String request;
    private LlmModel llmModel;
}
