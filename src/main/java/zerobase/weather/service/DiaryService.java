package zerobase.weather.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.domain.DateWeather;
import zerobase.weather.domain.Diary;
import zerobase.weather.repository.DateWeatherRepository;
import zerobase.weather.repository.DiaryRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiaryService {

	@Value("${openweathermap.key}") // application.properties 에 설정된 값 가져옴
	private String apikey;

	private final DiaryRepository diaryRepository;
	private final DateWeatherRepository dateWeatherRepository;

	public DiaryService (DiaryRepository diaryRepository, DateWeatherRepository dateWeatherRepository) {
		this.diaryRepository = diaryRepository;
		this.dateWeatherRepository = dateWeatherRepository;
	}

	@Transactional
	@Scheduled(cron = "0 0 1 * * *")
	public void saveWeatherDate() {
		dateWeatherRepository.save(getWeatherFromApi());
	}

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public void createDiary(LocalDate date, String text) {
		// 날씨 데이터 가져오기 (API 에서 가져오기 or DB 에서 기존 값 가져오기)
		DateWeather dateWeather = getDateWeather(date);

		// 우리 db에 저장하기
		Diary nowDiary = new Diary();
		nowDiary.setDateWeather(dateWeather);
		nowDiary.setText(text);
		diaryRepository.save(nowDiary);

	}

	private DateWeather getWeatherFromApi() {
		// open weather map 에서 데이터 받아오기
		String weatherData = getWeatherString();
		// 받아온 날씨 데이터 json 파싱하기
		Map<String, Object> parsedWeather = parseWeather(weatherData);
		DateWeather dateWeather = new DateWeather();
		dateWeather.setDate(LocalDate.now());
		dateWeather.setWeather(parsedWeather.get("main").toString());
		dateWeather.setIcon(parsedWeather.get("icon").toString());
		dateWeather.setTemperature((Double) parsedWeather.get("temp"));

		return dateWeather;
	}

	private DateWeather getDateWeather(LocalDate localDate) {
		List<DateWeather> dateWeatherListFromDB = dateWeatherRepository.findAllByDate(localDate);
		if (dateWeatherListFromDB.size() == 0) {
			// 새로 api 에서 날씨 정보를 가져와야 한다.
			// 프로젝트 정책 세우기
				// 현재 날씨를 가져오도록 하거나
				// 날씨없이 일기를 쓰도록
			return getWeatherFromApi();
		 } else {
			return dateWeatherListFromDB.get(0);
		}
	}

	@Transactional(readOnly = true)
	public List<Diary> readDiary(LocalDate date) {
		return diaryRepository.findAllByDate(date);
	}

	public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
		return diaryRepository.findAllByDateBetween(startDate, endDate);
	}

	public void updateDiary(LocalDate date, String text) {
		Diary nowDiary = diaryRepository.getFirstByDate(date);
		nowDiary.setText(text);
		diaryRepository.save(nowDiary);
	}

	public void deleteDiary(LocalDate date) {
		diaryRepository.deleteAllByDate(date);
	}

	private String getWeatherString() {
		String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid=" + apikey;

		try {
			URL url = new URL(apiUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			BufferedReader br;
			int responseCode = connection.getResponseCode();
			if (responseCode == 200) {
				br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			} else {
				br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
			}
			String inputLine;
			StringBuilder response = new StringBuilder();
			while((inputLine = br.readLine()) != null){
				response.append(inputLine);
   			}
			br.close();

			return response.toString();
		} catch (Exception e) {
			return "failed to get response";
		}
	}

	private Map<String, Object> parseWeather(String jsonString){
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject;

		try{
			jsonObject = (JSONObject) jsonParser.parse(jsonString);
	 	 }catch (ParseException e){
			throw new RuntimeException();
	 	 }

		Map<String, Object> resultMap = new HashMap<>();

		JSONObject mainData = (JSONObject) jsonObject.get("main");
		resultMap.put("temp", mainData.get("temp"));
		JSONArray weatherArray = (JSONArray) jsonObject.get("weather");
		JSONObject weatherData = (JSONObject) weatherArray.get(0);
		resultMap.put("main", weatherData.get("main"));
		resultMap.put("icon", weatherData.get("icon"));

		return resultMap;
	}

}
