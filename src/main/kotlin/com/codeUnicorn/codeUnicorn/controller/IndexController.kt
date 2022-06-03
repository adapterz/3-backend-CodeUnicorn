package com.codeUnicorn.codeUnicorn.controller

import com.codeUnicorn.codeUnicorn.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
class IndexController {

    @Autowired
    private lateinit var userService: UserService

    @GetMapping(path = ["/"])

    fun index(request: HttpServletRequest): ResponseEntity<String> {
        val browserName: String = userService.getBrowserInfo(request)
        val ip: String? = userService.getClientIp(request)
        println(request.getHeader("user-agent"))
        println("브라우저 이름: $browserName")
        println("IP 정보: $ip")
        val hello = "Hello World!"
        return ResponseEntity.ok(hello)
    }
}
