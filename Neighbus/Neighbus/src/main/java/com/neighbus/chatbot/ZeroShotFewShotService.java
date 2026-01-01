package com.neighbus.chatbot;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ZeroShotFewShotService {

    private final ChatClient chatClient;

    public ZeroShotFewShotService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 제로샷 프롬프트 (Zero-Shot)
     * - 오직 명령어로 일반화 능력 평가
     */
    public String runZeroShot(String inputText) {
        System.out.println("--- 1. 제로샷 실행 ---");
        return chatClient.prompt()
                    .user(inputText)
                    .call()
                    .content();
    }

    /**
     * 퓨샷 프롬프트 (Few-Shot)
     * - 예제 다수 제공하여 모델 학습 유도
     */
    public String runFewShot(String inputText) {
        System.out.println("--- 2. 퓨샷 실행 ---");
        String userPrompt ="""
                                   질문에 대한 답은 다음과 같이 과정과 답으로 나눠서 작성해주세요. 
                                   10개의 공이 있었는데 3개를 가져가고 2개를 돌려받았다. 몇 개인가? 
                                   과정: (10-3)+2=9
                                   답: 9개.
                                   
                                   질문내용 : %s""".formatted(inputText);

        return chatClient.prompt()
                .user(userPrompt)
                .call()
                .content();

    }
}