// File: app/src/main/java/com/proyek/maganggsp/data/dto/UpdateProfileRequest.kt
package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Request DTO untuk PUT /profiles/ppid/{ppid}
 * Sesuai dengan HTTP request: {"mpPpid": "PIDLKTD0025blok"}
 */
data class UpdateProfileRequest(
    @SerializedName("mpPpid") val mpPpid: String
) {
    companion object {
        /**
         * Helper untuk create block request
         */
        fun createBlockRequest(ppid: String): UpdateProfileRequest {
            return UpdateProfileRequest(mpPpid = "${ppid}blok")
        }

        /**
         * Helper untuk create unblock request
         */
        fun createUnblockRequest(ppid: String): UpdateProfileRequest {
            return UpdateProfileRequest(mpPpid = ppid)
        }
    }
}
