package com.example.webflux;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

@SpringBootTest
public class FunctionalProgrammingTest {

    //1~9까지 출력하는 api
    @Test
    public void produceOneToNine() {
        List<Integer> sink = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            sink.add(i);
        }

        //*를 전부 해주고 싶다
//        sink = map(sink);
        sink = map1(sink, data -> data * 4);

        //4의 배수들만 남겨두고 싶다.
//        sink = filter(sink);
        sink = filter1(sink, data -> data % 4 == 0);

//        forEach(sink);
        forEach1(sink, data -> System.out.println(data));
    }

    @Test
    public void produceOneToNineStream() {
        IntStream.rangeClosed(1, 9).boxed()
                .map(data -> data * 4)
                .filter(data -> data % 4 == 0)
                .forEach(data -> System.out.println(data)); //collect, foreach, min, max
//        List<Integer> sink = new ArrayList<>();
//        for (int i = 1; i < 10; i++) {
//            sink.add(i);
//        }
//        sink.stream()
//                .map(data -> data * 4)
//                .filter(data -> data % 4 == 0)
//                .forEach(data -> System.out.println(data));
    }

    @Test
    public void produceOneToNineFlux() {
        //sleep도안되고 종료 ->  블로킹도 안됨 -> 코드 함수 덩어리임 -> 실행을 시켜주어야함 -> Stream의 종결문처럼 구독을 해줘야함
        //Controller는 내부적으로 구독을 해서 네티에게 전달
        Flux<Integer> intFlux = Flux.create(sink -> {
            for (int i = 1; i < 10; i++) {
                sink.next(i);
            } //총 4.5초 소요 -> 요건사항 : 데이터가 처리 될때마다 바로 보여주세요
            sink.complete();//사용후 닫아줌
        });

        intFlux.subscribe(data -> System.out.println("WebFlux가 구독중 : " + data));
        System.out.println("Netty 이벤트 루프로 스레드 복귀 !!");
    }

    @Test
    public void produceOneToNineFluxOperator() {
        Flux.fromIterable(IntStream.rangeClosed(1, 9).boxed().toList())
                .map(data -> data * 4) //operator 대부분이 stream과 유사하게 동작
                .filter(data -> data % 4 == 0)
                .subscribe(data -> System.out.println(data)); //collect, foreach, min, max
    }

    private List<Integer> map1(List<Integer> sink, Function<Integer, Integer> mapper) {
        List<Integer> newSink1 = new ArrayList<>();
        for (int i = 0; i <= 8; i++) {
            newSink1.add(mapper.apply(sink.get(i)));
        }
        sink = newSink1;
        return sink;
    }

    private void forEach(List<Integer> sink) {
        for (int i = 0; i < sink.size(); i++) {
            System.out.println(sink.get(i));
        }
    }

    private void forEach1(List<Integer> sink, Consumer<Integer> consumer) {
        for (int i = 0; i < sink.size(); i++) {
            consumer.accept(sink.get(i));
        }
    }


    private List<Integer> filter(List<Integer> sink) {
        List<Integer> newSink2 = new ArrayList<>();
        for (int i = 0; i <= 8; i++) {
            if (sink.get(i) % 4 == 0) { //4의 배수일 때만 리스트 추가
                newSink2.add(sink.get(i));
            }
        }
        sink = newSink2;
        return sink;
    }

    private List<Integer> filter1(List<Integer> sink, Function<Integer, Boolean> predicate) {
        List<Integer> newSink2 = new ArrayList<>();
        for (int i = 0; i <= 8; i++) {
            if (predicate.apply(sink.get(i))) { //4의 배수일 때만 리스트 추가
                newSink2.add(sink.get(i));
            }
        }
        sink = newSink2;
        return sink;
    }

    private List<Integer> map(List<Integer> sink) {
        List<Integer> newSink1 = new ArrayList<>();
        for (int i = 0; i <= 8; i++) {
            newSink1.add(sink.get(i) * 4);
        }
        sink = newSink1;
        return sink;
    }

}
