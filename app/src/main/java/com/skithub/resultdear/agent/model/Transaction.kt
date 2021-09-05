package com.skithub.resultdear.agent.model

data class Transaction(
    val price: Int,
    val transactionRef: String?,
    val validity: String?
)