package com.example.webflux.chapter2;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class SchedulerTest {

    /*
        Scheduler
        Flux와 Mono에서 사용하는 스레드를 관리
        - BoundedElastic
           생성 방법 : 유저가 스레드를 요청할 때마다 탄력적으로 생성해서 할당, 이미 만들어둔 스레드가 있으면 해당 스레드를 할당
                     하지만 생성 가능 스레드 제한이 있다. 스레드 제한 이 걸리면 스레드를 할당 받지 못한 작업은 큐에서 대기한다.
           생명 주기 : 한 번 만들어지면 일정 시간 동안 스레들 삭제하지 않고 유지, 일정 시간 동안 사용되지 않으면 삭제됨.
           용도 : 블로킹 작업 처리에 사용된다.
        - Parallel
           생성 방법 : 유저가 한번 호출하면 물리스레드와 같은 양의 스레드르 생ㅅ성해두고 삭제하지 않는다.
           생명 주기: 처음에 한번 생성되고 나면 계속 유지 된다.
           용도 : CPU 작업을 병렬로 처리할 때 사용된다.
     */

    /*
    subscribe
    publish
     */

    @Test
    public void testBasicFluxMono() {
        Mono.<Integer>just(2)
                .map(data -> {
                    //boundedElastic
                    System.out.println("map Thread Name = " + Thread.currentThread().getName());
                    return data * 2;
                })
                .publishOn(Schedulers.parallel())
                //parallel
                .filter(data -> {
                    System.out.println("filter Thread Name = " + Thread.currentThread().getName());
                    return data % 4 == 0;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(data -> System.out.println("Mono가 구독한 data! = " + data));
    }
}
