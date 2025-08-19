package com.example.webflux.model.llmclient.gemini.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GeminiChatResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -1105456784688743622L;

    private List<GeminiCandidate> candidates;

    public String getSingleText() {
        //map 사용시 Optional이 두개가 되므로 flatMap으로 하나로 만들어줌
        return candidates.stream().findFirst()
                .flatMap(candidate -> candidate.getContent().getParts().stream().findFirst()
                        .map(GeminiPart::getText))
                .orElseThrow();
    }
}
