package com.rn00n.inhibitor.commons.datasources.config

import com.rn00n.inhibitor.commons.datasources.CustomDataSourceProperties
import com.rn00n.inhibitor.commons.datasources.annotations.InhibitorRepository
import org.hibernate.cfg.AvailableSettings
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(
    basePackages = ["com.rn00n.inhibitor.infrastructure.*"],
    includeFilters = [
        ComponentScan.Filter(
            type = FilterType.ANNOTATION,
            classes = [InhibitorRepository::class]
        )
    ],
    entityManagerFactoryRef = "inhibitorEntityManagerFactory",
    transactionManagerRef = "inhibitorTransactionManager"
)
class InhibitorDataSourceConfig(
    customDataSourceProperties: CustomDataSourceProperties,
) {

    companion object {
        const val PERSISTENCE_UNIT = "inhibitor-db"
        const val DATASOURCE_BEAN = "inhibitorDataSource"
        const val ENTITY_MANAGER_FACTORY_BEAN = "inhibitorEntityManagerFactory"
        const val TRANSACTION_MANAGER_BEAN = "inhibitorTransactionManager"
        const val JDBC_TEMPLATE_BEAN = "inhibitorJdbcTemplate"
    }

    val inhibitorDataSourceProperties = customDataSourceProperties.databases["rn00n"]!!["inhibitor"]!!

    @Bean(DATASOURCE_BEAN)
    fun inhibitorDataSource(): DataSource {
        return DataSourceBuilder.create().apply {
            driverClassName(inhibitorDataSourceProperties.driverClassName)
            url(inhibitorDataSourceProperties.url)
            username(inhibitorDataSourceProperties.username)
            password(inhibitorDataSourceProperties.password)
        }.build()
    }

    @Bean(ENTITY_MANAGER_FACTORY_BEAN)
    fun inhibitorEntityManagerFactory(): LocalContainerEntityManagerFactoryBean {
        val jpaProperties: MutableMap<String, Any> = mutableMapOf()
        jpaProperties[AvailableSettings.HBM2DDL_AUTO] = inhibitorDataSourceProperties.ddlAuto
//        jpaProperties[AvailableSettings.SHOW_SQL] = "true"                 // 콘솔에 SQL 출력
//        jpaProperties[AvailableSettings.FORMAT_SQL] = "true"               // SQL 예쁘게 출력
//        jpaProperties[AvailableSettings.USE_SQL_COMMENTS] = "true"         // JPQL → SQL 주석 추가

        val localContainerEntityManagerFactoryBean = LocalContainerEntityManagerFactoryBean()
        localContainerEntityManagerFactoryBean.dataSource = inhibitorDataSource()
        localContainerEntityManagerFactoryBean.persistenceUnitName = PERSISTENCE_UNIT
        localContainerEntityManagerFactoryBean.jpaVendorAdapter = HibernateJpaVendorAdapter()
        localContainerEntityManagerFactoryBean.setPackagesToScan(
            "com.rn00n.inhibitor.domain.accounts",
            "com.rn00n.inhibitor.domain.oauth2",
        )
        localContainerEntityManagerFactoryBean.setJpaPropertyMap(jpaProperties)
        localContainerEntityManagerFactoryBean.afterPropertiesSet()
        return localContainerEntityManagerFactoryBean
    }

    @Bean(TRANSACTION_MANAGER_BEAN)
    fun inhibitorTransactionManager(): JpaTransactionManager =
        JpaTransactionManager(inhibitorEntityManagerFactory().`object`!!)

    @Bean(JDBC_TEMPLATE_BEAN)
    fun jdbcTemplate(): JdbcTemplate {
        return JdbcTemplate(inhibitorDataSource())
    }
}