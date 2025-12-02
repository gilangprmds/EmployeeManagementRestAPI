package com.gcompany.employeemanagement.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Response<T>{
    private String message;
    private T data;
}
