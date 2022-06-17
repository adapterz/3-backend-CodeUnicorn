package com.codeUnicorn.codeUnicorn.domain.section

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

// @Entity
// @Table(name = "section")
// data class Section(
//     @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
//     val id: Int? = null,
//     @Column(name = "course_id")
//     var courseId: Int,
//     var name: String,
//     @Column(name = "total_hours")
//     var totalHours: Int,
//     @Column(name = " lecture_count")
//     var lectureCount: Int,
// )

@Entity
@Table(name = "section")
data class SectionDetailInfo(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val sectionId: Int,

    )
