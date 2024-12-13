package com.example.visit.services.permission

interface PermissionHandler {
    fun requestPermission(permission: String, requestCode: Int)
    fun isPermissionGranted(permission: String): Boolean
}