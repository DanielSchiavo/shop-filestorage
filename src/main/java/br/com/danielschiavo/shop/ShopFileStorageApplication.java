package br.com.danielschiavo.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
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
	
	public static void main(String[] args) {
		String diretorioRaiz = System.getProperty("user.dir");
		System.out.println("O diretório raiz do seu código Java é: " + diretorioRaiz);
		
		SpringApplication.run(ShopFileStorageApplication.class, args);
	}
	
}
