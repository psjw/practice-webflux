package com.example.webflux.chapter1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@SpringBootTest
public class WebClientTest {

    private WebClient webClient = WebClient.builder().build();

    /**
     * WebFlux의 블로킹 처리 원칙
     * 1. R2DBC, WebClient 등을 이용해 IO 블로킹을 가능한한 최소화 해야 하며
     * 2. 어쩔수 없이 블로킹이 발생하는 요소, 병렬처리, 오래걸리는 작업은 Scheduler를 이용해 스레드를 분리해야되고
     * 3. 리액티브 프로그래밍의 기본원칙은 스레드 변경 최소화다 불필요한 스레드 분리는 삼가해야 한다.
     */

    @Test
    public void testWebClient(){
        //flux로는 scheduler를 사용하지 않았는데 어떻게 다른 스레드가 실행 되는 건가?
        //Flux 내부적으로  fluxReceive
        // .retrieve()-> 요청을 받아오는 블로킹 동작시 네티 이벤트로 등록
        // .retrieve()가 완료되벤 callback 함수(intFlux.subscrib)를네티가 이벤트 루프 스레드를할당해서 실행
        // WebClinet를 사용하면 스케쥴러의 스레드의 지원이 없이도 완벽하게 비동기로 블로킹 회피가 가능
        // 이걸사용하면 스케쥴러를 따로 할당해야하는 문제도 해결
        // 스케쥴러는 언제 사용 ?
        // 1. 어쩔수 없이 블로킹이 발생하는 요소
        // 2. 마땅한 라이브러리가 없는 I/O 작업
        // 3. 병렬처리를 하고 싶을 대, 이벤트 루프 쓰레드가 할일이 너무 많을때
        // 비동기 DB 조회 라이브러리 R2DBC를 경험부족으로 전적으로 신뢰하지는 못함
        // 그래서 신뢰도가 중요한 DB작업에는 JPA를 사용하는데 이때 스케쥴러 (boundedElastic)을 사용하여 스레드를 분리
        // 중요한 작업(회원가입, 로그인등)시 발생하는 모든 작업 -> 스레드를 분리하여 JPA 활용
        // 실시간으로 저장할 필요는 없는데이터 -> MQ활용
        // 신뢰성이 떨어져도 되는 조회 -> R2DBC 활용

        Flux<Integer> intFlux = webClient.get()
                .uri("http://localhost:8080/reactive/onenine/flux")
                .retrieve()
                .bodyToFlux(Integer.class);

        intFlux.subscribe(data -> {
            System.out.println("처리되고 있는 스레드 이름 : "+ Thread.currentThread().getName());
            System.out.println("WebFlux가 구독중 : " + data);
        });

        System.out.println("Netty 이벤트 루프로 스레드 복귀 !!");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
