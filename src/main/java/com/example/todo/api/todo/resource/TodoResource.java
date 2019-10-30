package com.example.todo.api.todo.resource;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

// REST APIの入出力となるJSON(またはXML)を表現するJava Bean
public class TodoResource implements Serializable{

    private static final long serialVersionUID = 1L;

    private String todoId;

    @NotNull
    @Size(min=1, max=30)
    private String todoTitle;

    private boolean finished;

    private Date createdAt;

    public String getTodoId() {
        return todoId;
    }

    public String getTodoTitle() {
        return todoTitle;
    }

    public boolean isFinished() {
        return finished;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setTodoId(String todoId) {
        this.todoId = todoId;
    }

    public void setTodoTitle(String todoTitle) {
        this.todoTitle = todoTitle;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
