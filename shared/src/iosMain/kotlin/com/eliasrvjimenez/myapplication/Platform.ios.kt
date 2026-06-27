package com.eliasrvjimenez.myapplication

import platform.UIKit.UIDevice
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform as NativePlatform

class IOSPlatform(
    override val name: String,
    override val isDebug: Boolean
) : Platform

@OptIn(ExperimentalNativeApi::class)
actual fun getPlatform(): Platform = IOSPlatform(
    name = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion,
    isDebug = NativePlatform.isDebugBinary
)