package com.hado.playgroundcamera

import android.content.Context
import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.SurfaceView

/**
 * Created by Hado on 8/16/17.
 */
class CameraPreview : SurfaceView, SurfaceHolder.Callback {

    var mCamera: Camera
    var mHolder: SurfaceHolder

    constructor(context: Context, camera: Camera) : super(context) {
        mCamera = camera
        mHolder = holder
        mHolder.addCallback(this)
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }


    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        mCamera.release()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        mCamera.setPreviewDisplay(holder)
        mCamera.startPreview()
    }
}