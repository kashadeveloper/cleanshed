package click.ufate.cleanshed

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CleanScheduleApplication

fun main(args: Array<String>) {
    runApplication<CleanScheduleApplication>(*args)
}
