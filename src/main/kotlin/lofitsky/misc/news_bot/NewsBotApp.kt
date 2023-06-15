package lofitsky.misc.news_bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class NewsBotApp

fun main(args: Array<String>) {
    runApplication<NewsBotApp>(*args)
}
