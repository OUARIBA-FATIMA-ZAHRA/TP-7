package com.example;

import com.example.service.DataInitService;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class App {

    public static void main(String[] args) {

        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("hibernate-performance");

        DataInitService dataInit = new DataInitService(emf);
        dataInit.initData();

        emf.close();
    }
}
