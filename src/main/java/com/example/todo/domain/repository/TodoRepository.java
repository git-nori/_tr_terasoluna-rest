package com.example.todo.domain.repository;

import java.util.Collection;
import java.util.Optional;

import com.example.todo.domain.model.Todo;

public interface TodoRepository {
    Optional<Todo> findById(String todoId);

    Collection<Todo> findAll();

    void create(Todo todo);

    boolean updateById(Todo todo);

    void deleteById(Todo todo);

    long countByFinished(boolean finished); // 完了状態がxxである件数を取得する
}
