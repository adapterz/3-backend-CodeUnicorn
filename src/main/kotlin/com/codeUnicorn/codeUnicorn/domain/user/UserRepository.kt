package com.codeUnicorn.codeUnicorn.domain.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

// DB Layer 접근자
// JpaRepository 는 기본적으로 CRUD 쿼리 함수 제공
interface UserRepository : JpaRepository<User, Int> {
    fun findByEmail(email: String): User?

    @Transactional
    @Modifying
    @Query("update user set nickname = :nickname where id = :id", nativeQuery = true)
    fun updateNickname(@Param("id") id: Int, @Param("nickname") nickname: String): Int?

    @Transactional
    @Query("select * from user where nickname = :nickname", nativeQuery = true)
    fun findByNickname(@Param("nickname") nickname: String): User?

    @Transactional
    @Modifying
    @Query("update user set profile_path = :profilePath where id = :id", nativeQuery = true)
    fun updateProfile(@Param("id") id: Int, @Param("profilePath") profilePath: String): Int
}