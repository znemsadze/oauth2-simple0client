package ge.magticom.oauth.config;

import ge.magticom.oauth.ApplicationRoot;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootApplication
@ComponentScan(basePackageClasses = { ApplicationRoot.class })
@EnableAutoConfiguration
@EnableScheduling
public @interface BaseBootConfig {

}
