package br.com.danielschiavo.shop;

import java.io.IOException;
import java.util.Properties;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableFeignClients(basePackages = "br.com.danielschiavo")
@ComponentScan(basePackages = "br.com.danielschiavo")
@EntityScan("br.com.danielschiavo.shop.model")
@PropertySource("classpath:application-${spring.profiles.active}.properties")
@EnableJpaRepositories("br.com.danielschiavo")
public class ShopFileStorageApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ShopFileStorageApplication.class);
    }
	
	public static void main(String[] args) throws IOException {
        org.springframework.core.io.Resource resource = new ClassPathResource("application.properties");
        Properties props = PropertiesLoaderUtils.loadProperties(resource);
        String activeProfile = props.getProperty("spring.profiles.active");
        String serverPort = props.getProperty("shopfilestorage.server.port");
        
        String additionalConfig = "classpath:/application-" + activeProfile + "-compartilhado.properties";
        
        new SpringApplicationBuilder(ShopFileStorageApplication.class)
                .properties("spring.config.additional-location=" + additionalConfig)
                .properties("server.port=" + serverPort)
                .run(args);
	}
	
}
