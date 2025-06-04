package com.rn00n.inhibitor.commons.datasources.config

import com.rn00n.inhibitor.commons.datasources.CustomDataSourceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataSourcePropertiesConfig {

    @Bean
    fun customDataSourceProperties(): CustomDataSourceProperties {
        return CustomDataSourceProperties()
    }
}
