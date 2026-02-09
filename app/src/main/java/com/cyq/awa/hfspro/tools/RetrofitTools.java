package com.cyq.awa.hfspro.tools;

import com.cyq.awa.hfspro.tools.GsonModel.LoginResponse;
import com.cyq.awa.hfspro.tools.GsonModel.LoginRequest;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class RetrofitTools {
    public interface ApiService {
        @POST("v2/users/sessions")
        Call<LoginResponse> login(@Body LoginRequest request);
    }

    public static class RetrofitClient {
        private static final String BASE_URL = "https://hfs-be.yunxiao.com/";
        private static Retrofit retrofit = null;
        
        // 调试标志 - 设置为true时忽略SSL证书验证
        private static final boolean DEBUG_MODE = true;

        // 创建不验证SSL的OkHttpClient（仅用于调试）
        private static OkHttpClient getUnsafeOkHttpClient() {
            try {
                // 创建一个信任所有证书的TrustManager
                final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) 
                            throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) 
                            throws CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
                };

                // 安装信任所有证书的SSLContext
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                
                // 创建SSLSocketFactory
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
                builder.hostnameVerifier((hostname, session) -> true); // 信任所有主机名

                return builder.build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public static Retrofit getClient() {
            if (retrofit == null) {
                OkHttpClient okHttpClient;
                
                if (DEBUG_MODE) {
                    // 调试模式：不验证SSL证书
                    okHttpClient = getUnsafeOkHttpClient();
                } else {
                    // 生产模式：正常验证SSL证书
                    okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .addInterceptor(chain -> {
                                okhttp3.Request original = chain.request();
                                okhttp3.Request request = original
                                        .newBuilder()
                                        .header("Content-Type", "application/json")
                                        .header("Accept", "application/json")
                                        .method(original.method(), original.body())
                                        .build();
                                return chain.proceed(request);
                            })
                            .build();
                }

                // 创建Retrofit实例
                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            return retrofit;
        }

        public static ApiService getAuthService() {
            return getClient().create(ApiService.class);
        }
    }
}