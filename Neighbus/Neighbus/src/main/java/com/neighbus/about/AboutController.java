package com.neighbus.about;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.neighbus.Util;

@Controller
@RequestMapping(value="/about")
public class AboutController {

    // application.properties에서 카카오맵 JavaScript 키를 주입받습니다.
    @Value("${google.maps.appkey}")
    private String googleMapsApiKey;
    
	@GetMapping(value={"/",""})
	public String aboutForm(Model model) {
		System.out.println("AboutController - aboutForm");

        model.addAttribute("googleMapsApiKey", googleMapsApiKey);

		return "about/About";
	}
}