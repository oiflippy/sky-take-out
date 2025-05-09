package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }


        /**
         * 处理SQL完整性约束异常（如唯一索引冲突）
         * @param ex SQL完整性约束异常对象，包含数据库约束冲突的详细信息
         * @return 返回封装后的错误结果对象，包含具体的错误提示信息
         */
        @ExceptionHandler
        public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
            // 获取异常原始信息
            String message = ex.getMessage();
            
            // 处理MySQL唯一约束冲突异常
            if(message.contains("Duplicate entry")){
                // 解析异常信息获取重复字段值
                String[] split = message.split(" ");
                String username = split[2];
                String msg = username + MessageConstant.ALREADY_EXISTS;
                return Result.error(msg);
            }else{
                // 处理其他未知数据库异常
                return Result.error(MessageConstant.UNKNOWN_ERROR);
            }
        }

}
