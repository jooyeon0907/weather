package zerobase.weather.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.domain.Memo;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
	// 테스트로 인해 DB 안에 있는 데이터가 변경되지 않아야 하므로 해당 어노테이션을 사용하면
	// 테스트를 다 진행되고 난 후에 원래 상태로 복구를 하기 때문에 기존 DB에 영향을 주지 않음
class JdbcMemoRepositoryTest {

	@Autowired
	JdbcMemoRepository jdbcMemoRepository;

	@Test
	void insertMemoTest() {
		// given
		Memo newMemo = new Memo(2, "insertMemoTest");

		// when
		jdbcMemoRepository.save(newMemo);

		// then
		Optional<Memo> result = jdbcMemoRepository.findById(2);
		assertEquals(result.get().getText(), "insertMemoTest");
	}

	@Test
	void findAllMemoTest() {
		// given
		List<Memo> memoList = jdbcMemoRepository.findAll();
		System.out.println(memoList);
		assertNotNull(memoList);
	}


}