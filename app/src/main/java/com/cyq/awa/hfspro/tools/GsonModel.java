package com.cyq.awa.hfspro.tools;

import com.google.gson.annotations.SerializedName;

public class GsonModel {
    // LoginRequest.java - 请求体模型
    public static class LoginRequest {
        @SerializedName("loginName")
        private String loginName;

        @SerializedName("password")
        private String password;

        @SerializedName("roleType")
        private int roleType;

        @SerializedName("loginType")
        private int loginType = 1; // 固定值

        @SerializedName("rememberMe")
        private int rememberMe = 1; // 固定值

        public LoginRequest(String loginName, String password, int s) {
            this.loginName = loginName;
            this.password = password;
            this.roleType = s;
        }
    }

    // LoginData.java - 登录数据模型
    public static class LoginData {
        @SerializedName("token")
        private String token;

        @SerializedName("userId")
        private String userId;

        @SerializedName("expiresIn")
        private long expiresIn;

        // 其他可能的字段...

        public String getToken() {
            return this.token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getUserId() {
            return this.userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public long getExpiresIn() {
            return this.expiresIn;
        }

        public void setExpiresIn(long expiresIn) {
            this.expiresIn = expiresIn;
        }
    }

    
    public static class LoginResponse extends ApiResponse<LoginData> {
        public boolean isSuccess() {
            return getCode() == 0;
        }
        
        public String getMessage() {
            return getMsg();
        }
    }

    // 通用的API响应类
    public static class ApiResponse<T> {
        @SerializedName("code")
        private int code;

        @SerializedName("msg")
        private String msg;

        @SerializedName("data")
        private T data;

        public int getCode() {
            return this.code;
        }

        public String getMsg() {
            return this.msg;
        }

        public T getData() {
            return this.data;
        }
        
        // 通用方法
        public boolean isSuccess() {
            return code == 0;
        }
    }
}