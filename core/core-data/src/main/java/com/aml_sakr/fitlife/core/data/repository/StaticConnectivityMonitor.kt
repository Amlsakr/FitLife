package com.aml_sakr.fitlife.core.data.repository

import com.aml_sakr.fitlife.core.data.connectivity.ConnectivityMonitor

class StaticConnectivityMonitor(
    private val connected: Boolean
) : ConnectivityMonitor {
    override fun isConnected(): Boolean = connected
}
