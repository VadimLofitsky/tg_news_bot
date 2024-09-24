package lofitsky.misc.news_bot

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.PropertySources
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.io.support.EncodedResource
import org.springframework.core.io.support.PropertySourceFactory
import java.io.IOException
import java.util.*

@Configuration
@PropertySources(
    PropertySource(ignoreResourceNotFound = true, value = ["file:\${appConfigPath}"], factory = YamlPropertySourceFactory::class),
)
class AppConfig

@Configuration
@PropertySources(
    PropertySource(ignoreResourceNotFound = false, value = ["file:\${botConfigPath}"], factory = YamlPropertySourceFactory::class),
)
class BotConfig

class YamlPropertySourceFactory : PropertySourceFactory {
    @Throws(IOException::class)
    override fun createPropertySource(name: String?, encodedResource: EncodedResource): PropertiesPropertySource {
        val factory = YamlPropertiesFactoryBean()
        factory.setResources(encodedResource.resource)
        val properties: Properties = factory.getObject() ?: Properties()
        return PropertiesPropertySource(encodedResource.resource.filename!!, properties)
    }
}
