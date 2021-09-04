package com.skithub.resultdear.agent.model.response

import com.skithub.resultdear.agent.model.ReferUser

data class ReferUserResponse(
    val error: Boolean?,
    val error_description: String?,
    val users: List<ReferUser>?
)