package com.aml_sakr.fitlife.core.data.repository

import com.aml_sakr.fitlife.core.domain.NetworkErrors
import com.aml_sakr.fitlife.core.domain.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class SafeCallTest {
    @Test
    fun safeCall_returnsSuccessWhenConnectedAndBlockSucceeds() = runTest {
        val result = SafeCall.execute(isConnected = true) { "ok" }

        assertEquals(Result.Success("ok"), result)
    }

    @Test
    fun safeCall_returnsNoConnectionWhenOffline() = runTest {
        val result = SafeCall.execute(isConnected = false) { "ignored" }

        assertEquals(Result.Failure(NetworkErrors.NoConnection), result)
    }

    @Test
    fun safeCall_mapsIoExceptionsToNetworkError() = runTest {
        val result = SafeCall.execute(isConnected = true) {
            throw IOException("socket closed")
        }

        assertEquals(Result.Failure(NetworkErrors.NoConnection), result)
    }

    @Test
    fun safeCall_rethrowsCancellationExceptions() = runTest {
        try {
            SafeCall.execute(isConnected = true) {
                throw CancellationException("scope cancelled")
            }
            fail("Expected cancellation to propagate")
        } catch (expected: CancellationException) {
            assertEquals("scope cancelled", expected.message)
        }
    }

    @Test
    fun responseToResult_mapsHttpStatusesWithoutRetrofitDependency() {
        val success = NetworkResponse(statusCode = 200, body = "body")
        val emptySuccess = NetworkResponse<Unit>(statusCode = 204, body = null)
        val unauthorized = NetworkResponse<String>(statusCode = 401, body = null)
        val server = NetworkResponse<String>(statusCode = 503, body = null)

        assertEquals(Result.Success("body"), success.toResult())
        assertEquals(Result.Success(Unit), emptySuccess.toResult(Unit))
        assertEquals(Result.Failure(NetworkErrors.Unauthorized), unauthorized.toResult())
        assertEquals(Result.Failure(NetworkErrors.ServerError), server.toResult())
    }

    @Test
    fun baseRepositoryUsesConnectivityBeforeExecutingCall() = runTest {
        val repository = TestRepository(isConnected = false)

        val result = repository.load()

        assertTrue(result is Result.Failure)
        assertEquals(NetworkErrors.NoConnection, (result as Result.Failure).error)
    }

    private class TestRepository(isConnected: Boolean) : BaseRepository(
        connectivityMonitor = StaticConnectivityMonitor(isConnected)
    ) {
        suspend fun load(): Result<String, NetworkErrors> = safeCall { "loaded" }
    }
}
