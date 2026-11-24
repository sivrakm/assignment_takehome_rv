package com.crio.rentvideo.dto;

import com.crio.rentvideo.entity.Role;

public class LoginResponse {

    private String message;
    private String email;
    private Role role;

    public LoginResponse() {
    }

    public LoginResponse(String message, String email, Role role) {
        this.message = message;
        this.email = email;
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
