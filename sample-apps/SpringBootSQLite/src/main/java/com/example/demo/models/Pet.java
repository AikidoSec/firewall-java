package com.example.demo.models;
/*
Creating this table using SQL :
CREATE TABLE pets (
    pet_id INTEGER PRIMARY KEY AUTOINCREMENT,
    pet_name TEXT NOT NULL,
    owner TEXT NOT NULL
);
 */
public record Pet(Integer pet_id, String name, String owner) {}