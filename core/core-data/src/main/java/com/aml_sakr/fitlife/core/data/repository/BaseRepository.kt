package com.aml_sakr.fitlife.core.data.repository

import com.aml_sakr.fitlife.core.data.connectivity.ConnectivityMonitor
import com.aml_sakr.fitlife.core.domain.IBaseRepository
import com.aml_sakr.fitlife.core.domain.NetworkErrors
import com.aml_sakr.fitlife.core.domain.Result

abstract class BaseRepository(
    private val connectivityMonitor: ConnectivityMonitor
) : IBaseRepository {
    protected suspend fun <T> safeCall(block: suspend () -> T): Result<T, NetworkErrors> =
        SafeCall.execute(
            isConnected = connectivityMonitor.isConnected(),
            block = block
        )
}
