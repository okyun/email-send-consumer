package org.example.emailsendconsumer;

import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;

@Service
public class EmailSendConsumer {
    //email.send 프로듀서 service에서 언급되어있음.
    @KafkaListener(
            topics = "email.send",
            groupId = "email-send-group" // 컨슈머 그룹 이름(없으면 새로 컨슈머 그룹 만듬)
    )
    // Spring Boot 3 + Spring Kafka 3: backoff = @Backoff(...)
    // Spring Boot 4 + Spring Kafka 4: backOff = @BackOff(...) — 클래스명 BackOff, 속성명 backOff
    @RetryableTopic(
            // 총 시도 횟수 (최초 시도 1회 + 재시도 4회)
            attempts = "5",
            // 재시도 간격 (1000ms -> 2000ms -> 4000ms -> 8000ms 순으로 재시도 시간이 증가한다.)
            backOff = @BackOff(delay = 1000, multiplier = 2),

            // DLT 토픽 이름에 붙일 접미사
            dltTopicSuffix = ".dlt"
    )
    public void consume(String message) {
        System.out.println("Kafka로부터 받아온 메시지: " + message);

        EmailSendMessage emailSendMessage = EmailSendMessage.fromJson(message);

        // 잘못된 이메일 주소일 경우 실패 가정
        if (emailSendMessage.getTo().equals("fail@naver.com")) {
            System.out.println("잘못된 이메일 주소로 인해 발송 실패");
            throw new RuntimeException("잘못된 이메일 주소로 인해 발송 실패");
        }
        // ... 실제 이메일 발송 로직은 생략 ...
        try {
            Thread.sleep(3000); // 이메일 발송을 하는 데 3초가 걸린다고 가정!!
        } catch (InterruptedException e) {
            throw new RuntimeException("이메일 발송 실패");
        }

        System.out.println("3초 후 이메일 발송 완료");
    }

    /**
     * DLT(최종 실패) 메시지 전용. 여기서 예외를 던지면 복구기가 같은 DLT로 또 보내려 해
     * 동일 레코드가 DLT에 반복 적재되는 현상이 난다. 로그/DB 저장만 하고 정상 종료한다.
     */
    @DltHandler
    public void handleDlt(String message) {
        System.err.println("[DLT] 재시도 소진 후 최종 실패 (추가 DLT 전송 없음): " + message);
    }
}
