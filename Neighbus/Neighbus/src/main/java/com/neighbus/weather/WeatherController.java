package com.neighbus.weather;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller // RestController 아님
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/weather")
    public String getWeather(@RequestParam(value = "city", required = false) String city, Model model) {
        if (city != null && !city.isEmpty()) {
            // 날씨 정보를 가져와서 'data'라는 이름으로 화면에 전달
            model.addAttribute("data", weatherService.getWeatherDTO(city));
        }
        return "weather/weather"; // templates/weather.html 파일을 찾아감
    }
    @GetMapping("/weather/current")
    @ResponseBody // HTML이 아니라 데이터(JSON)만 돌려줌
    public WeatherDTO getCurrentWeather(@RequestParam("lat") double lat, 
                                        @RequestParam("lon") double lon) {
        return weatherService.getWeatherByLatLon(lat, lon);
    }
}
