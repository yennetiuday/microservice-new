package com.uday.inventoryservice;

import com.uday.inventoryservice.mocel.Inventory;
import com.uday.inventoryservice.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaClient
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadData(InventoryRepository inventoryRepository) {
		return args -> {
			Inventory inventory = new Inventory();
			inventory.setSkuCode("iPhone_14");
			inventory.setQuantity(100);
			inventoryRepository.save(inventory);

			Inventory inventory1 = new Inventory();
			inventory1.setSkuCode("OnePlus7");
			inventory1.setQuantity(100);
			inventoryRepository.save(inventory1);

			Inventory inventory2 = new Inventory();
			inventory2.setSkuCode("iPhone_14_red");
			inventory2.setQuantity(0);
			inventoryRepository.save(inventory2);
		};
	}

}
