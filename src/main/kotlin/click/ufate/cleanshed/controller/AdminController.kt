package click.ufate.cleanshed.controller

import click.ufate.cleanshed.dto.*
import click.ufate.cleanshed.service.AdminService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val adminService: AdminService
) {
    @PostMapping("/login")
    fun login(@RequestBody request: AdminLoginRequest): ResponseEntity<AdminLoginResponse> {
        val token = adminService.authenticate(request.password)
            ?: return ResponseEntity.badRequest().build()

        return ResponseEntity.ok(AdminLoginResponse(token = token))
    }

    @PostMapping("/logout")
    fun logout(@RequestHeader("X-Admin-Token") token: String): ResponseEntity<Void> {
        adminService.logout(token)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/verify")
    fun verifyToken(@RequestHeader("X-Admin-Token") token: String): ResponseEntity<Map<String, Boolean>> {
        val isValid = adminService.validateSession(token)
        return ResponseEntity.ok(mapOf("valid" to isValid))
    }

    @GetMapping("/password")
    fun getMasterPassword(@RequestHeader("X-Admin-Token") token: String): ResponseEntity<Map<String, String>> {
        if (!adminService.validateSession(token)) {
            return ResponseEntity.badRequest().build()
        }

        val password = adminService.getMasterPassword()
        return ResponseEntity.ok(mapOf("password" to password))
    }

    @PostMapping("/reset-password")
    fun resetPassword(@RequestHeader("X-Admin-Token") token: String): ResponseEntity<PasswordResetResponse> {
        if (!adminService.validateSession(token)) {
            return ResponseEntity.badRequest().build()
        }

        val newPassword = adminService.resetPassword()
        return ResponseEntity.ok(PasswordResetResponse(newPassword = newPassword))
    }
}
