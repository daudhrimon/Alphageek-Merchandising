package com.gdm.alphageek.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planogram_questions")
data class PlanogramQuestions(
    @PrimaryKey
    val id: Long,
    val question_name: String?,
    var answer_type:Int=0
)