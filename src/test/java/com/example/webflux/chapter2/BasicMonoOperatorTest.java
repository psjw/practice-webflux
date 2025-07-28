package com.example.webflux.chapter2;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class BasicMonoOperatorTest {

    //just, empty
    @Test
    public void startMonoFromData() {
        Mono.just(1).subscribe(data -> System.out.println("data" + data));

        //데이터가 없으므로 흐름 자체가 실행이 안됨
        //ex)시스템 에러가 발생했을때 로그를 남기고 empty의 Mono를 전판
        Mono.empty().subscribe(data -> System.out.println("data" + data));
    }

    //fromCallable, defer

    /**
     * fromCallable -> 동기적인 객체를 반환할 때 사용
     * defer -> Mono를 반환하고 싶을 때 사용
     */
    @Test
    public void startMonoFromFunction() {
        Mono<String> monoFromCallable = Mono.fromCallable(() -> {
            //우리 로직을 실행하고
            //동기적인 객체를 반환
            return callRestTemplate("안녕");
        }).subscribeOn(Schedulers.boundedElastic());
        /**
         * 임시 마이그레이션
         * restTemplate, JPA >> 블로킹이 발생한느 라이브러리 Mono 스레드 분리시켜 처리
         */


        /**
         * Mono 객체를 Mono 객체로 반환하고 있음
         */

        Mono<String> monoFromDefer = Mono.defer(() -> {
//            return Mono.just("안녕");
            // monoFromDefer.subscribe(); 실행이후에 코드 접근
            return callWebClient("안녕");
        });
        monoFromDefer.subscribe();  //Mono.just는 구독을 해야 Mono.just가 만들어짐
        Mono<String> monoFromJust = Mono.just("안녕");
    }

    @Test
    public void testDeferNecessity() {
        //a,b,c 만드는 로직도 Mono의 흐름 안에서 관리하고 싶다.
        Mono<String> stringMono = Mono.defer(() -> {
            String a = "안녕";
            String b = "하세"; //blocking 발생
            String c = "요";
            return callWebClient(a + b + c);
        }).subscribeOn(Schedulers.boundedElastic());
        //로직중에서 블로킹 발생시 스레드를 할당해서 논블로킹으로 만듬
//        String a = "안녕";
//        String b = "하세";
//        String c = "요";
//        Mono<String> stringMono = callWebClient(a + b + c);
    }

    public Mono<String> callWebClient(String request) {
        return Mono.just(request + "callWebClient");
    }

    public String callRestTemplate(String request) {
        return request + "callRestTemplate 응답";
    }


    /**
     * Mono의 흐름 시작 방법
     * 1. 데이터로부터 시작 -> 일반적인 경우 just / 특이한 상황 empty (Optional.empty())
     * 2. 함수로부터 시작
     * -> 동기적인 객체를 Mono로 반환하고 싶을때 fromCallable / 코드의 흐름을 Mono에서 관리하면서 Mono를 반환하고 싶을때 defer
     */


    @Test
    public void testBasicFluxMono() {
        //데이터로부터 시작 -> 대표적 just
        Flux.<Integer>just(1, 2, 3, 4, 5) //첫번쨰는 빈 함수로부터, 두번째는 데이터로부터 시작할 수 있음
                .map(data -> data * 2)
                .filter(data -> data % 4 == 0)
                .subscribe(data -> System.out.println("Flux가 구독한 data! = " + data));
        //1. just 데이터로부터 흐름을 시작
        //2. map과 filter같은 연산자로 데이터를 가공.subscribe(data -> System.out.println("Flux가 구독한 data! = "+data));
        //3. subscribe하면서 데이터를 방출

        //Mono 0개부터 1개의 데이터만 방출할 수 있는 객체 -> Optional 정도
        //Flux 0개 이상의 데이터를 방출할 수 있는 객체 -> List, Stream 0개 이상의 데이터 방출
        Mono.<Integer>just(2) //첫번쨰는 빈 함수로부터, 두번째는 데이터로부터 시작할 수 있음
                .map(data -> data * 2)
                .filter(data -> data % 4 == 0)
                .subscribe(data -> System.out.println("Mono가 구독한 data! = " + data));
        //왜 Mono가 필요하지? -> 1개의 데이터를 가공할떄는 Mono가 더 편함
    }
    //흐름 시작/ 데이터 가공/ 구독

    //Mono에서 데이터 방출이 개수가 많아져서 Flux를 바꾸고 싶다 -> flatMapMany
    @Test
    public void testMonoToFlux() {
        Mono<Integer> one = Mono.just(1);
        Flux<Integer> integerFlux = one.flatMapMany(data -> {
            return Flux.just(data, data + 1, data + 2);
        });
        integerFlux.subscribe(data -> System.out.println("data = " + data));
    }

    //flatMap, collectlist <<다음 시간!!
}
