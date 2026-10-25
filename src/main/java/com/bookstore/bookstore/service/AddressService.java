package com.bookstore.bookstore.service;

import com.bookstore.bookstore.dto.AddressDto;
import com.bookstore.bookstore.entity.Address;
import com.bookstore.bookstore.entity.User;

import java.util.List;

public interface AddressService {

    List<Address> getAddressesByUser(User user);

    Address getAddressById(Long id, User user);

    Address getDefaultAddress(User user);

    Address createAddress(User user, AddressDto dto);

    Address updateAddress(User user, Long id, AddressDto dto);

    void deleteAddress(User user, Long id);

    void setDefaultAddress(User user, Long id);
}
