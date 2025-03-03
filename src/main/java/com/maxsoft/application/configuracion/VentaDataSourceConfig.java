
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.configuracion;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;

import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.example.application.repo", // Paquete donde están tus repositorios
        entityManagerFactoryRef = "entityManagerFactoryDespacho",
        transactionManagerRef = "transactionManagerDespacho"
)
public class VentaDataSourceConfig {

    // Configuración del DataSource
    @Bean(name = "despacho")
    @ConfigurationProperties(prefix = "despacho.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:mysql://172.20.0.60:3306/ventasdb?zeroDateTimeBehavior=convertToNull")
                .username("sistemaventas")
                .password("SistemaVentas123")
                .driverClassName("com.mysql.jdbc.Driver")
                .build();
    }

    // Configuración de EntityManagerFactory
   
    @Bean(name = "entityManagerFactoryDespacho")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Qualifier("despacho") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.application.venta"); // Paquete donde están tus entidades

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        // Propiedades de Hibernate
        Properties properties = new Properties();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect"); // Dialecto para PostgreSQL
        em.setJpaProperties(properties);

        return em;
    }

    // Configuración de TransactionManager
    @Bean(name = "transactionManagerDespacho")
    public PlatformTransactionManager transactionManager(@Qualifier("entityManagerFactoryDespacho") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
