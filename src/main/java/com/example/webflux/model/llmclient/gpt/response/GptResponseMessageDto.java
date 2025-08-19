package com.example.webflux.model.llmclient.gpt.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class GptResponseMessageDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 6667907440387219186L;

    private String finishReason;
    private GptResponseMessageDto message;
}
