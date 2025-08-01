package com.example.webflux.chapter2;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class OperatorFlatMapTest {

    //Flat - 평탄화
    //Map - 객체변환
    /*
    Mono<Mono<T>> -> Mono<T>
    Mono<Flux<T>> -> Flux<T>
    Flux<Mono<T>> -> Flux<T>
    mono.block()으로 mono 안의 객체를 꺼낼 수 있으나 이건 mono 안의 객체가 방출될 때 까지 스레드를 블로킹 시키는 것이기 때문에
    절대 사용하면 안됨
     */
    @Test
    public void testMonoToFlux() {
        Mono<Integer> one = Mono.just(1);
        //Mono<Flux<Integer>> = 언제 만들어질지 모르는 객체 + 언제 만들어질지 모르는 객체 = 언제만들어질지 모르는 객체
        // 무한대 + 무한대 = 무한대
        // 비동기(Mono, Flux) + 비동기 = 비동기
        //이렇게 비동기가 겹쳐진 구조를 비동기 1개로 평탄화 시켜주는 것이 FlatMap
//        Mono<Flux<Integer>> integerFlux = one.map(data -> {
//            return Flux.just(data, data + 1, data + 2);
//        });

        //just는 순서 보장
        Flux<Integer> integerFlux = one.flatMapMany(data -> {
            return Flux.just(data, data + 1, data + 2);
        });
        integerFlux.subscribe(data -> System.out.println("data = " + data));
    }

    @Test
    public void testClientFlatMap() {
        //중첩된 비동기구조
//        Flux<Mono<String>> just = Flux.just(callWebClient("1단계 - 문제 이해하기", 1500),
//                callWebClient("2단계 - 문제 단계별로 풀어가기", 1500),
//                callWebClient("3단계 - 최종 응답", 1500));

//        Flux<Mono<String>> objectFlux = Flux.<Mono<String>>create(sink -> {
//            sink.next(callWebClient("1단계 - 문제 이해하기", 1500));
//            sink.next(callWebClient("2단계 - 문제 단계별로 풀어가기", 1500));
//            sink.next(callWebClient("3단계 - 최종 응답", 1500));
//            sink.complete();
//        });

        //순서를 보장하지 않는다(flatMap)
        Flux<String> flatMap = Flux.just(callWebClient("1단계 - 문제 이해하기", 1500),
                        callWebClient("2단계 - 문제 단계별로 풀어가기", 1500),
                        callWebClient("3단계 - 최종 응답", 1500))
                .flatMap(monoData -> {
                    return monoData.map(data -> data+"추가 가공!");
                });

        flatMap.subscribe(data -> System.out.println("FlatMapped data = " + data));

        //순서를 보장 (flatMapSequential)
        Flux<String> flatMapSequential = Flux.just(callWebClient("1단계 - 문제 이해하기", 1500),
                        callWebClient("2단계 - 문제 단계별로 풀어가기", 1500),
                        callWebClient("3단계 - 최종 응답", 1500))
                .flatMapSequential(monoData -> {
                    return monoData;
                });

        flatMapSequential.subscribe(data -> System.out.println("FlatMap Sequential data = " + data));


        //map을 통한 데이터 가공이 없는경우
        Flux<String> merge = Flux.merge(callWebClient("1단계 - 문제 이해하기", 1500),
                        callWebClient("2단계 - 문제 단계별로 풀어가기", 1500),
                        callWebClient("3단계 - 최종 응답", 1500));
        //            .map(~~~~~~) : 여기서 추가로 가공하면 flatMap이랑 비슷한 구조

        merge.subscribe(data -> System.out.println("merge = " + data));

        //순서를 보장
        Flux<String> mergeSequential = Flux.mergeSequential(callWebClient("1단계 - 문제 이해하기", 1500),
                callWebClient("2단계 - 문제 단계별로 풀어가기", 1500),
                callWebClient("3단계 - 최종 응답", 1500));
        //            .map(~~~~~~) : 여기서 추가로 가공하면 flatMap이랑 비슷한 구조
        mergeSequential.subscribe(data -> System.out.println("mergeSequential = " + data));

        //concat은 순서에 의존해서 실행 -> 비효율적
        //mergeSequential은 한번에 실행하고 나중에 순서를 정리

//        Flux<String> objectFlux = Flux.<Mono<String>>create(sink -> {
//            sink.next(callWebClient("1단계 - 문제 이해하기", 1500));
//            sink.next(callWebClient("2단계 - 문제 단계별로 풀어가기", 1500));
//            sink.next(callWebClient("3단계 - 최종 응답", 1500));
//            sink.complete();
//        }).flatMap(monoData -> {
//            return monoData;
//        });


        Mono<String>  monomonoString = Mono.just(Mono.just("안녕"))
                .flatMap(data -> data);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    //concat, concatMa 이런건 쓰지 말자


    /*
        Flux<Mono<T>>
        Mono<Mono<T>> --> 이 구조 안에 있는 Mono는 flatMap, merge로 벗겨낼 수 있다.
                      --> flatMap, merge 순서를 보장하지 않으니 순서 보장이 필요하면 sequential을 사용하자
        Mono<Flux<T>> --> flatMapMany -> 얘는 Flux<T>의 순서가 보장
        Flux<Flux<T>> / collectList --> Flux<Mono<List<T>> --> Flux<List<T>>

        Flux, Mono 안에서 외부 api 호출, DB 호출 등의 비동기 작업 흐름을 시작하면
        Flux, Mono안에 Flux, Mono가 중첩된 구조가 형성된다.
     */
    public Mono<String> callWebClient(String request, long delay) {
        return Mono.defer(() -> {
                    try {
                        Thread.sleep(delay);
                        return Mono.just(request + " -> 딜레이 : " + delay);
                    } catch (InterruptedException e) {
                        return Mono.empty();
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }


}
