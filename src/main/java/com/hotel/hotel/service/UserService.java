package com.hotel.hotel.service;

import com.hotel.hotel.model.User;

import java.util.List;

public interface UserService {
    User registerUser(User user);
    List<User> getUsers();
    void deleteUser(String email);
    User getUser(String email);

}
