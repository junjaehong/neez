package com.bbey.neez.service;

public interface AuthService {
    String register(String userId, String password, String name, String email);
    String login(String userId, String password);
}
