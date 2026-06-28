package com.eliasrvjimenez.guildhall

class JVMPlatform(
    override val name: String,
    override val isDebug: Boolean
) : Platform

actual fun getPlatform(): Platform = JVMPlatform(
    name = "Java ${System.getProperty("java.version")}",
    isDebug = System.getProperty("debug") == "true"
)