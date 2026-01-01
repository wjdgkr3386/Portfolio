package com.neighbus.chatbot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class ChatService {

    private final ChatClient chatClient;

    @Value("classpath:prompt.txt")
    private Resource promptResource;

    public ChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String syncChat(String userMessage) {
        String systemPrompt = "";
        try {
            systemPrompt = promptResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String s = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .call()
                    .content();

        return s;
    }

    public Flux<String> streamChat(String userMessage) {
        try {
            String systemPrompt = promptResource.getContentAsString(StandardCharsets.UTF_8);

            Flux<String> f = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .stream()
                    .content();

            return f;

        } catch (IOException e) {
            return Flux.error(e);
        }
    }
}

