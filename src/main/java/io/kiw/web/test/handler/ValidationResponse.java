package io.kiw.web.test.handler;

public class ValidationResponse {
    public String name;
    public String email;
    public int age;
    public String city;
    public String page;
    public String userId;

    public ValidationResponse(String name, String email, int age, String city, String page, String userId) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.city = city;
        this.page = page;
        this.userId = userId;
    }
}
