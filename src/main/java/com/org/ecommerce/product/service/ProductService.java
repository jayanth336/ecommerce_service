package com.org.ecommerce.product.service;

import com.org.ecommerce.common.client.InventoryClient;
import com.org.ecommerce.common.client.InventoryResponse;
import com.org.ecommerce.product.dto.ProductRequestDto;
import com.org.ecommerce.product.dto.ProductResponseDto;
import com.org.ecommerce.product.entity.Product;
import com.org.ecommerce.product.exception.ProductNotFoundException;
import com.org.ecommerce.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final InventoryClient inventoryClient;

    public ProductService(ProductRepository productRepository,  InventoryClient inventoryClient) {
        this.productRepository = productRepository;
        this.inventoryClient = inventoryClient;
    }

    public List<ProductResponseDto> getProducts() {
        List<Product> productsList = productRepository.findAll();
        return productsList.stream()
                .map(product -> mapToResponseDto(product))
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponseDto saveProduct(ProductRequestDto  requestDto) {
        Product product = mapToProduct(requestDto);
        Product savedProduct = productRepository.save(product);
        inventoryClient.createInventory(savedProduct.getId(), requestDto.getInitialStockQuantity());
        return mapToResponseDto(savedProduct);
    }

    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return mapToResponseDto(product);
    }

    public void deleteById(Long id) {
        boolean flag = productRepository.existsById(id);
        if(!flag) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    public ProductResponseDto updateProduct(Long id, ProductRequestDto requestDto) {
        Product oldProduct = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));

        oldProduct.setProductName(requestDto.getProductName());
        oldProduct.setBrand(requestDto.getBrand());
        oldProduct.setCategory(requestDto.getCategory());
        oldProduct.setDescription(requestDto.getDescription());
        oldProduct.setImageUrl(requestDto.getImageUrl());
        oldProduct.setPrice(requestDto.getPrice());
        oldProduct.setRating(requestDto.getRating());

        Product savedProduct = productRepository.save(oldProduct);
        return mapToResponseDto(savedProduct);
    }

    private ProductResponseDto mapToResponseDto(Product product) {
        ProductResponseDto productResponseDto = new ProductResponseDto();

        InventoryResponse inventory = inventoryClient.getInventory(product.getId());

        productResponseDto.setId(product.getId());
        productResponseDto.setProductName(product.getProductName());
        productResponseDto.setBrand(product.getBrand());
        productResponseDto.setCategory(product.getCategory());
        productResponseDto.setDescription(product.getDescription());
        productResponseDto.setImageUrl(product.getImageUrl());
        productResponseDto.setPrice(product.getPrice());
        productResponseDto.setStockQuantity(inventory.stockQuantity());
        productResponseDto.setRating(product.getRating());
        return  productResponseDto;
    }

    private Product mapToProduct(ProductRequestDto requestDto) {
        Product product = new Product();
        product.setProductName(requestDto.getProductName());
        product.setBrand(requestDto.getBrand());
        product.setCategory(requestDto.getCategory());
        product.setDescription(requestDto.getDescription());
        product.setImageUrl(requestDto.getImageUrl());
        product.setPrice(requestDto.getPrice());
        product.setRating(requestDto.getRating());
        return product;
    }
}
