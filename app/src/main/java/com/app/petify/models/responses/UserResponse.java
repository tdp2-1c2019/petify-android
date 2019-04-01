package com.app.petify.models.responses;

import com.app.petify.models.User;

public class UserResponse<T extends User>{

    public enum ServiceStatusCode {
        SUCCESS, UNAUTHORIZED, ERROR, CONFLICT
    }

    private ServiceStatusCode statusCode;
    private T serviceResponse;

    public UserResponse(ServiceStatusCode statusCode){
        this(statusCode, null);
    }

    public UserResponse(ServiceStatusCode statusCode, T serviceResponse){
        this.statusCode = statusCode;
        this.serviceResponse = serviceResponse;
    }

    public ServiceStatusCode getStatusCode() {
        return statusCode;
    }

    public T getServiceResponse(){
        return serviceResponse;
    }
}



