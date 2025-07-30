package com.example.webflux.chapter2;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BasicFluxOperatorTest {
    /**
     * Flux
     * 데이터 : just, empty, from-시리즈
     * 함수 : defer, create
     */

    @Test
    public void testFluxFromData() {
        Flux.just(1, 2, 3, 4)
                .subscribe(data -> System.out.println("data = " + data));
        List<Integer> basicList = List.of(1, 2, 3, 4);
        Flux.fromIterable(basicList)
                .subscribe(data -> System.out.println("data fromIterable = " + data));
    }

    /**
     * Flux defer -> 안에서 Flux 객체를 반환해줘야 합니다.
     * Flux create -> 안에서 동기적인 객체를 반환해줘야 합니다.
     */

    @Test
    public void defer() {
        Flux.defer(() -> {
            return Flux.just(1, 2, 3, 4);
        }).subscribe(data -> System.out.println("data from defer = " + data));

        Flux.create(sink -> {
            sink.next(1);
            sink.next(2);
            sink.next(3);
            sink.complete(); //sink가 언제 끝나는지 알수가 없기 때문에 sink 사용할 때 마지막에 호출
        }).subscribe(data -> System.out.println("data from sink = " + data));
    }

    @Test
    public void testSinkDetail() {
        //sink 사용 -> 동기적인 데이터 마이그레이션
        //Flux의 방출타이밍을 지정 -> 로직이 엄청 복잡한 상황에서 특정 데이터만 뽑아냄
        Flux.<String>create(sink -> {
                    AtomicInteger counter = new AtomicInteger(0);
                    recursiveFunction(sink, counter);
                })
                .subscribe(data -> System.out.println("data from recursive = " + data));

        Flux.<String>create(sink -> {
                    recursiveFunction1(sink);
                    recursiveFunction1(sink);
                    recursiveFunction1(sink);
                })
                .contextWrite(Context.of("counter", new AtomicInteger(0)))
                .subscribe(data -> System.out.println("data from recursive1 = " + data));
    }

    public void recursiveFunction(FluxSink<String> sink, AtomicInteger counter) {
        if (counter.incrementAndGet() < 10) { // = ++int
            sink.next("sink count " + counter);
            recursiveFunction(sink, counter);
        } else {
            sink.complete();
        }
    }

    public void recursiveFunction1(FluxSink<String> sink) {
        AtomicInteger counter = sink.contextView().get("counter");
        if (counter.incrementAndGet() < 10) { // = ++int
            sink.next("1 sink count " + counter);
            recursiveFunction1(sink);
        } else {
            sink.complete();
        }
    }

    //ThreadLocal -> context
    /*
    Flux의 흐름 시작 방법
    1. 데이터로 부터 : just, empty, from시리즈
    2. 함수로 부터 : defer (Flux 객체를 return), create(동기적인 객체를 return - next)
     */
    @Test
    public void testFluxCollectList() {
        Mono<List<Integer>> listMono = Flux.<Integer>just(1, 2, 3, 4, 5) //첫번쨰는 빈 함수로부터, 두번째는 데이터로부터 시작할 수 있음
                .map(data -> data * 2)
                .filter(data -> data % 4 == 0)
                .collectList();//MonoL<List<...>> -> 처리 될때까지 기다렸다가 List로 모아줌 -> 언제 끝날지 모르니깐 Mono로 감싸줌
        listMono.subscribe(data -> System.out.println("collectList가 변환한 list data! = " + data));
    }
    /*
    Mono -> Flux변환 flatMapMany
    Flux -> Mono변환 collectList
     */
}
