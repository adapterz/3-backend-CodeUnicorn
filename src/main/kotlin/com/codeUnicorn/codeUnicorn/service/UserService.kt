package com.codeUnicorn.codeUnicorn.service

import com.codeUnicorn.codeUnicorn.constant.PLATFORM_TYPE
import com.codeUnicorn.codeUnicorn.domain.user.User
import com.codeUnicorn.codeUnicorn.domain.user.UserRepository
import com.codeUnicorn.codeUnicorn.dto.CreateUserDto
import com.codeUnicorn.codeUnicorn.dto.RequestUserDto
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import javax.transaction.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
) {

    @Transactional // 트랜잭션 => 실패 => 롤백!
    fun login(
        requestUserDto: RequestUserDto,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): MutableMap<String, Any> {
        var platformType: String = "";
        val returnData: MutableMap<String, Any> = mutableMapOf()

        println("이메일 검사 통과")

        // 이메일로 사용자 존재 여부 파악
        val userInfoInDb: User? = userRepository.findByEmail(requestUserDto.email)
        println("userInfoInDb: $userInfoInDb")

        var user: User

        // 회원가입 처리
        if (userInfoInDb == null) {
            // request body의 email 값에 따라 platformType 결정
            if (requestUserDto.email.contains("naver")) {
                println("네이버 계정")
                platformType = PLATFORM_TYPE.NAVER.toString()
            } else if (requestUserDto.email.contains("gmail")) {
                println("구글 계정")
                platformType = PLATFORM_TYPE.GOOGLE.toString()
            }

            // 회원가입 사용자의 브라우저 정보 및 IP 주소 정보 수집
            val browserName: String = this.getBrowserInfo(request)
            println("회원가입 사용자의 브라우저 정보: $browserName")
            val ip: String? = this.getClientIp(request)
            println("회원가입 사용자의 IP 정보: $ip")

            // UserDto --> 엔티티로 변환
            val newUserDto: CreateUserDto =
                CreateUserDto(requestUserDto.email, requestUserDto.nickname, platformType, ip, browserName)
            user = newUserDto.toEntity()
            println("이 사용자는 회원가입이 필요한 사용자입니다.")
            userRepository.save(user); // 회원 정보 DB에 저장
            println("DB에 사용자 정보 저장 결과: $user")

            returnData["type"] = "회원가입"
            returnData["user"] = user
            // 로그인 처리
        } else {
            println("이 사용자는 바로 로그인 처리하면 됩니다.")

            val (email, nickname) = requestUserDto

            // DB에 저장된 닉네임과 일치여부 확인 후 일치하지 않으면 닉네임 업데이트
            if (userInfoInDb.nickname != nickname) {
                // 닉네임 업데이트
                val updatedUserId: Int? = userRepository.updateNickname(email, nickname)
                userInfoInDb.nickname = nickname
                println("userInfoInDb: $userInfoInDb")
            }

            // 로그인 사용자의 브라우저 정보 및 IP 주소 정보 수집
            val browserName: String = this.getBrowserInfo(request)
            println("로그인 사용자의 브라우저 정보: $browserName")
            val ip: String? = this.getClientIp(request) // IPv4 형식의 주소
            println("로그인 사용자의 IP 정보: $ip")

            // TODO 로그인 로그 쌓기

            user = userInfoInDb

            returnData["type"] = "로그인"
            returnData["user"] = user
        }

        // 세션 발급
        // 세션이 있으면 있는 세션 반환, 없으면 신규 세션 생성
        val session: HttpSession = request.getSession(false)
        // 세션에 로그인 회원 정보 보관
        session.setAttribute("user", user)
        println("발급된 세션 정보에 저장된 사용자 정보 : ${session.getAttribute("user")}")

        return returnData;
    }

    // 사용자 정보 조회
    fun getUserInfo(userId: Int?): User? {
        return userRepository.findByIdOrNull(userId)
    }

    // 클라이언트 브라우저 정보 가져오기
    fun getBrowserInfo(request: HttpServletRequest): String {
        val userAgent: String = request.getHeader("User-Agent")

        if (userAgent.indexOf("Trident") > -1) {
            return "ie"
        } else if (userAgent.indexOf("Edge") > -1) {
            return "edge"
        } else if (userAgent.indexOf("Whale") > -1) {
            return "whale"
        } else if (userAgent.indexOf("Opera") > -1 || userAgent.indexOf("OPR") > -1) {
            return "opera"
        } else if (userAgent.indexOf("Firefox") > -1) {
            return "firefox"
        } else if (userAgent.indexOf("Safari") > -1 && userAgent.indexOf("Chrome") == -1) {
            return "safari"
        } else if (userAgent.indexOf("Chrome") > -1) {
            return "chrome"
        }
        return ""
    }

    // 클라이언트 IP 정보 가져오기
    fun getClientIp(request: HttpServletRequest): String? {
        var ip: String? = request.getHeader("X-FORWARDED-FOR")
        if (ip == null) {
            ip = request.getHeader("Proxy-Client-IP")
            println(">>>> Proxy-Client-IP : $ip")
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_CLIENT_IP")
            println(">>>> HTTP_CLIENT_IP : $ip")
        }
        if (ip == null) {
            ip = request.remoteAddr
        }
        println(">>>> Result : IP Address : $ip")
        return ip
    }

    fun getSession(request: HttpServletRequest): User? {
        val session: HttpSession = request.getSession(false) ?: return null

        return session.getAttribute("user") as User? ?: return null
    }
}
