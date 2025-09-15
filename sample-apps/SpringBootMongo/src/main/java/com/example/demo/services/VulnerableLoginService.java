package com.example.demo.services;

import com.example.demo.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class VulnerableLoginService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public boolean authenticate(String email, Object password) {
        // Vulnerable query construction
        Query query = new Query();
        query.addCriteria(Criteria.where("email").is(email).and("password").is(password));
        return mongoTemplate.exists(query, User.class);
    }
}
