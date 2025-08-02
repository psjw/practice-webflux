package com.example.webflux.chapter2;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class FluxMonoErrorAndSignalTest {
    /*
        Signal과 에러처리
        데이터가 방출 될 때 -> onNext
        스트림이 완료 됬을 때 -> onComplete
        스트림에서 에러가 발생 했을 때 ->  onError
     */

    @Test
    public void testBasicSignal() {
        Flux.just(1, 2, 3, 4) //upstream
                .doOnNext(publishData -> System.out.println("publishData = " + publishData))
                .doOnComplete(() -> System.out.println("스트림이 끝났습니다."))
                .doOnError(ex -> {
                    System.out.println("ex 에러 상황 발생! = " + ex);
                })
                .subscribe(data -> System.out.println("data = " + data));
    }

    @Test
    public void testFluxMonoError() {
        //에러가 throw되지 않음 -> Reactive Stream안에서 발생한에러는 자체적으로 잡음
        try {
            Flux.just(1, 2, 3, 4)
                    .map(data -> {
//                        try{
                        //자바에서는 에러를 찾을 때 이코드를 실행중인 스레드의 호출스택에서 찾음
                        /*
                            호출 스택이란?
                            스레드에서 관리되는 메모리로서
                            스레드 함수가 호출될 때마다
                            함수에 대한 정보를 스택형태로 저장해 두고 있음
                            호출스택은 각스레드에서 독립적으로 관리
                         */
                        if (data == 3) {
                            throw new RuntimeException();
                        }
                        return data * 2;
//                        }catch (Exception e){
//                            System.out.println("에러시그널 발생시키");
//                        }
                    })
                    .subscribeOn(Schedulers.boundedElastic()) //외부스레드
                    .subscribe(data -> System.out.println("data = " + data));
        } catch (Exception e) {
            System.out.println("에러가 발생했어요!");
        }
    }


    @Test
    public void testFluxMonoError1() {
        //에러가 throw되지 않음 -> Reactive Stream안에서 발생한에러는 자체적으로 잡음
        try {
            Flux.just(1, 2, 3, 4)
                    .map(data -> {
                        //에러가 발생해도 결과물을 지속시키는 직관적인 방법
                        try {
                            if (data == 3) {
                                throw new RuntimeException();
                            }
                            return data * 2;
                        } catch (Exception e) {
                            //data.setError() 에러 필드를 세팅해서 클라이언트를 확인
                            return data * 999;
                        }
                    })
                    //.subscribeOn(Schedulers.boundedElastic()) //외부스레드
                    .onErrorMap(ex -> new IllegalArgumentException()) //에러를 다른 에러로 변환
                    .onErrorReturn(999) //에러 발생시 별도 데이터 전달
                    .onErrorComplete() //에러발생시 컴플리트 시그널을 전파
                    .subscribe(data -> System.out.println("data = " + data));
        } catch (Exception e) {
            System.out.println("에러가 발생했어요!");
        }
    }

    /*
        Flux.Mono.error()
     */

    @Test
    public void testFluxMonoDotError() {
        Flux.just(1, 2, 3, 4)
                .flatMap(data -> {
                    if (data != 3) {
                        return Mono.just(data);
                    } else {
                        return Mono.error(new RuntimeException()); //Throw 대신사용
//                        throw new RuntimeException();
                    }
                }).subscribe(data -> System.out.println("data = " + data));
    }
    /*
        시그널에 대하여
        Mono와 Flux에는
        [방출(onNext) / 완료(onComplete) / 에러(onError)] 시그널이 있고
        doOnNext / doOnComplete / doOnError로 포착이 가능

        에러처리에 대하여
        리액티브 스트림 안에서 발생하는 예외는 스트림 밖으로 던져지지 않는다.
        때문에 스트림 안에서 적절히 try -catch를 이용해서 처리하거나
        onError~류 오퍼레이터를 사용하여 처리해야한다.
     */
}
