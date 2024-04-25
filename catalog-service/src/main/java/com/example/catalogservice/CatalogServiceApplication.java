package com.example.catalogservice;

import com.example.catalogservice.entity.CatalogEntity;
import com.example.catalogservice.repository.CatalogRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@RequiredArgsConstructor
public class CatalogServiceApplication {

    private final CatalogRepository catalogRepository;

    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }

    @PostConstruct
    public void init() {

        CatalogEntity catalog1 = new CatalogEntity("CATALOG-001", "Berlin", 100, 1500);
        CatalogEntity catalog2 = new CatalogEntity("CATALOG-002","Tokyo", 110,1000);
        CatalogEntity catalog3 = new CatalogEntity("CATALOG-003","Stockholm", 120,2000);

        catalogRepository.save(catalog1);
        catalogRepository.save(catalog2);
        catalogRepository.save(catalog3);
    }

}
