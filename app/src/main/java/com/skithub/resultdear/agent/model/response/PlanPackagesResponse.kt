package com.skithub.resultdear.agent.model.response

import com.skithub.resultdear.agent.model.Package

data class PlanPackagesResponse(
    val error: Boolean,
    val error_description: String?,
    val packages: List<Package>?
)