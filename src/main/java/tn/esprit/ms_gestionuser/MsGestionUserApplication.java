package tn.esprit.ms_gestionuser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MsGestionUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsGestionUserApplication.class, args);
    }

}
