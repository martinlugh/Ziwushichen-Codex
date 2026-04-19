package com.example.meridian.common.response;

import com.example.meridian.common.constants.CommonConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 统一响应 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private Integer code;
    private String message;
    private T data;
    public static <T> ApiResponse<T> success(T data){ return new ApiResponse<>(CommonConstants.SUCCESS_CODE,"成功",data);}    
    public static <T> ApiResponse<T> fail(String msg){ return new ApiResponse<>(CommonConstants.ERROR_CODE,msg,null);}    
}
