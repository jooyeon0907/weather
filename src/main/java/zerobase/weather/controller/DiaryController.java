package zerobase.weather.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import zerobase.weather.domain.Diary;
import zerobase.weather.service.DiaryService;

import java.time.LocalDate;
import java.util.List;

@RestController
public class DiaryController {
	private final DiaryService diaryService;

	public DiaryController(DiaryService diaryService) {
		this.diaryService = diaryService;
	}

	@Operation(summary = "일기 텍스트와 날씨를 이용해서 DB에 일기 저장", description = "이것은 노트")
	@PostMapping("/create/diary")
	void createDiary(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
					 @RequestBody String text) {
		diaryService.createDiary(date, text);
	}

	@Operation(summary = "선택한 날짜의 모든 일기 데이터를 가져옵니다")
	@GetMapping("/read/diary")
	List<Diary> readDiary(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return diaryService.readDiary(date);
	}

	@Operation(summary = "선택한 기간 중의 모든 일기 데이터를 가져옵니다")
	@GetMapping("/read/diaries")
	List<Diary> readDiaries(@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
									@Parameter(description = "조회할 기간의 첫번째날", example = "2024-12-20") LocalDate startDate,
							@RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
								@Parameter(description = "조회할 기간의 마지막날", example = "2024-12-21") LocalDate endDate){
		return diaryService.readDiaries(startDate, endDate);
	}

	@PutMapping("/update/diary")
	void updateDiary(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
					 @RequestBody String text){
		diaryService.updateDiary(date, text);
	}

	@DeleteMapping("/delete/diary")
	void deleteDiary(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		diaryService.deleteDiary(date);
	}
}
