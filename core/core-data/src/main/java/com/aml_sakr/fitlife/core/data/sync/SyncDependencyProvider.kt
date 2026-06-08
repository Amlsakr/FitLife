package com.aml_sakr.fitlife.core.data.sync

import com.aml_sakr.fitlife.core.data.connectivity.ConnectivityMonitor

object SyncDependencyProvider {
    var dao: SyncTestDao? = null
    var remoteClient: RemoteSyncClient? = null
    var connectivityMonitor: ConnectivityMonitor? = null
}
