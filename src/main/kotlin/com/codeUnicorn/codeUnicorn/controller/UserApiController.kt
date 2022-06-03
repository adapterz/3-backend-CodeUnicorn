package com.codeUnicorn.codeUnicorn.controller

import com.codeUnicorn.codeUnicorn.domain.user.ErrorResponse
import com.codeUnicorn.codeUnicorn.domain.user.SuccessResponse
import com.codeUnicorn.codeUnicorn.domain.user.User
import com.codeUnicorn.codeUnicorn.dto.RequestUserDto
import com.codeUnicorn.codeUnicorn.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import javax.validation.Valid

@RestController
@RequestMapping("/users")
@Validated
class UserApiController(private val userService: UserService) {
    // 사용자 로그인 API
    @PostMapping(path = ["/login"])
    fun login(
        // Request Body 데이터 유효성 검증
        @Valid @RequestBody requestUserDto: RequestUserDto,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<SuccessResponse?> {
        // 프론트 서버로부터 받아온 사용자 정보(request.body 에 해당)
        println("requestUserDto: $requestUserDto")

        // 각각 회원가입 || 로그인, 사용자 데이터 리턴
        val result: MutableMap<String, Any> = userService.login(requestUserDto, request, response)

        println("타입: ${result["type"]}")
        println("사용자 정보: ${result["user"]}")

        if (result["type"] == "회원가입") {
            val successResponse = SuccessResponse(201, result["user"])
            return ResponseEntity.status(HttpStatus.CREATED).body(successResponse)
        } else if (result["type"] == "로그인") {
            val successResponse = SuccessResponse(200, result["user"])
            return ResponseEntity.status(HttpStatus.OK).body(successResponse)
        }

        return ResponseEntity.status(HttpStatus.OK).body(null)
    }

    // 사용자 로그아웃 API
    @DeleteMapping(path = ["/logout"])
    fun logout(request: HttpServletRequest): ResponseEntity<Void> {
        println("로그아웃 전")
        val httpSession: HttpSession = request.getSession()
        httpSession.setAttribute("user", null)
        httpSession.invalidate()
        println("로그아웃 후")
        return ResponseEntity.noContent().build()
    }

    // 세션 조회
    @GetMapping(path = ["/session"])
    fun sessionCheck(request: HttpServletRequest): ResponseEntity<Any> {
        val user: User? = userService.getSession(request)

        // 세션 정보 존재하지 않은 경우 예외 처리
        if (user == null) {
            val errorResponse = ErrorResponse().apply {
                this.status = HttpStatus.NOT_FOUND.value().toString()
                this.method = request.method
                this.message = "세션 정보가 존재하지 않습니다."
                this.path = request.requestURI.toString()
                this.timestamp = LocalDateTime.now()
                this.errors = null
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
        }

        // 세션 정보 존재하는 경우 현재 로그인한 사용자 정보 제공
        println("user: $user")
        return ResponseEntity.ok(user)
    }
}
