package click.ufate.cleanshed.repository

import click.ufate.cleanshed.entity.AdminConfig
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AdminConfigRepository : JpaRepository<AdminConfig, Long> {
    fun findBySessionToken(token: String): AdminConfig?
}
