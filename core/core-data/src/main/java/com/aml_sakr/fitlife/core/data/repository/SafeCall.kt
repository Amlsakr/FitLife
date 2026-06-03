package com.aml_sakr.fitlife.core.data.repository

import com.aml_sakr.fitlife.core.domain.NetworkErrors
import com.aml_sakr.fitlife.core.domain.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import java.io.IOException
import java.net.SocketTimeoutException

object SafeCall {
    suspend fun <T> execute(
        isConnected: Boolean,
        block: suspend () -> T
    ): Result<T, NetworkErrors> {
        if (!isConnected) {
            return Result.Failure(NetworkErrors.NoConnection)
        }

        return try {
            Result.Success(block())
        } catch (_: SocketTimeoutException) {
            Result.Failure(NetworkErrors.Timeout)
        } catch (_: TimeoutCancellationException) {
            Result.Failure(NetworkErrors.Timeout)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: IOException) {
            Result.Failure(NetworkErrors.NoConnection)
        } catch (_: SecurityException) {
            Result.Failure(NetworkErrors.Unauthorized)
        } catch (_: IllegalArgumentException) {
            Result.Failure(NetworkErrors.SerializationError)
        } catch (_: Exception) {
            Result.Failure(NetworkErrors.UnknownApiError)
        }
    }
}
