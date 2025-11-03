package com.bbey.neez.service;

public interface LoginService {
    // User login, returns a token if successful
    String login(String username, String password);

    // User logout, invalidates the token
    String logout(String token);

    // Get user information based on the token
    String getUserInfo(String token);
}