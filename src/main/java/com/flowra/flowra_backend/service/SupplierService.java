package com.flowra.flowra_backend.service;

import com.flowra.flowra_backend.dto.SupplierDTO;
import com.flowra.flowra_backend.entity.Supplier;
import com.flowra.flowra_backend.exception.BadRequestException;
import com.flowra.flowra_backend.exception.ResourceNotFoundException;
import com.flowra.flowra_backend.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Transactional(readOnly = true)
    public List<SupplierDTO.Response> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupplierDTO.Response getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        return mapToResponse(supplier);
    }

    @Transactional
    public SupplierDTO.Response createSupplier(SupplierDTO.Request request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Supplier name is mandatory");
        }
        String name = request.getName().trim();
        Supplier supplier = supplierRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> Supplier.builder()
                        .name(name)
                        .contactPerson(request.getContactPerson())
                        .phone(request.getPhone())
                        .email(request.getEmail())
                        .address(request.getAddress())
                        .build());

        Supplier saved = supplierRepository.save(supplier);
        return mapToResponse(saved);
    }

    @Transactional
    public Supplier findOrCreateSupplier(String name) {
        if (name == null || name.trim().isEmpty()) {
            name = "Direct Mandi Grower";
        }
        final String finalName = name.trim();
        return supplierRepository.findByNameIgnoreCase(finalName)
                .orElseGet(() -> supplierRepository.save(Supplier.builder()
                        .name(finalName)
                        .contactPerson("Grower Rep")
                        .phone("+91 98000 00000")
                        .email("info@" + finalName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase() + ".com")
                        .address("Mandi Wholesale Depot")
                        .build()));
    }

    public SupplierDTO.Response mapToResponse(Supplier supplier) {
        return SupplierDTO.Response.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .contactPerson(supplier.getContactPerson())
                .phone(supplier.getPhone())
                .email(supplier.getEmail())
                .address(supplier.getAddress())
                .createdAt(supplier.getCreatedAt())
                .build();
    }
}
