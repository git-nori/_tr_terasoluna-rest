package com.example.todo.api.common.error;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// Restの例外処理のためResponseEntityExceptionHandlerを継承
@ControllerAdvice
public class RestGlobalExceptionHandler extends ResponseEntityExceptionHandler{

    @Autowired
    MessageSource messageSource;

    // すべてのハンドリングに共通する処理を挟みたい場合はオーバーライドする
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request){
        Object responseBody = body;
        if(body == null) {
            responseBody = createApiError(request, "E999", ex.getMessage());
        }
        return ResponseEntity.status(status).headers(headers).body(responseBody);
    }

    /**
     * ApiErrorオブジェクトのインスタンスを生成し、返す
     * @param request
     * @param errorCode
     * @param args
     * @return ApiErrorオブジェクト
     */
    private ApiError createApiError(WebRequest request, String errorCode, Object...args) {
        // request.getLocale => Accept-Language ヘッダを元に、クライアントがコンテンツを表示できると想定される Locale を返す ex. Locale.JAPAN
        return new ApiError(errorCode, messageSource.getMessage(errorCode, args, request.getLocale()));
    }
}
