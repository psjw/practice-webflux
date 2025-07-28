package com.example.webflux.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/reactive")
@Slf4j
public class ReactiveProgrammingExampleController {

    //1~9까지 출력하는 api
    @GetMapping("/onenine/legacy")
    public Mono<List<Integer>> produceOneToNineLeacy() {
        return Mono.fromCallable(() -> {
            List<Integer> sink = new ArrayList<>();
            for (int i = 1; i < 10; i++) {
                try {
                    Thread.sleep(500); //0.5초동안 Sleep
                } catch (Exception e) {

                }
                sink.add(i);
            }
            return sink;
        }).subscribeOn(Schedulers.boundedElastic());
    }


    //1~9까지 출력하는 api
    @GetMapping("/onenine/defer")
    public Mono<List<Integer>> produceOneToNineDefer() {
        return Mono.defer(() -> {
            List<Integer> sink = new ArrayList<>();
            for (int i = 1; i < 10; i++) {
                try {
                    Thread.sleep(500); //0.5초동안 Sleep
                } catch (Exception e) {

                }
                sink.add(i);
            }
            return Mono.just(sink);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    //1~9까지 출력하는 api
    @GetMapping("/onenine/list")
    public List<Integer> produceOneToNine() {
        //Mono나 Flux가 아니지만 Spring이 구독을 함
        //Mono나 Flux가 아닌 동기적 객체를 반환시 자동으로 Mono.just(sink)로 반환
        //아래의 코드는 subscribe()시에 호출이 되는게 아니라 함수 호출시 바로 호출되므로 blocking
        //Mono나 Flux 사용
        List<Integer> sink = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            try {
                Thread.sleep(500); //0.5초동안 Sleep
            } catch (Exception e) {

            }
            sink.add(i);
        } //총 4.5초 소요 -> 요건사항 : 데이터가 처리 될때마다 바로 보여주세요
        return sink;
    }

    //Flux와 Mono를 사용하면 아주 쉽게 리액티브한 프로그래밍이 가능
    @GetMapping("/onenine/flux")
    public Flux<Integer> produceOneToNineFlux() {
        return Flux.<Integer>create(sink -> {
            for (int i = 1; i < 10; i++) {
                try {
                    // reactor-http-nio-2 현제 처리되고 있는 이벤트루프의 스레드의 이름 -> 블로킹 되지 않게 만들어야함
                    // -> subscribeOn을 사용 (boundedElastic-1로변경) -> 콜드 시퀀스라고 함
                    // -> 구독과 관계 없이 발행(publish()) -> 핫 시퀀스로라고 함
                    //     구독자가 데이터를 전부 소화 안될수있음 -> 백프레셔 같은 것을 사용하여 방출속도 조절(동영상 이런거 아니면 일반적으로 사용할일이 없음)
                    log.info("현채 처리하고 있는 스레드 이름 : {}", Thread.currentThread().getName());
                    Thread.sleep(500); //0.5초동안 Sleep
                } catch (Exception e) {

                }
                sink.next(i);
            } //총 4.5초 소요 -> 요건사항 : 데이터가 처리 될때마다 바로 보여주세요
            sink.complete();//사용후 닫아줌
        }).subscribeOn(Schedulers.boundedElastic());
    }
    //리액티브 스트림 구현체 Flux, Mono를 사용하여 발생하는 데이터를 바로바로 리액티브하게 처리
    //비동기로동작 -> 논 블로킹하게 동작 해야한다. -> Thread.sleep이 블로킹하므로 우회하여 회피해야함

    //리액티브 프로그래밍 필수 요소
    //1. 데이터가 준비될 때 마다 바로바로 리액티브하게 처리
    //  > 리액티브 스트림 구현체 Flux, Mono를 사용하여 발생하는 데이터 바로바로 처리
    //2. 로직을 짤 때는 반드시 논 블로킹하게 짜야함
    //  > 이를 위해 비동기 프로그래밍이 필요
}
