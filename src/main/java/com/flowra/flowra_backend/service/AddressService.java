package com.flowra.flowra_backend.service;

import com.flowra.flowra_backend.dto.AddressDTO;
import com.flowra.flowra_backend.entity.Address;
import com.flowra.flowra_backend.entity.User;
import com.flowra.flowra_backend.exception.ResourceNotFoundException;
import com.flowra.flowra_backend.repository.AddressRepository;
import com.flowra.flowra_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional
    public AddressDTO.Response createAddress(AddressDTO.Request request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        // If marked as default, unset previous defaults
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .ifPresent(existingDefault -> {
                        existingDefault.setIsDefault(false);
                        addressRepository.save(existingDefault);
                    });
        }

        Address address = Address.builder()
                .user(user)
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity() != null ? request.getCity() : "Mumbai")
                .state(request.getState() != null ? request.getState() : "Maharashtra")
                .pincode(request.getPincode())
                .country(request.getCountry() != null ? request.getCountry() : "India")
                .addressType(request.getAddressType() != null ? request.getAddressType() : "Home")
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .build();

        Address saved = addressRepository.save(address);
        return mapToResponse(saved);
    }

    public List<AddressDTO.Response> getAddressesByUser(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AddressDTO.Response getAddressById(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
        return mapToResponse(address);
    }

    @Transactional
    public AddressDTO.Response updateAddress(Long id, AddressDTO.Request request) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));

        if (request.getRecipientName() != null) address.setRecipientName(request.getRecipientName());
        if (request.getRecipientPhone() != null) address.setRecipientPhone(request.getRecipientPhone());
        if (request.getAddressLine1() != null) address.setAddressLine1(request.getAddressLine1());
        if (request.getAddressLine2() != null) address.setAddressLine2(request.getAddressLine2());
        if (request.getCity() != null) address.setCity(request.getCity());
        if (request.getState() != null) address.setState(request.getState());
        if (request.getPincode() != null) address.setPincode(request.getPincode());
        if (request.getCountry() != null) address.setCountry(request.getCountry());
        if (request.getAddressType() != null) address.setAddressType(request.getAddressType());

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.findByUserIdAndIsDefaultTrue(address.getUser().getId())
                    .filter(a -> !a.getId().equals(address.getId()))
                    .ifPresent(prevDefault -> {
                        prevDefault.setIsDefault(false);
                        addressRepository.save(prevDefault);
                    });
            address.setIsDefault(true);
        }

        Address updated = addressRepository.save(address);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteAddress(Long id) {
        if (!addressRepository.existsById(id)) {
            throw new ResourceNotFoundException("Address not found with id: " + id);
        }
        addressRepository.deleteById(id);
    }

    public AddressDTO.Response mapToResponse(Address address) {
        return AddressDTO.Response.builder()
                .id(address.getId())
                .userId(address.getUser().getId())
                .recipientName(address.getRecipientName())
                .recipientPhone(address.getRecipientPhone())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .country(address.getCountry())
                .addressType(address.getAddressType())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .build();
    }
}
