package com.example.todo.api.common.error;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.terasoluna.gfw.common.exception.BusinessException;
import org.terasoluna.gfw.common.exception.ResourceNotFoundException;
import org.terasoluna.gfw.common.exception.ResultMessagesNotificationException;
import org.terasoluna.gfw.common.message.ResultMessage;

// Restの例外処理のためResponseEntityExceptionHandlerを継承
@ControllerAdvice
public class RestGlobalExceptionHandler extends ResponseEntityExceptionHandler {
    /*
     * 入力エラーの種類
     * org.springframework.web.bind.MethodArgumentNotValidException
     * org.springframework.validation.BindException
     * org.springframework.http.converter.HttpMessageNotReadableException
     * org.springframework.beans.TypeMismatchException
     */

    @Autowired
    MessageSource messageSource;

    // すべてのハンドリングに共通する処理を挟みたい場合はオーバーライドする
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        Object responseBody = body;
        if (body == null) {
            responseBody = createApiError(request, "E999", ex.getMessage());
        }
        return ResponseEntity.status(status).headers(headers).body(responseBody);
    }

    // // HTTPリクエストBODYに格納されているデータに入力エラーがあった場合に発生する例外処理
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ApiError apiError = createApiError(request, "E400");
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            apiError.addDetails(createApiError(request, fieldError, fieldError.getField()));
        }
        for (ObjectError objectError : ex.getBindingResult().getGlobalErrors()) {
            apiError.addDetails(createApiError(request, objectError, objectError.getObjectName()));
        }
        return handleExceptionInternal(ex, apiError, headers, status, request);
    }

    // BusinessExceptionが発生した際の例外処理
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(BusinessException ex, WebRequest request) {
        return handleResultMessagesNotificationException(ex, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    private ResponseEntity<Object> handleResultMessagesNotificationException(ResultMessagesNotificationException ex,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        // ResultMessagesNotificationException => BusinessException, ResourceNotFoundException例外の親クラス
        // 内部にResultMessageを持つ
        ResultMessage message = ex.getResultMessages().iterator().next();
        ApiError apiError = createApiError(request, message.getCode(), message.getArgs());
        return handleExceptionInternal(ex, apiError, headers, status, request);
    }

    // ResourceNotFoundExceptionが発生した際の例外処理
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request){
        return handleResultMessagesNotificationException(ex, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    // SystemExceptionが発生した際の例外処理
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleSystemError(ResourceNotFoundException ex, WebRequest request){
        ApiError apiError = createApiError(request, "E500");
        return handleExceptionInternal(ex, apiError, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * ApiErrorオブジェクトのインスタンスを生成し、返す
     * @param request
     * @param errorCode
     * @param args
     * @return ApiErrorオブジェクト
     */
    private ApiError createApiError(WebRequest request, String errorCode, Object... args) {
        // request.getLocale => Accept-Language ヘッダを元に、クライアントがコンテンツを表示できると想定される Locale を返す ex. Locale.JAPAN
        return new ApiError(errorCode, messageSource.getMessage(errorCode, args, request.getLocale()));
    }

    /**
     * メッセージプロパティからメッセージを取得し、ApiErrorオブジェクトのインスタンスを生成し、返す
     * @param request
     * @param messageSourceResolvable
     * @param target
     * @return
     */
    private ApiError createApiError(WebRequest request, DefaultMessageSourceResolvable messageSourceResolvable,
            String target) {
        // DefaultMessageSourceResolvable => 「メッセージに埋め込む値」をプロパティファイルから取得
        return new ApiError(messageSourceResolvable.getCode(),
                messageSource.getMessage(messageSourceResolvable, request.getLocale()), target);
    }
}
