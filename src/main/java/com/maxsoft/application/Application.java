
package com.maxsoft.application;

//import com.example.application.data.SamplePersonRepository;
import com.maxsoft.application.configuracion.MysqlConnection;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
//@ComponentScan(basePackages= {"com.maxsoft.application.*"})
@Theme(value = "gestion-colmadom")

public class Application implements AppShellConfigurator {

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
        MysqlConnection.getConnection();
    }

//    @Bean
//    SqlDataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer(DataSource dataSource,
//            SqlInitializationProperties properties, SamplePersonRepository repository) {
//        // This bean ensures the database is only initialized when empty
//        return new SqlDataSourceScriptDatabaseInitializer(dataSource, properties) {
//            @Override
//            public boolean initializeDatabase() {
//                if (repository.count() == 0L) {
//                    return super.initializeDatabase();
//                }
//                return false;
//            }
//        };
//    }
}
