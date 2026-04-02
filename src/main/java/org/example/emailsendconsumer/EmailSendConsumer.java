package org.example.emailsendconsumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class EmailSendConsumer {
    //email.send 프로듀서 service에서 언급되어있음.
    @KafkaListener(
            topics = "email.send",
            groupId = "email-send-group" // 컨슈머 그룹 이름(없으면 새로 컨슈머 그룹 만듬)
    )
    public void consume(String message) {
        System.out.println("Kafka로부터 받아온 메시지: " + message);

        EmailSendMessage emailSendMessage = EmailSendMessage.fromJson(message);

        // 잘못된 이메일 주소일 경우 실패 가정
//        if (emailSendMessage.getTo().equals("fail@naver.com")) {
//            System.out.println("잘못된 이메일 주소로 인해 발송 실패");
//            throw new RuntimeException("잘못된 이메일 주소로 인해 발송 실패");
//        }
        // ... 실제 이메일 발송 로직은 생략 ...
        try {
            Thread.sleep(3000); // 이메일 발송을 하는 데 3초가 걸린다고 가정!!
        } catch (InterruptedException e) {
            throw new RuntimeException("이메일 발송 실패");
        }

        System.out.println("3초 후 이메일 발송 완료");
    }
}
