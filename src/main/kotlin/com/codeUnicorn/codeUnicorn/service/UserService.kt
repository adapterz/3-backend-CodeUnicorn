package com.codeUnicorn.codeUnicorn.service

import com.codeUnicorn.codeUnicorn.constant.BEHAVIOR_TYPE
import com.codeUnicorn.codeUnicorn.constant.PLATFORM_TYPE
import com.codeUnicorn.codeUnicorn.domain.user.User
import com.codeUnicorn.codeUnicorn.domain.user.UserAccessLogRepository
import com.codeUnicorn.codeUnicorn.domain.user.UserRepository
import com.codeUnicorn.codeUnicorn.dto.CreateUserDto
import com.codeUnicorn.codeUnicorn.dto.RequestUserDto
import com.codeUnicorn.codeUnicorn.dto.UserAccessLogDto
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import javax.transaction.Transactional

@Service
class UserService @Autowired constructor(
    private val userRepository: UserRepository,
    private val userAccessLogRepository: UserAccessLogRepository
) {

    // 리턴 값 : { "type": "로그인" || "회원가입", "data": { 사용자 정보 } }
    @Transactional // 트랜잭션 => 실패 => 롤백!
    fun login(
        requestUserDto: RequestUserDto,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): MutableMap<String, Any> {
        var platformType: String = ""
        val returnData: MutableMap<String, Any> = mutableMapOf()

        // 이메일로 사용자 존재 여부 파악
        val userInfoInDb: User? = userRepository.findByEmail(requestUserDto.email)

        var user: User

        // 회원가입 처리
        if (userInfoInDb == null) {
            val (email) = requestUserDto
            // request body의 email 값에 따라 platformType 결정
            if (email.contains("naver")) {
                platformType = PLATFORM_TYPE.NAVER.toString()
            } else if (email.contains("gmail")) {
                platformType = PLATFORM_TYPE.GOOGLE.toString()
            }

            // 회원가입 사용자의 브라우저 정보 및 IP 주소 정보 수집
            val browserName: String = this.getBrowserInfo(request)
            val ip: String? = this.getClientIp(request)

            // DB 에 저장할 사용자 정보 DTO 생성
            val newUserDto =
                CreateUserDto(requestUserDto.email, requestUserDto.nickname, platformType, ip, browserName)
            // 사용자 정보 DTO => 사용자 정보 엔티티로 변환
            user = newUserDto.toEntity()
            userRepository.save(user); // 회원 정보 DB에 저장

            returnData["type"] = "회원가입"
            returnData["data"] = user
            // 로그인 처리
        } else {
            val (email, nickname) = requestUserDto

            // DB에 저장된 닉네임과 일치여부 확인 후 일치하지 않으면 닉네임 업데이트
            if (userInfoInDb.nickname != nickname) {
                // 닉네임 업데이트
                userRepository.updateNickname(email, nickname)
                // 업데이트 전 조회했던 사용자 엔티티에 업데이트된 닉네임 동기화
                userInfoInDb.nickname = nickname
            }

            user = userInfoInDb

            returnData["type"] = "로그인"
            returnData["user"] = user
        }

        // 세션 발급
        val session: HttpSession = request.session
        // 사용자 객체 데이터 변환 (Object to JSON string)
        val userInfoForSession = jacksonObjectMapper().writeValueAsString(user)
        // 세션에 로그인 회원 정보 보관
        session.setAttribute("user", userInfoForSession)

        // 로그인 사용자의 브라우저 정보 및 IP 주소 정보 수집
        val browserName: String = this.getBrowserInfo(request)
        val ip: String? = this.getClientIp(request) // IPv4 형식의 주소

        // 로그인 로그 쌓기
        val userAccessLog =
            userInfoInDb?.let { userInfo ->
                userInfo.id?.let {
                    UserAccessLogDto(
                        it,
                        BEHAVIOR_TYPE.LOGIN.toString(),
                        ip,
                        browserName,
                        session.id
                    )
                }
            }
        if (userAccessLog != null) {
            userAccessLogRepository.save(userAccessLog.toEntity())
        }

        return returnData
    }

    @Transactional
    fun logout(request: HttpServletRequest): MutableMap<String, String> {
        val returnData: MutableMap<String, String> = mutableMapOf()
        // 세션 가져오기
        val session: HttpSession? = request.getSession(false)

        // 세션에 사용자 정보가 존재하지 않는 경우
        if (session == null) {
            returnData["message"] = "세션이 존재하지 않습니다."
            return returnData
        }

        // 세션 속 저장되어 있는 사용자 정보 가져오기
        val userInSession: User =
            jacksonObjectMapper().readValue(session.getAttribute("user").toString(), User::class.java)

        // 세션 테이블에 저장된 세션 데이터 삭제됨.
        session.invalidate()

        // 로그아웃 로그 저장
        val userAccessLog =
            userInSession.id?.let {
                UserAccessLogDto(
                    it,
                    BEHAVIOR_TYPE.LOGOUT.toString(),
                    userInSession.ip,
                    userInSession.browser_type,
                    session.id
                )
            }

        if (userAccessLog != null) {
            userAccessLogRepository.save(userAccessLog.toEntity())
            returnData["message"] = "로그아웃 성공"
            return returnData
        }
        return returnData
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
