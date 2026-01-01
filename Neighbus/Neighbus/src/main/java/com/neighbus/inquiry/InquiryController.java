package com.neighbus.inquiry;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InquiryController {

    // 문의하기 폼 페이지
    @GetMapping("/inquiry")
    public String inquiryForm() {
        return "inquiry/inquiry";
    }

    // 나의 문의 내역 페이지
    @GetMapping("/my-inquiries")
    public String myInquiries() {
        return "inquiry/my-inquiries";
    }
}
