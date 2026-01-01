package com.neighbus.weather;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class WeatherService {
	@Value("${weather.api.key}")
	private String apiKey; // 설정 파일에서 키를 가져옴

	private final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

	public WeatherDTO getWeatherDTO(String city) {
		RestTemplate restTemplate = new RestTemplate();
		String url = UriComponentsBuilder.fromHttpUrl(BASE_URL).queryParam("q", city).queryParam("appid", apiKey)
				.queryParam("units", "metric").queryParam("lang", "kr").toUriString();

		// 1. 전체 데이터를 Map으로 받아옴
		Map<String, Object> response = restTemplate.getForObject(url, Map.class);

		// 2. 필요한 데이터만 쏙쏙 뽑기 (파싱)
		Map<String, Object> main = (Map<String, Object>) response.get("main");
		List<Map<String, Object>> weatherList = (List<Map<String, Object>>) response.get("weather");
		Map<String, Object> weather = weatherList.get(0);

		// 3. DTO에 담아서 반환
		return new WeatherDTO((String) response.get("name"), ((Number) main.get("temp")).doubleValue(), // 숫자 형변환 안전하게
																										// 처리
				(String) weather.get("description"), (int) main.get("humidity"), (String) weather.get("icon"));
	}

	public WeatherDTO getWeatherByLatLon(double lat, double lon) {
		RestTemplate restTemplate = new RestTemplate();

		// 좌표(lat, lon)를 사용하는 URL 생성
		String url = UriComponentsBuilder.fromHttpUrl(BASE_URL).queryParam("lat", lat).queryParam("lon", lon)
				.queryParam("appid", apiKey).queryParam("units", "metric").queryParam("lang", "kr").toUriString();

		// 데이터 요청 및 파싱 (기존 로직과 동일)
		Map<String, Object> response = restTemplate.getForObject(url, Map.class);
		Map<String, Object> main = (Map<String, Object>) response.get("main");
		List<Map<String, Object>> weatherList = (List<Map<String, Object>>) response.get("weather");
		Map<String, Object> weather = weatherList.get(0);

		return new WeatherDTO((String) response.get("name"), // 지역 이름
				((Number) main.get("temp")).doubleValue(), (String) weather.get("description"),
				(int) main.get("humidity"), (String) weather.get("icon"));
	}

}
