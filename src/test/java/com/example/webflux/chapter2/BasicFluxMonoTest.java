package com.example.webflux.chapter2;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class BasicFluxMonoTest {
    /**
     * Flux와 Mono 어떻게 사용해야 할까?
     * 1. Flux, Mono는 크게 데이터 흐름의 시작, 데이터 가공, 구독의 흐름으로 이루어진다.
     * 2. Flux는 0개 이상의 무한정 데이터를 방출한다.
     * 3. Mono는 0개, 1개의 데이터만 방출한다.
     */

    @Test
    public void testBasicFluxMono() {
        //데이터로부터 시작 -> 대표적 just
        Flux.<Integer>just(1,2,3,4,5) //첫번쨰는 빈 함수로부터, 두번째는 데이터로부터 시작할 수 있음
                .map(data -> data * 2)
                .filter(data -> data % 4 == 0)
                .subscribe(data -> System.out.println("Flux가 구독한 data! = "+data));
        //1. just 데이터로부터 흐름을 시작
        //2. map과 filter같은 연산자로 데이터를 가공.subscribe(data -> System.out.println("Flux가 구독한 data! = "+data));
        //3. subscribe하면서 데이터를 방출

        //Mono 0개부터 1개의 데이터만 방출할 수 있는 객체 -> Optional 정도
        //Flux 0개 이상의 데이터를 방출할 수 있는 객체 -> List, Stream 0개 이상의 데이터 방출
        Mono.<Integer>just(2) //첫번쨰는 빈 함수로부터, 두번째는 데이터로부터 시작할 수 있음
                .map(data -> data * 2)
                .filter(data -> data % 4 == 0)
                .subscribe(data -> System.out.println("Mono가 구독한 data! = "+data));
        //왜 Mono가 필요하지? -> 1개의 데이터를 가공할떄는 Mono가 더 편함
    }

    @Test
    public void testFluxMonoBlock(){
        Mono<String> justString = Mono.just("String");
        //Mono<String> 구독을 하고 안에 데어터가 완성이 되고 방출이되어야 완성
        //지금은 비동기 객체(언제 만들어질지 모르는 상태)
        //String문자열 뽑음 -> 문자열이 완성될떄까지 대기 ->이벤트루프도 블록되므로 왠만하면 사용 X
        String block = justString.block();
        System.out.println("block = " + block);
    }
}
