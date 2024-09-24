package lofitsky.misc.news_bot.animator

import org.openqa.selenium.*
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.io.FileOutputStream


class YandexWeatherAnimationBuilder(seleniumUrl: String) : AbstractWeatherAnimationBuilder(seleniumUrl) {
    companion object {
        private const val TOTAL_FRAMES = 13
    }

    override fun getScreenshots(lat: Float, lon: Float, prefix: String, zoom: Int): List<File> {
        driver.get("https://yandex.ru/pogoda/maps/nowcast?lat=$lat&lon=$lon&z=8.5")
        WebDriverWait(driver, 30).until {
            (it as JavascriptExecutor).executeScript("return document.readyState") == "complete"
        }

        WebDriverWait(driver, 30).until {
            (it as JavascriptExecutor).executeScript("return document.visibilityState") == "visible"
        }

        val result = (0 until TOTAL_FRAMES).map {
            WebDriverWait(driver, 30).until {
                (it as JavascriptExecutor).executeScript("return document.visibilityState") == "visible"
            }

            driver.findElement(By.ByXPath(".//html")).sendKeys(Keys.ARROW_RIGHT)

            Thread.sleep(3000)

            listOf(
                // header
                ".yandex-header",
                // map type
                ".radio-button_view_classic.weather-maps__layers",
                // left panel
                "div.map-left-pane",
                // zoom
                ".ymaps-2-1-79-controls__control:nth-of-type(3)",
                // location
                ".ymaps-2-1-79-controls__control:nth-of-type(4)",
                // идёт дождь / нет дождя
                "div[class*=MapsPie_groundedL1__]",
                // copyright
                ".ymaps-2-1-79-copyright",
                // timeline tooltip
                ".timeline__tooltip",
            ).forEach { xPathExpr ->
                driver.findElements(By.ByCssSelector(xPathExpr))
                    .firstOrNull()
                    ?.also { (driver as JavascriptExecutor).executeScript("arguments[0].remove();", it) }
            }

            val screenshot = (driver as TakesScreenshot).getScreenshotAs(OutputType.BYTES)
            val frame = it.toString().padStart(2, '0')
            val file = File(screenshotName(prefix = prefix, key = frame, totalFiles = TOTAL_FRAMES, zoom = 1, "-"))
            FileOutputStream(file).use {
                it.write(screenshot)
                it.flush()
            }

            println("Frame $frame: ${file.absolutePath}")
            file
        }

        return result
    }

    override fun imageFileMask(prefix: String, key: String): String = """screens/id$prefix-z"""

    override fun screenshotName(prefix: String, key: String, totalFiles: Int, zoom: Int, time: String): String
        = """screens/id$prefix-z$zoom-n$key-$time.png"""
}
