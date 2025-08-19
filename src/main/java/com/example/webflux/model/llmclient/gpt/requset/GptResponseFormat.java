package com.example.webflux.model.llmclient.gpt.requset;

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
public class GptResponseFormat implements Serializable {
    @Serial
    private static final long serialVersionUID = -2761774228835569431L;
    private String type;
}
