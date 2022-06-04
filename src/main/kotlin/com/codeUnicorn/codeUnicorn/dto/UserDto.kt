package com.codeUnicorn.codeUnicorn.dto

import com.codeUnicorn.codeUnicorn.domain.user.User
import javax.validation.constraints.AssertTrue
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class RequestUserDto(
    @field: NotBlank(message = "이메일이 누락되었습니다.")
    @field: Email(message = "이메일 형식에 어긋납니다.")
    var email: String,
    @field: NotBlank(message = "닉네임이 누락되었습니다.")
    @field: Size(min = 1, max = 60, message = "닉네임은 1 ~ 60자 이어야 합니다.")
    var nickname: String,
) {
    // 이메일 주소가 구글 혹은 네이버인지 판단
    @AssertTrue(message = "이메일 주소는 반드시 @gmail.com 혹은 @naver.com 를 포함해야 합니다.")
    private fun isValidEmail(): Boolean { // 정상 true 비정상 false
        return "gmail" in this.email || "naver" in this.email
    }
}

data class CreateUserDto(
    private val email: String,
    private val nickname: String?, // 빈 값 허용
    private val platform_type: String,
    private val ip: String?,
    private val browser_type: String
) {
    fun toEntity(): User {
        return User(
            email = this.email,
            nickname = this.nickname,
            platform_type = this.platform_type,
            ip = this.ip,
            browser_type = this.browser_type
        )
    }
}
