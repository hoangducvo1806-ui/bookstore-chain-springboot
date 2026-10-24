package com.bookstore.bookstore.service.impl;

import com.bookstore.bookstore.dto.AddressDto;
import com.bookstore.bookstore.entity.Address;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.repository.AddressRepository;
import com.bookstore.bookstore.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Address> getAddressesByUser(User user) {
        return addressRepository.findByUserOrderByIdDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Address getAddressById(Long id, User user) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Địa chỉ không tồn tại!"));
        if (!address.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền truy cập địa chỉ này!");
        }
        return address;
    }

    @Override
    @Transactional(readOnly = true)
    public Address getDefaultAddress(User user) {
        return addressRepository.findByUserAndIsDefaultTrue(user)
                .orElseGet(() -> {
                    List<Address> list = addressRepository.findByUserOrderByIdDesc(user);
                    return list.isEmpty() ? null : list.get(0);
                });
    }

    @Override
    public Address createAddress(User user, AddressDto dto) {
        List<Address> currentList = addressRepository.findByUserOrderByIdDesc(user);
        boolean shouldBeDefault = currentList.isEmpty() || Boolean.TRUE.equals(dto.getIsDefault());

        if (shouldBeDefault && !currentList.isEmpty()) {
            addressRepository.resetDefaultAddressForUser(user);
        }

        Address address = Address.builder()
                .user(user)
                .recipientName(dto.getRecipientName().trim())
                .phone(dto.getPhone().trim())
                .streetAddress(dto.getStreetAddress().trim())
                .ward(dto.getWard().trim())
                .district(dto.getDistrict().trim())
                .province(dto.getProvince().trim())
                .isDefault(shouldBeDefault)
                .build();

        return addressRepository.save(address);
    }

    @Override
    public Address updateAddress(User user, Long id, AddressDto dto) {
        Address address = getAddressById(id, user);

        if (Boolean.TRUE.equals(dto.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.resetDefaultAddressForUser(user);
            address.setIsDefault(true);
        }

        address.setRecipientName(dto.getRecipientName().trim());
        address.setPhone(dto.getPhone().trim());
        address.setStreetAddress(dto.getStreetAddress().trim());
        address.setWard(dto.getWard().trim());
        address.setDistrict(dto.getDistrict().trim());
        address.setProvince(dto.getProvince().trim());

        return addressRepository.save(address);
    }

    @Override
    public void deleteAddress(User user, Long id) {
        Address address = getAddressById(id, user);
        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        addressRepository.delete(address);

        if (wasDefault) {
            List<Address> remaining = addressRepository.findByUserOrderByIdDesc(user);
            if (!remaining.isEmpty()) {
                Address newDefault = remaining.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
            }
        }
    }

    @Override
    public void setDefaultAddress(User user, Long id) {
        Address address = getAddressById(id, user);
        addressRepository.resetDefaultAddressForUser(user);
        address.setIsDefault(true);
        addressRepository.save(address);
    }
}
