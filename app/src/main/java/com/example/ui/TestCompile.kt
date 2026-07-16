package com.example.ui

import coil.request.ImageRequest
import coil.request.videoFrameMillis

fun test(builder: ImageRequest.Builder) {
    builder.videoFrameMillis(1000)
}
