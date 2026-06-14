package com.aml_sakr.fitlife.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainContractsTest {
    @Test
    fun result_successExposesValueAndDataAlias() {
        val result: Result<String, NetworkErrors> = Result.Success("cached-plan")

        assertTrue(result is Result.Success)
        result as Result.Success
        assertEquals("cached-plan", result.value)
        assertEquals("cached-plan", result.data)
    }

    @Test
    fun networkErrorsExposeStableCodesAndMessages() {
        val error: Error = NetworkErrors.Timeout

        assertEquals("network_timeout", error.code)
        assertEquals("Request timed out.", error.message)
    }

    @Test
    fun baseRepositoryIsSharedDomainContract() {
        val repository = object : IBaseRepository {}

        assertTrue(repository is IBaseRepository)
    }
}
