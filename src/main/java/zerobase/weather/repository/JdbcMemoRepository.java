package zerobase.weather.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import zerobase.weather.domain.Memo;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcMemoRepository { // jdbc 형식이고, memo 테이블을 연동
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public JdbcMemoRepository(DataSource dataSource){ // DataSource : application.properties 에 작성된 정보들이 (spring.datasource..) 담긴 객체
		jdbcTemplate = new JdbcTemplate(dataSource); //
	}

	public Memo save(Memo memo){ // 클래스의 값을 저장하면, mysql 에 memo 값이 저장됨
		String sql = "insert into memo values (?,?)";
		jdbcTemplate.update(sql, memo.getId(), memo.getText());
		return memo;
	}

	public List<Memo> findAll() {
		String sql = "select * from memo";
		return jdbcTemplate.query(sql, memoRowMapper());
	}

	public Optional<Memo> findById(int id){
		String sql = "select * from memo where id = ?";
		return jdbcTemplate.query(sql, memoRowMapper(), id).stream().findFirst();
	}

	private RowMapper<Memo> memoRowMapper() {
		// jdbc 를 통해 mysql 에 가져온 데이터 값은 ResultSet 객체로 반환됨
		// ResultSet 은 {id = 1, text ='this is memo'} 와 같은 형식으로 되어있음
		// 이 객체를 Memo 클래스 형식으로 매핑 시켜야 함
		return (rs, rowNum) -> new Memo(
				rs.getInt("id"),
				rs.getString("text")
		);
	}

}
