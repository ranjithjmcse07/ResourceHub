package com.toolshare.service;

import com.toolshare.dao.UserDao;
import com.toolshare.exception.ResourceNotFoundException;
import com.toolshare.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User getUserByUsername(String username) {
        return userDao.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    public User getUserById(Long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User updateProfile(String username, User updateData) {
        User existing = getUserByUsername(username);
        existing.setFullName(updateData.getFullName());
        existing.setMobile(updateData.getMobile());
        existing.setCompanyName(updateData.getCompanyName());
        existing.setAddress(updateData.getAddress());
        userDao.updateProfile(existing);
        return existing;
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public void updateUserStatus(Long id, boolean active) {
        getUserById(id); // verify exists
        userDao.updateStatus(id, active);
    }
}
