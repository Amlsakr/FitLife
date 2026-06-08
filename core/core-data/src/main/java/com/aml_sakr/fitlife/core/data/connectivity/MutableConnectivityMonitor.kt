package com.aml_sakr.fitlife.core.data.connectivity

class MutableConnectivityMonitor(
    private var isOnline: Boolean = true
) : ConnectivityMonitor {
    override fun isConnected(): Boolean = isOnline

    fun setConnected(online: Boolean) {
        isOnline = online
    }
}
