package com.example.demo;
import java.util.List;

public class TopUsersResponse {
    private List<User> topUsers;

    public List<User> getTopUsers() {
        return topUsers;
    }

    public void setTopUsers(List<User> topUsers) {
        this.topUsers = topUsers;
    }
}
