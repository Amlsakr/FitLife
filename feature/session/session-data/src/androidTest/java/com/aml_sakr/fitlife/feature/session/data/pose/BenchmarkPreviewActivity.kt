package com.aml_sakr.fitlife.feature.session.data.pose

import android.Manifest
import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.camera.view.PreviewView
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class BenchmarkPreviewActivity : Activity() {
    lateinit var previewView: PreviewView
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        previewView = PreviewView(this)
        setContentView(previewView)
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            finishPermissionRequest(true)
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CameraRequestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CameraRequestCode) {
            finishPermissionRequest(grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED)
        }
    }

    private fun finishPermissionRequest(result: Boolean) {
        granted = result
        permissionLatch.countDown()
    }

    companion object {
        private const val CameraRequestCode = 7004
        private var permissionLatch = CountDownLatch(1)
        @Volatile
        private var granted = false

        fun launch(
            instrumentation: Instrumentation,
            context: Context,
            timeoutSeconds: Long = 60L
        ): BenchmarkPreviewActivity {
            permissionLatch = CountDownLatch(1)
            granted = context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            val intent = Intent(context, BenchmarkPreviewActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val activity = instrumentation.startActivitySync(intent) as BenchmarkPreviewActivity
            if (!granted) {
                permissionLatch.await(timeoutSeconds, TimeUnit.SECONDS)
            }
            check(granted) { "Camera permission must be granted from the device prompt before benchmark starts." }
            return activity
        }
    }
}
