package lofitsky.misc.news_bot.animator

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs


class GismeteoWeatherAnimationBuilder(seleniumUrl: String) : AbstractWeatherAnimationBuilder(seleniumUrl) {
    override fun getScreenshots(lat: Float, lon: Float, prefix: String, zoom: Int): List<File> {
        driver.get("https://www.gismeteo.ru/nowcast-nevinnomyssk-5222/")
        println("Opening https://www.gismeteo.ru/nowcast-nevinnomyssk-5222/ ... zoom = $zoom")
        WebDriverWait(driver, 30).until {
            (it as JavascriptExecutor).executeScript("return document.readyState") == "complete"
        }

        WebDriverWait(driver, 30).until {
            (it as JavascriptExecutor).executeScript("return document.visibilityState") == "visible"
        }

        driver.findElement(By.className("resize-btn")).click()

        WebDriverWait(driver, 30).until {
            (it as JavascriptExecutor).executeScript("return document.readyState") == "complete"
        }

        WebDriverWait(driver, 30).until {
            (it as JavascriptExecutor).executeScript("return document.visibilityState") == "visible"
        }

        val zoomSuffix = if(zoom < 0) "out" else "in"
        val zoomBtn = driver.findElement(By.cssSelector("button.zoom-btn.zoom-$zoomSuffix"))
        repeat(abs(zoom)) {
            zoomBtn.click()
            Thread.sleep(1000)
        }

        val timelineButtons = driver.findElements(By.cssSelector(".timeline > .timeline-item"))
        val totalFiles = timelineButtons.size

        timelineButtons.forEach { btn ->
            println("Clicking timeline ${btn.text} button to preload weather image")
            btn.click()
            Thread.sleep(500)
        }

        val result = timelineButtons.mapIndexed { i, btn ->
            println("Clicking timeline ${btn.text} button to make screenshot")
            btn.click()
            Thread.sleep(1000)
            val screenshot = (driver as TakesScreenshot).getScreenshotAs(OutputType.BYTES)
            val frame = i.toString().padStart(2, '0')
            val timeStr = btn.text.replace(":", "-")
            val file = File(screenshotName(prefix = prefix, key = frame, totalFiles = totalFiles, zoom = zoom, time = timeStr))
            FileOutputStream(file).use {
                it.write(screenshot)
                it.flush()
            }

            println("Frame $frame: ${file.absolutePath}")
            file
        }

        return result
    }

    override fun imageFileMask(prefix: String, key: String): String = """screens/id$prefix-z$key.png"""

    override fun screenshotName(prefix: String, key: String, totalFiles: Int, zoom: Int, time: String): String
        = """screens/id$prefix-z$zoom-n$key-$totalFiles-$time.png"""
}
