package com.maxsoft.application;

//import com.example.application.data.SamplePersonRepository;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.shared.ui.Transport;
import com.vaadin.flow.theme.Theme;
import java.util.TimeZone;
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
// lsof -i :8081
// kill -9 <PID>

@Theme(value = "gestion-colmadom")
//@Push()
@Push(value = PushMode.AUTOMATIC, transport = Transport.WEBSOCKET_XHR) // Puedes usar solo @Push si quieres default
public class Application implements AppShellConfigurator {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Application.class);

     
    //para  buscar libreria duplicadas : mvn dependency:tree > deps.txt
    // mvn versions:display-dependency-updates
    // mvn clean



    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);

        TimeZone.setDefault(TimeZone.getTimeZone("America/Santo_Domingo"));

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        log.debug("Max Memory: " + maxMemory / 1024 / 1024 + " MB");
        log.debug("Allocated Memory: " + allocatedMemory / 1024 / 1024 + " MB");
        log.debug("Free Memory in allocated: " + freeMemory / 1024 / 1024 + " MB");
        log.debug("Used Memory: " + (allocatedMemory - freeMemory) / 1024 / 1024 + " MB");
        
        log.debug("Prueba 1: ");

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
