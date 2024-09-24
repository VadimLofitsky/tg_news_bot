package lofitsky.misc.news_bot

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.location
import com.github.kotlintelegrambot.dispatcher.text
import lofitsky.misc.news_bot.animator.AbstractWeatherAnimationBuilder
import lofitsky.misc.news_bot.animator.GismeteoWeatherAnimationBuilder
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.env.get
import java.io.File


@SpringBootApplication
class NewsBotApp {
    companion object {
        private lateinit var token: String
        private lateinit var seleniumUrl: String
        private lateinit var weatherAnimationBuilder: AbstractWeatherAnimationBuilder

        private val singleFloatRePattern = """[+-]?([0-9]*[.])?[0-9]+"""
        private val latlonRe = Regex("(${singleFloatRePattern})[, ](${singleFloatRePattern})")

        @JvmStatic
        fun main(args: Array<String>) {
            val context = runApplication<NewsBotApp>(*args)
            token = context.environment["telegram.bot.token"]?.takeIf { it.isNotBlank() } ?: error("Failed to get Bot token from config: must be in telegram.bot.token")
            seleniumUrl = context.environment["selenium.url"]?.takeIf { it.isNotBlank() } ?: error("Failed to get Bot token from config: must be in selenium.url")

//            weatherAnimationBuilder = context.getBean(AbstractWeatherAnimationBuilder::class.java, seleniumUrl)
            weatherAnimationBuilder = GismeteoWeatherAnimationBuilder(seleniumUrl)

            runBot()
        }

        private fun runBot() {
            val locationAndZoomRe = Regex("""^(.*?)(,(.*))?$""")

            bot {
                this.token = Companion.token

                // todo добавить команду выбора между источниками погоды
                dispatch {
                    command("weather") { bot, update ->
                        update.message?.let { message ->
                            bot.sendMessage(chatId = message.chat.id, text = "Укажи место")
                        }
                    }

                    command("clear") { bot, update ->
                        update.message?.let { message ->
                            File("/app/screens").listFiles()
                                ?.takeIf { it.isNotEmpty() }
                                ?.forEach { it.delete() }
                                ?.also {
                                    bot.sendMessage(chatId = message.chat.id, text = "Файлы очищены")
                                }
                        }
                    }

                    text { bot, update ->
                        if(update.message?.text.isNullOrBlank()) return@text

                        val chatId = update.message!!.chat.id

                        if(update.message?.from?.isBot != false || update.message?.entities?.any { it.type == "bot_command" } == true) return@text

                        val res = locationAndZoomRe.find(update.message!!.text!!)
                        val locationStr = res?.groupValues?.get(1)
                        val zoom = res?.groupValues?.get(3)?.toIntOrNull() ?: 0

                        when {
                            // todo сделать получение страницы по названию города из API Gismeteo
                            locationStr == "." -> {
                                val location = 44.613723f to 41.919496f
                                bot.sendMessage(chatId = chatId, text = "Погода для Невиночки...")
                                weatherAnimationBuilder.buildWeatherAnimation(bot, chatId, location.first, location.second, zoom)
                            }

                            update.message?.text?.let { latlonRe.matches(it) } ?: false -> {
                                try {
                                    val s = update.message!!.text!!
                                    val (lat, lon) = latlonRe.find(s)!!.let { (it.groupValues[1]).toFloat() to (it.groupValues[3]).toFloat() }
                                    weatherAnimationBuilder.buildWeatherAnimation(bot, chatId, lat, lon, zoom)
                                } catch(e: Exception) {
                                    bot.sendMessage(chatId = chatId, text = "Не получилось\uD83D\uDE15")
                                    e.printStackTrace()
                                }
                            }

                            else -> bot.sendMessage(chatId = chatId, text = "Не понял...\uD83E\uDDD0")
                        }
                    }

                    location { bot, update, location -> weatherAnimationBuilder.buildWeatherAnimation(bot, update.message!!.chat.id, location.latitude, location.longitude) }
                }
            }
            .also { it.startPolling() }
        }
    }
}
