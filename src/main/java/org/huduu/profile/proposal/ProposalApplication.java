package org.huduu.profile.proposal;

import org.huduu.common.logging.EnableLoggingAspect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

@EnableDiscoveryClient
@SpringBootApplication
@EnableLoggingAspect
@EnableJpaRepositories(basePackages = "org.huduu.profile.repository")
@EntityScan(basePackages= "org.huduu.profile.domain")
@ComponentScan(basePackages = "org.huduu.profile, org.huduu.common")
public class ProposalApplication {
    public static void main(String [] args) {
        SpringApplication.run(ProposalApplication.class, args);
    }

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
