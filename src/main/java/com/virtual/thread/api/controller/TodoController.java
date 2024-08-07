package com.virtual.thread.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/api")
@Slf4j
public class TodoController {


    @GetMapping(value = "/todos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTodos() {
        log.info("GetTodos Running on {} ", Thread.currentThread());
        return RestClient.create()
                .get()
                .uri("https://jsonplaceholder.typicode.com/todos/")
                .retrieve()
                .toEntity(String.class);
    }


    @GetMapping(value = "/todos/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTodoById(@PathVariable int id) {
        log.info("GetTodoById Running on {} ", Thread.currentThread());
        return RestClient.create("https://jsonplaceholder.typicode.com/todos")
                .get()
                .uri("/" + id)
                .retrieve()
                .toEntity(String.class);
    }

    @DeleteMapping(value = "/todos/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteTodo(@PathVariable int id) {
        Thread.startVirtualThread(() -> {
            log.info("DeleteTodoById Running on {} ", Thread.currentThread());
            RestClient.create("https://jsonplaceholder.typicode.com/todos")
                    .delete()
                    .uri("/" + id)
                    .retrieve()
                    .toEntity(String.class);
        });
    }

    @GetMapping(value = "/todos/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTodoList() throws Exception {
        ExecutorService executorService = Executors.newScheduledThreadPool(0, Thread.ofVirtual().factory());
        Future<ResponseEntity<String>> future = executorService.submit(() -> {
            log.info("getTodoList Running on {} ", Thread.currentThread());
            return RestClient.create()
                    .get()
                    .uri("https://jsonplaceholder.typicode.com/todos/")
                    .retrieve()
                    .toEntity(String.class);
        });
        return future.get();
    }


    @GetMapping(value = "/todoDataList", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTodoDataList() {
        CompletableFuture<ResponseEntity<String>> future = CompletableFuture.supplyAsync(() -> {
            log.info("GetTodoDataList Running task on virtual thread: {}", Thread.currentThread().getName());

            ResponseEntity<String> response = RestClient.create()
                    .get()
                    .uri("https://jsonplaceholder.typicode.com/todos/")
                    .retrieve()
                    .toEntity(String.class);

            log.info("GetTodoDataList Task completed on virtual thread: {}", Thread.currentThread().getName());
            return response;
        }, Executors.newScheduledThreadPool(0, Thread.ofVirtual().factory()));

        // Handle completion of the asynchronous task
        future.thenRun(() -> log.info("Asynchronous task completed."));

        // Handle completion of the asynchronous task and return the data
        ResponseEntity<String> todoList = future.join();

        return new ResponseEntity<>(todoList.getBody(), HttpStatus.OK);
    }

    @GetMapping(value = "/getTodoDummyDataList", produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<ResponseEntity<String>> getTodoDummyDataList() {
        DeferredResult<ResponseEntity<String>> deferredResult = new DeferredResult<>();
        CompletableFuture<ResponseEntity<String>> future = CompletableFuture.supplyAsync(() -> {
            log.info("GetTodoDummyDataList Running task on virtual thread: {}", Thread.currentThread().getName());
            // Simulate fetching data
            ResponseEntity<String> response = RestClient.create()
                    .get()
                    .uri("https://jsonplaceholder.typicode.com/todos/")
                    .retrieve()
                    .toEntity(String.class);
            log.info("GetTodoDummyDataList Task completed on virtual thread: {}", Thread.currentThread().getName());
            return response;
        }, Executors.newScheduledThreadPool(0, Thread.ofVirtual().factory()));

        // Complete the deferred result when the async task is done
        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                deferredResult.setErrorResult(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
            } else {
                deferredResult.setResult(new ResponseEntity<String>(result.getBody(), HttpStatus.OK));
            }
        });

        return deferredResult;
    }

    @GetMapping(value = "/getDummyDataList", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getDataList() throws Exception {
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        Future<ResponseEntity<String>> future = executorService.submit(() -> {
            log.info("Running task on thread: {}", Thread.currentThread().getName());
            return RestClient.create()
                    .get()
                    .uri("https://jsonplaceholder.typicode.com/todos/")
                    .retrieve()
                    .toEntity(String.class);
        });
        return future.get();
    }


}
