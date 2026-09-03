package click.ufate.cleanshed.service

import click.ufate.cleanshed.entity.AdminConfig
import click.ufate.cleanshed.repository.AdminConfigRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.Base64

@Service
class AdminService(
    private val adminConfigRepository: AdminConfigRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val secureRandom = SecureRandom()

    @PostConstruct
    fun init() {
        val config = getOrCreateAdminConfig()
        log.info("========================================")
        log.info("  Мастер-пароль: ${config.masterPassword}")
        log.info("========================================")
    }

    fun getOrCreateAdminConfig(): AdminConfig {
        return adminConfigRepository.findById(1).orElseGet {
            val config = AdminConfig(
                id = 1,
                masterPassword = generatePassword(),
                passwordGenerated = true
            )
            adminConfigRepository.save(config)
        }
    }

    fun authenticate(password: String): String? {
        val config = getOrCreateAdminConfig()

        if (config.masterPassword == password) {
            val token = generateToken()
            config.sessionToken = token
            config.sessionExpiry = LocalDateTime.now().plusHours(24)
            adminConfigRepository.save(config)
            return token
        }

        return null
    }

    fun validateSession(token: String): Boolean {
        val config = adminConfigRepository.findBySessionToken(token)
        return config != null &&
                config.sessionExpiry != null &&
                config.sessionExpiry!!.isAfter(LocalDateTime.now())
    }

    fun logout(token: String) {
        val config = adminConfigRepository.findBySessionToken(token)
        if (config != null) {
            config.sessionToken = null
            config.sessionExpiry = null
            adminConfigRepository.save(config)
        }
    }

    @Transactional
    fun resetPassword(): String {
        val config = getOrCreateAdminConfig()
        val newPassword = generatePassword()
        config.masterPassword = newPassword
        config.passwordGenerated = true
        adminConfigRepository.save(config)
        return newPassword
    }

    fun getMasterPassword(): String {
        return getOrCreateAdminConfig().masterPassword
    }

    private fun generatePassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..12)
            .map { chars[secureRandom.nextInt(chars.length)] }
            .joinToString("")
    }

    private fun generateToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
