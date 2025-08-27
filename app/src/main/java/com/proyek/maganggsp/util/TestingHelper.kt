// File: app/src/main/java/com/proyek/maganggsp/util/TestingHelper.kt
package com.proyek.maganggsp.util

import android.util.Log
import com.proyek.maganggsp.BuildConfig

/**
 * Helper utilities untuk manual testing API login
 * Digunakan untuk debugging dan validasi integration
 */
object TestingHelper {

    private const val TAG = "TestingHelper"

    /**
     * Validasi konfigurasi network untuk API login
     */
    fun validateNetworkConfig(): String {
        val report = StringBuilder()

        report.appendLine("🔧 NETWORK CONFIGURATION CHECK")
        report.appendLine("================================")
        report.appendLine("Base URL: ${BuildConfig.BASE_URL}")
        report.appendLine("Build Type: ${BuildConfig.BUILD_TYPE}")
        report.appendLine("Target API: ${BuildConfig.BASE_URL}auth/login")
        report.appendLine("HTTP Allowed: ${BuildConfig.DEBUG}") // Cleartext in debug only
        report.appendLine("")

        // Expected request format
        report.appendLine("📤 EXPECTED REQUEST FORMAT")
        report.appendLine("==========================")
        report.appendLine("Method: POST")
        report.appendLine("URL: http://192.168.168.6:8180/api/auth/login")
        report.appendLine("Headers:")
        report.appendLine("  Content-Type: application/json")
        report.appendLine("  Accept: application/json")
        report.appendLine("  User-Agent: GesPay-Admin-Android/${BuildConfig.VERSION_NAME}")
        report.appendLine("Body:")
        report.appendLine("  {")
        report.appendLine("    \"email\": \"lalan@gsp.co.id\",")
        report.appendLine("    \"password\": \"123456\"")
        report.appendLine("  }")
        report.appendLine("")

        // Expected response format
        report.appendLine("📥 EXPECTED RESPONSE FORMAT")
        report.appendLine("===========================")
        report.appendLine("HTTP 200 OK")
        report.appendLine("Content-Type: application/json")
        report.appendLine("Body:")
        report.appendLine("  {")
        report.appendLine("    \"token\": \"eyJ...\",")
        report.appendLine("    \"email\": \"lalan@gsp.co.id\",")
        report.appendLine("    \"role\": \"admin\"")
        report.appendLine("  }")
        report.appendLine("")
        report.appendLine("Note: Server sends 'email' and 'role', not 'admin_name' and 'admin_email'")
        report.appendLine("Admin name will be derived from email (e.g., 'lalan@gsp.co.id' -> 'Lalan')")

        return report.toString()
    }

    /**
     * Curl command untuk testing manual dari command line
     */
    fun generateCurlCommand(): String {
        return """
        📋 MANUAL CURL TEST COMMAND
        ===========================
        
        curl -X POST http://192.168.168.6:8180/api/auth/login \
          -H "Content-Type: application/json" \
          -H "Accept: application/json" \
          -H "User-Agent: GesPay-Admin-Android/${BuildConfig.VERSION_NAME}" \
          -d '{
            "email": "lalan@gsp.co.id",
            "password": "123456"
          }'
        
        Expected Success Response:
        {
          "admin_name": "Admin Name",
          "admin_email": "lalan@gsp.co.id", 
          "token": "eyJ..."
        }
        
        Expected Error Responses:
        - 401: {"message": "Invalid credentials"}
        - 422: {"message": "Validation failed"}
        - 500: {"message": "Internal server error"}
        """.trimIndent()
    }

    /**
     * Postman/Insomnia request template
     */
    fun getPostmanTemplate(): String {
        return """
        🛠️ POSTMAN/INSOMNIA TEMPLATE
        ============================
        
        Method: POST
        URL: http://192.168.168.6:8180/api/auth/login
        
        Headers:
        Content-Type: application/json
        Accept: application/json
        User-Agent: GesPay-Admin-Android/${BuildConfig.VERSION_NAME}
        
        Body (raw JSON):
        {
          "email": "lalan@gsp.co.id",
          "password": "123456"
        }
        
        🔍 TROUBLESHOOTING CHECKLIST:
        ☑️ Server running on 192.168.168.6:8180
        ☑️ Network connectivity to server
        ☑️ Firewall allows port 8180
        ☑️ Content-Type exactly "application/json" (no charset)
        ☑️ Request body is valid JSON
        ☑️ Credentials are correct
        """.trimIndent()
    }

    /**
     * Debug network request untuk development
     */
    fun debugNetworkRequest(
        url: String,
        headers: Map<String, String>,
        body: String
    ) {
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🌐 DEBUG NETWORK REQUEST")
            Log.d(TAG, "URL: $url")
            Log.d(TAG, "Headers:")
            headers.forEach { (key, value) ->
                Log.d(TAG, "  $key: $value")
            }
            Log.d(TAG, "Body: $body")
            Log.d(TAG, "Content Length: ${body.length}")
        }
    }

    /**
     * Validate server response structure
     */
    fun validateResponseStructure(
        httpCode: Int,
        responseBody: String?
    ): String {
        val report = StringBuilder()

        report.appendLine("📥 RESPONSE VALIDATION")
        report.appendLine("======================")
        report.appendLine("HTTP Code: $httpCode")
        report.appendLine("Response Body: ${responseBody ?: "NULL"}")
        report.appendLine("")

        when (httpCode) {
            200 -> {
                report.appendLine("✅ SUCCESS - Checking JSON structure...")
                if (responseBody != null) {
                    val hasAdminName = responseBody.contains("admin_name")
                    val hasAdminEmail = responseBody.contains("admin_email")
                    val hasToken = responseBody.contains("token")

                    report.appendLine("admin_name field: ${if (hasAdminName) "✅" else "❌"}")
                    report.appendLine("admin_email field: ${if (hasAdminEmail) "✅" else "❌"}")
                    report.appendLine("token field: ${if (hasToken) "✅" else "❌"}")
                } else {
                    report.appendLine("❌ Response body is null")
                }
            }
            401 -> report.appendLine("❌ UNAUTHORIZED - Check credentials")
            422 -> report.appendLine("❌ VALIDATION ERROR - Check request format")
            500 -> report.appendLine("❌ SERVER ERROR - Check server logs")
            else -> report.appendLine("❌ UNEXPECTED HTTP CODE")
        }

        return report.toString()
    }

    /**
     * Print comprehensive debug info
     */
    fun printDebugInfo() {
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, validateNetworkConfig())
            Log.d(TAG, generateCurlCommand())
            Log.d(TAG, getPostmanTemplate())
        }
    }
}