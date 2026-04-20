package com.ecommerce.product.service;

import com.ecommerce.product.dto.*;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;

    public Page<ProductResponse> getAll(int page, int size) {
        return repository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::toResponse);
    }

    public ProductResponse getById(Long id) {
        return toResponse(repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id)));
    }

    public Page<ProductResponse> search(String keyword, String category,
                                        BigDecimal minPrice, BigDecimal maxPrice,
                                        int page, int size) {
        return repository.searchProducts(keyword, category, minPrice, maxPrice,
                PageRequest.of(page, size)).map(this::toResponse);
    }

    @Transactional
    public ProductResponse create(ProductRequest req, String sellerEmail) {
        Product product = Product.builder()
                .name(req.getName()).description(req.getDescription())
                .price(req.getPrice()).stockQuantity(req.getStockQuantity())
                .category(req.getCategory()).imageUrl(req.getImageUrl())
                .sku(req.getSku()).sellerEmail(sellerEmail)
                .status(Product.ProductStatus.ACTIVE).build();
        return toResponse(repository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest req, String sellerEmail) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        if (!product.getSellerEmail().equals(sellerEmail)) {
            throw new RuntimeException("Unauthorized to update this product");
        }
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setStockQuantity(req.getStockQuantity());
        product.setCategory(req.getCategory());
        product.setImageUrl(req.getImageUrl());
        return toResponse(repository.save(product));
    }

    @Transactional
    public void delete(Long id, String sellerEmail) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        if (!product.getSellerEmail().equals(sellerEmail)) {
            throw new RuntimeException("Unauthorized");
        }
        product.setStatus(Product.ProductStatus.INACTIVE);
        repository.save(product);
    }

    @Transactional
    public void decreaseStock(Long id, int quantity) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + id);
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        if (product.getStockQuantity() == 0) {
            product.setStatus(Product.ProductStatus.OUT_OF_STOCK);
        }
        repository.save(product);
    }

    private ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId()).name(p.getName()).description(p.getDescription())
                .price(p.getPrice()).stockQuantity(p.getStockQuantity())
                .category(p.getCategory()).imageUrl(p.getImageUrl()).sku(p.getSku())
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .sellerEmail(p.getSellerEmail()).createdAt(p.getCreatedAt()).build();
    }
}
