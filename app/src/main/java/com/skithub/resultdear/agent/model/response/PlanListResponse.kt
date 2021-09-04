package com.skithub.resultdear.agent.model.response

import com.skithub.resultdear.agent.model.Plan

data class PlanListResponse(
    val error: Boolean,
    val error_description: String?,
    val planList: List<Plan>?
)