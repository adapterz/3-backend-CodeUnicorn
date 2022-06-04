package com.codeUnicorn.codeUnicorn.service

import com.codeUnicorn.codeUnicorn.domain.user.User
import com.codeUnicorn.codeUnicorn.domain.user.UserRepository
import com.codeUnicorn.codeUnicorn.dto.RequestUserDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import javax.transaction.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class UserServiceTest {

    @Autowired // DI, 생성 객체를 가져와 연결
    private lateinit var userRepository: UserRepository

    @Autowired // DI, 생성 객체를 가져와 연결
    private lateinit var restTemplate: TestRestTemplate

    @LocalServerPort // DI, 생성 객체를 가져와 연결
    private var port: Int = 8080

    @Test
    @Transactional // 트랜잭션 => 실패 => 롤백!
    fun 사용자_회원가입_성공() {
        val email = "pbc9236@gmail.com"
        val nickname = "론이다"

        val requestDto: RequestUserDto = RequestUserDto(email, nickname)

        val url = "http://localhost:$port/users/login"

        // when
        val responseEntity: ResponseEntity<Long> = restTemplate.postForEntity(url, requestDto, Long::class.java)

        // then
        // 상태 코드 검증
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.OK)
        // 응답 바디 데이터 검증
        assertThat(responseEntity.body).isGreaterThan(0)

        // DB에 저장된 후 요청으로 들어온 사용자 정보 데이터와 일치 여부 비교
        val all: List<User> = userRepository.findAll()
        assertThat(all[0].platform_type).isEqualTo("google")
        assertThat(all[0].email).isEqualTo(email)
        assertThat(all[0].nickname).isEqualTo(nickname)
    }
}
