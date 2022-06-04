package com.codeUnicorn.codeUnicorn.controller

import com.codeUnicorn.codeUnicorn.dto.RequestUserDto
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@AutoConfigureMockMvc
@SpringBootTest
class UserControllerApiTest {
    @set:Autowired
    lateinit var mockMvc: MockMvc // 가상의 요청을 만듬.

    @Test
    @DisplayName("만약 이메일, 닉네임 값이 \"\" 으로 들어온다면")
    fun loginFailTest1() {
        // 실패 케이스 1 : 이메일, 닉네임 값이 "" 으로 들어온 경우
        // given
        val userRequest = RequestUserDto("", "")

        val json = jacksonObjectMapper().writeValueAsString(userRequest)
        println(json)

        // when
        val performLogin = mockMvc.perform(
            MockMvcRequestBuilders.post("/users/login")
                .content(json)
                .contentType("application/json")
                .accept("application/json")
        )

        // then
        performLogin.andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("\$.status").value("400"))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.method").value("POST"))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.message").value("요청에 에러가 발생했습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.path").value("/users/login"))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.errors").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @DisplayName("만약 닉네임 값이 60자 이상이라면")
    @Test
    fun loginFailTest2() {
        // 실패 케이스 2 : 닉네임 값이 60자 이상인 경우
        // given
        val userRequest =
            RequestUserDto("gildong@naver.com", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")

        val json = jacksonObjectMapper().writeValueAsString(userRequest)
        println(json)

        // when
        val performLogin = mockMvc.perform(
            MockMvcRequestBuilders.post("/users/login")
                .content(json)
                .contentType("application/json")
                .accept("application/json")
        )

        // then
        performLogin
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("\$.status").value("400"))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.method").value("POST"))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.message").value("요청에 에러가 발생했습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.path").value("/users/login"))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.errors").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("\$.errors.message").value("닉네임은 1 ~ 60자 이어야 합니다."))
            .andDo(MockMvcResultHandlers.print())
    }

    @DisplayName("만약 이메일 값이 naver 혹은 gmail이 아니라면")
    @Test
    fun loginFailTest3() {
        // 실패 케이스 2 : 이메일 값이 naver 혹은 gmail 이 아닌 경우
        // given
        val userRequest =
            RequestUserDto("gildong@gildong.com", "홍길동")

        val json = jacksonObjectMapper().writeValueAsString(userRequest)
        println(json)

        // when
        val performLogin = mockMvc.perform(
            MockMvcRequestBuilders.post("/users/login")
                .content(json)
                .contentType("application/json")
                .accept("application/json")
        )
        // then
        performLogin
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("\$.status").value("400"))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.method").value("POST"))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.message").value("요청에 에러가 발생했습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.path").value("/users/login"))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.errors").isNotEmpty)
            .andExpect(
                MockMvcResultMatchers.jsonPath("\$.errors.message")
                    .value("이메일 주소는 반드시 @gmail.com 혹은 @naver.com 를 포함해야 합니다.")
            )
            .andDo(MockMvcResultHandlers.print())
    }

    @DisplayName("만약 이메일 값이 형식에 어긋난다면")
    @Test
    fun loginFailTest4() {
        // 실패 케이스 4 : 이메일 값이 형식에 어긋나는 경우
        // given
        val userRequest =
            RequestUserDto("gildong", "홍길동")

        val json = jacksonObjectMapper().writeValueAsString(userRequest)
        println(json)

        // when
        val performLogin =
            mockMvc.perform(
                MockMvcRequestBuilders.post("/users/login")
                    .content(json)
                    .contentType("application/json")
                    .accept("application/json")
            )
        // then
        performLogin
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("\$.status").value("400"))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.method").value("POST"))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.message").value("요청에 에러가 발생했습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.path").value("/users/login"))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.errors").isNotEmpty)
            .andExpect(
                MockMvcResultMatchers.jsonPath("\$.errors.message")
                    .value("이메일 형식에 어긋납니다.")
            )
            .andDo(MockMvcResultHandlers.print())
    }
}
