package com.example.todo.domain.service;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.terasoluna.gfw.common.exception.BusinessException;
import org.terasoluna.gfw.common.exception.ResourceNotFoundException;
import org.terasoluna.gfw.common.message.ResultMessage;
import org.terasoluna.gfw.common.message.ResultMessages;

import com.example.todo.domain.model.Todo;
import com.example.todo.domain.repository.TodoRepository;

@Service
@Transactional
/*
 * メソッド開始時にトランザクションの開始、メソッド正常終了時にトランザクションのコミットを行う
 * 途中で非検査例外が発生した場合は、トランザクションがロールバックする
 */
public class TodoServiceImpl implements TodoService {

    private static final long MAX_UNFINISHED_COUNT = 5;

    @Autowired
    TodoRepository todoRepository;

    @Override
    @Transactional(readOnly = true)
    public Todo findOne(String todoId) {
        Todo todo = todoRepository.findById(todoId).orElse(null);
        if (todo == null) {
            ResultMessages messages = ResultMessages.error(); // 結果メッセージを格納するクラスを用いてエラーメッセージを例外に追加する
            final String ERROR_MSG = "[E404] The requested Todo is not found. (id= %s)";
            messages.add(ResultMessage.fromText(String.format(ERROR_MSG, todoId)));
            throw new ResourceNotFoundException(messages);
        }
        return todo;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Todo> findAll() {
        return todoRepository.findAll();
    }

    @Override
    public Todo create(Todo todo) {
        long unfinishedCount = todoRepository.countByFinished(false);
        if (unfinishedCount >= MAX_UNFINISHED_COUNT) {
            // unfinishedCount >= MAX_UNFINISHED_COUNTの場合BusinessExceptionをthrowする
            ResultMessages messages = ResultMessages.error();
            final String ERROR_MSG = "[E001] The count of un-finished Todo must not be over %s .";
            messages.add(ResultMessage.fromText(String.format(ERROR_MSG, MAX_UNFINISHED_COUNT)));
            throw new BusinessException(messages);
        }

        String todoId = UUID.randomUUID().toString();
        Date createdAt = new Date();
        todo.setTodoId(todoId);
        todo.setCreatedAt(createdAt);
        todo.setFinished(false);

        todoRepository.create(todo);
        return todo;
    }

    @Override
    public Todo finish(String todoId) {
        Todo todo = findOne(todoId);
        if (todo.isFinished()) {
            ResultMessages messages = ResultMessages.error();
            final String ERROR_MSG = "[E002] The requested Todo is already finished. (id= %s)";
            messages.add(String.format(ERROR_MSG, todoId));
            throw new BusinessException(messages);
        }

        todo.setFinished(true);
        todoRepository.updateById(todo);
        return todo;
    }

    @Override
    public void delete(String todoId) {
        Todo todo = findOne(todoId);
        todoRepository.deleteById(todo);
    }
}
