package com.example.webflux;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@SpringBootTest
public class SubscriberPublisherAsyncTest {

    /**
     * 1. Flux에는 데이터가 들어 있는게 아니라 함수코드 덩어리가 들어가 있다.
     *    그래스 구독을 해서 함수를 실행시키는 순간부터 데이터가 발행
     * 2. 스레드가 1개라면 Flux 만으로는 어떻게 해도 블로킹을 회피할 수 없다.
     *    스케쥴러로 추가 스레드를 할당하여 대신 작업 시켜야 한다.
     */


    @Test
    public void produceOneToNineFlux() {
        //Subscriber - Publisher 패턴이란?
        //->한글로 구독자 - 발행자
        //->Flux를 구독하면 발행이 시작된다.
        //->이 간단한 개념이 Subscriber - Publisher 패턴이다.

        //Flux로 어떻게 블로킹을 회피할 수 있을까?
        //-> 스레드 1개만 사용해서는 절대로 블로킹을 회피할 수 없다.
        //-> flatMap()을 사용해도 블로킹 회피 불가
        //-> OS에 위임하는것은 별도 라이브러리 필요
        //-> Thread를 추가 할당 블로킹 로직을 대신 수행하게 함
        //-> Reactor의 스케쥴러를 사용해서 스레드를 추가할당하여 블로킹을 회피


        //Flux.<Integer> 다른 스레드에서 실행시 어떤 자료형이 반환될지 확신할수 없어서
        //Integer를 반환할것이라고 선언 -> 고정되지 않는 객체가 반환되면 유지보수 어려움
        Flux<Integer> intFlux = Flux.<Integer>create(sink -> {
            for (int i = 1; i < 10; i++) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {

                }
                sink.next(i);
            } //총 4.5초 소요 -> 요건사항 : 데이터가 처리 될때마다 바로 보여주세요
            sink.complete();//사용후 닫아줌
        }).subscribeOn(Schedulers.boundedElastic());
        //스케쥴러는 어느정도 블로킹이 발생해도 괜찮음
        //이벤트 루프일에는 관여하지 않고 직접 할당하는 일만 관여
        //스케쥴러가 제공하는 스레드는 톰캣의 스레드 처럼 어느정도 블로킹 되어도 괜찮다
        //이 스레드는 우리가 원하는곳에 마음대로 사용이 가능

        //스케쥴러가 제공하는 스레드가 중요한 스레드(이벤트 루프 스레드) 대신 대기 하는 것이
        //블로킹 회피의 기본적인 전략

        //이벤트를 발생시켜 OS에게 대기를 위임하는 방법은 다음 시간에 학습

        intFlux.subscribe(data -> {
            //블로킹 코드가 들어가 있는 Flux 코드는 스케쥴러의 스레드가 구독하고 실행시키지만 아래의 코드는
            //메인 스레드가 그냥 코드를 통과
            System.out.println("처리되고 있는 스레드 이름 : "+ Thread.currentThread().getName());
            System.out.println("WebFlux가 구독중 : " + data);
        });

        System.out.println("Netty 이벤트 루프로 스레드 복귀 !!");
        //테스트 환경이라 메인스레드는 자기 할 일을 다하면 죽음
        //메인 스레드가 살아 있어야지만 다른 스레드도 일을 하고 있을 수 있음
        //그래서 테스트를 위해 메인 스레드를 sleep으로 잡아둠
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
}
