package com.example.demo;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncContextPropagationService {
    @Async("asyncContextPropagationExecutor")
    public CompletableFuture<PetsController.Rows> createPetWithAsyncAnnotation(String name) {
        Integer rowsCreated = DatabaseHelper.createPetByName(name);
        return CompletableFuture.completedFuture(new PetsController.Rows(rowsCreated));
    }
}
