package lofitsky.misc.news_bot.animator

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatAction
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.RemoteWebDriver
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*


abstract class AbstractWeatherAnimationBuilder(private val seleniumUrl: String) {
    abstract fun getScreenshots(lat: Float, lon: Float, prefix: String, zoom: Int = 0): List<File>

    private val chromeOptions = ChromeOptions().apply {
        addArguments("--start-maximized")
        addArguments("--kiosk")
    }

    protected lateinit var driver: WebDriver

    fun buildWeatherAnimation(bot: Bot, chatId: Long, lat: Float, lon: Float, zoom: Int = 0) {
        bot.sendChatAction(chatId = chatId, action = ChatAction.UPLOAD_DOCUMENT)

        File("/app/screens").listFiles()
            ?.takeIf { it.isNotEmpty() }
            ?.forEach { it.delete() }

        try {
            driver = RemoteWebDriver(URL(seleniumUrl), chromeOptions)

            val screenshots = getScreenshots(
                lat = lat,
                lon = lon,
                prefix = chatId.toString(),
                zoom = zoom
            )

            val animationFile = makeAnimation(prefix = chatId.toString(), outName = chatId.toString())

            animationFile
                .takeIf { it.exists() }
                ?.also {
                    bot.sendDocument(chatId = chatId, document = it)
                    animationFile.delete()
//                    screenshots.forEach { it.delete() }
                }
                ?: run { bot.sendMessage(chatId = chatId, text = "Не получилось\uD83D\uDE15") }
        } catch(e: Exception) {
            println(e.message)
        } finally {
            driver.quit()
        }
    }

    private fun makeAnimation(prefix: String, outName: String): File {
        println("*".repeat(100))
        println("Animation building started...")
        println("*".repeat(100))

        val dateTimeStr = SimpleDateFormat("dd-MM_HH-mm").format(Date.from(Instant.now().atZone(ZoneId.of("UTC+3")).toInstant()))
        val file = File("$dateTimeStr.mp4")

        if(file.exists()) file.delete()

        val imageFileMask = imageFileMask(prefix, key = "*")

        val p = ProcessBuilder()
            .command(
                "ffmpeg",
                "-framerate", "2.5",
                "-pattern_type", "glob",
                "-i", imageFileMask,
                "-vf", "pad=ceil(iw/2)*2:ceil(ih/2)*2",
                "-c:v",
                "libx264",
                "-r", "30",
                "-pix_fmt", "yuv420p",
                file.absolutePath
            )
            .redirectOutput(File("./log/ffmpeg_o.log"))
            .redirectError(File("./log/ffmpeg_e.log"))
            .start()
            .also { println(it.info().commandLine().get()) }

        p.waitFor()

        println("*".repeat(100))
        println("Animation building finished...")
        println("*".repeat(100))

        val exitValue = p.waitFor()
        if(exitValue != 0) {
            println("!".repeat(100))
            println("Failed to build animation! Exit value = $exitValue")
            println("!".repeat(100))

            file.delete()
        }

        return file
    }

    abstract fun imageFileMask(prefix: String, key: String): String
    abstract fun screenshotName(prefix: String, key: String, totalFiles: Int, zoom: Int, time: String): String
}
