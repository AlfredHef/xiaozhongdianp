package com.fudan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class ResponseDTO<T> {

    private Integer code; //编码
    private String msg; //错误信息
    private T data; //数据



}