package com.codeUnicorn.codeUnicorn.domain.user

import com.codeUnicorn.codeUnicorn.constant.ROLE_TYPE
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "User") // 엔티티와 매핑할 테이블 지정
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
    var email: String,
    var nickname: String?,
    var platform_type: String,
    var ip: String?, // 최초 회원가입 IP
    var browser_type: String, // Chrome, Safari, Whale, Firefox, Opera, Edge, Samsung Internet 등
    @Enumerated(EnumType.STRING) // Enum 값을 문자열로 저장될 수 있도록 선언
    val role: ROLE_TYPE = ROLE_TYPE.USER,
    @Column(name = "created_at")
    @CreationTimestamp // insert 쿼리에 대해 자동으로 생성
    val createDateTime: LocalDateTime = LocalDateTime.now(),
    @Column(name = "updated_at")
    var updateDateTime: LocalDateTime? = null,
    @Column(name = "deleted_at")
    var deleteDateTime: LocalDateTime? = null
)
