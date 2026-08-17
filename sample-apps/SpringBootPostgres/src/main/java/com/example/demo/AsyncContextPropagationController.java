package com.example.demo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.*;

@RestController
@RequestMapping("/api/pets/create/async")
public class AsyncContextPropagationController {
    private final Executor springExecutor;
    private final AsyncContextPropagationService asyncContextPropagationService;

    public AsyncContextPropagationController(
        @Qualifier("asyncContextPropagationExecutor") Executor springExecutor,
        AsyncContextPropagationService asyncContextPropagationService
    ) {
        this.springExecutor = springExecutor;
        this.asyncContextPropagationService = asyncContextPropagationService;
    }

    private record PetCreate(String name) {}

    @PostMapping(
        path = "/completable-future-single",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public PetsController.Rows completableFutureSingle(@RequestBody PetCreate pet) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            return CompletableFuture
                .supplyAsync(() -> createPet(pet.name()), executor)
                .get();
        } finally {
            executor.shutdown();
        }
    }

    @PostMapping(
        path = "/submit-callable",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public PetsController.Rows submitCallable(@RequestBody PetCreate pet) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            return executor.submit(() -> createPet(pet.name())).get();
        } finally {
            executor.shutdown();
        }
    }

    @PostMapping(
        path = "/thread-pool-execute",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public PetsController.Rows threadPoolExecute(@RequestBody PetCreate pet) throws Exception {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        try {
            CompletableFuture<PetsController.Rows> future = new CompletableFuture<>();
            executor.execute(() -> {
                try {
                    future.complete(createPet(pet.name()));
                } catch (Throwable throwable) {
                    future.completeExceptionally(throwable);
                }
            });
            return future.get();
        } finally {
            executor.shutdown();
        }
    }

    @PostMapping(
        path = "/fork-join-submit",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public PetsController.Rows forkJoinSubmit(@RequestBody PetCreate pet) throws Exception {
        return ForkJoinPool.commonPool()
            .submit(() -> createPet(pet.name()))
            .get();
    }

    @PostMapping(
        path = "/scheduled-callable",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public PetsController.Rows scheduledCallable(@RequestBody PetCreate pet) throws Exception {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        try {
            return executor.schedule(
                () -> createPet(pet.name()),
                1,
                TimeUnit.MILLISECONDS
            ).get();
        } finally {
            executor.shutdown();
        }
    }

    @PostMapping(
        path = "/spring-task-executor",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public PetsController.Rows springTaskExecutor(@RequestBody PetCreate pet) throws Exception {
        CompletableFuture<PetsController.Rows> future = new CompletableFuture<>();
        springExecutor.execute(() -> {
            try {
                future.complete(createPet(pet.name()));
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future.get();
    }

    @PostMapping(
        path = "/spring-async-annotation",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public PetsController.Rows springAsyncAnnotation(@RequestBody PetCreate pet) throws Exception {
        return asyncContextPropagationService
            .createPetWithAsyncAnnotation(pet.name())
            .get();
    }

    private PetsController.Rows createPet(String name) {
        Integer rowsCreated = DatabaseHelper.createPetByName(name);
        return new PetsController.Rows(rowsCreated);
    }
}
