package com.bbey.neez.service;

import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {
    @Override
    public String login(String username, String password) {
        // Implementation of user login
        return null;
    }

    @Override
    public String logout(String token) {
        // Implementation of user logout
        return null;
    }

    @Override
    public String getUserInfo(String token) {
        // Implementation to get user information based on the token
        return null;
    }
}