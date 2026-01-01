package com.neighbus.util;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * ★ 비밀번호 암호화 도구 ★
 *
 * 이 클래스를 실행하면 입력한 비밀번호를 암호화된 형식으로 변환해줍니다.
 * DB에 저장할 때 사용하세요.
 *
 * 실행 방법:
 * ./gradlew run --args="원하는비밀번호"
 */
public class PasswordEncoderUtil {

    public static void main(String[] args) {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        if (args.length == 0) {
            System.out.println("========================================");
            System.out.println("비밀번호를 입력해주세요!");
            System.out.println("사용법: ./gradlew run --args=\"원하는비밀번호\"");
            System.out.println("========================================");

            // 예제로 몇 가지 비밀번호 암호화
            String[] examplePasswords = {"admin", "admin123!", "password"};

            System.out.println("\n예제 비밀번호 암호화 결과:\n");

            for (String password : examplePasswords) {
                String encoded = encoder.encode(password);
                System.out.println("원본 비밀번호: " + password);
                System.out.println("암호화된 비밀번호: " + encoded);
                System.out.println("\nSQL 예시:");
                System.out.println("UPDATE users SET password = '" + encoded + "' WHERE username = 'admin';");
                System.out.println("----------------------------------------\n");
            }

            return;
        }

        String password = args[0];
        String encodedPassword = encoder.encode(password);

        System.out.println("\n========================================");
        System.out.println("비밀번호 암호화 완료!");
        System.out.println("========================================");
        System.out.println("원본 비밀번호: " + password);
        System.out.println("암호화된 비밀번호:\n" + encodedPassword);
        System.out.println("\n========================================");
        System.out.println("아래 SQL을 복사해서 MySQL에서 실행하세요:");
        System.out.println("========================================");
        System.out.println("\nUPDATE users");
        System.out.println("SET password = '" + encodedPassword + "',");
        System.out.println("    grade = 0");
        System.out.println("WHERE username = 'admin';");
        System.out.println("\n========================================\n");
    }
}
