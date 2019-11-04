package com.example.todo.api.common.error;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

// REST APIで発生したエラー情報を保持するクラス
public class ApiError implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code;

    private String message;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final String target;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<ApiError> details = new ArrayList<>();

    public ApiError(String code, String message) {
        this(code, message, null);
    }

    public ApiError(String code, String message, String target) {
        this.code = code;
        this.message = message;
        this.target = target;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getTarget() {
        return target;
    }

    public List<ApiError> getDetails() {
        return details;
    }

    /**
     * detailsフィールドにApiErrorオブジェクトを追加する
     * @param detail
     */
    public void addDetails(ApiError detail) {
        this.details.add(detail);
    }
}
