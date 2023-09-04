package com.apes.inventoryservice.service;

import com.apes.inventoryservice.dto.InventoryResponse;
import com.apes.inventoryservice.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository repository;

    public InventoryService(InventoryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCodes) {
        return repository.findBySkuCodeIn(skuCodes)
                .stream()
                .map(InventoryResponse::convert)
                .toList();
    }
}
