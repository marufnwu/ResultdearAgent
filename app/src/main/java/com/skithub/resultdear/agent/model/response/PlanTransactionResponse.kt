package com.skithub.resultdear.agent.model.response

import com.skithub.resultdear.agent.model.Transaction

data class PlanTransactionResponse(
    val error: Boolean,
    val error_description: String?,
    val transaction: Transaction?
)