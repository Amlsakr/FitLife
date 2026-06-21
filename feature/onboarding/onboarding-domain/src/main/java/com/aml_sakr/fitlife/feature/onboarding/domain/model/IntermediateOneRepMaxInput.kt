package com.aml_sakr.fitlife.feature.onboarding.domain.model

import java.io.Serializable

data class IntermediateOneRepMaxInput(
    val valueText: String = "",
    val unit: OneRepMaxUnit = OneRepMaxUnit.Kilograms
) : Serializable

enum class OneRepMaxUnit(val kgMultiplier: Float) : Serializable {
    Kilograms(1f),
    Pounds(0.45359237f)
}
