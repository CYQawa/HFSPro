package com.cyq.awa.hfspro.tools.network;

import android.content.Context;
import com.cyq.awa.hfspro.tools.network.GsonModel.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import com.cyq.awa.hfspro.tools.MyDatabases.DatabaseManager;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class RetrofitTools {
  private static Context appContext;

  // 初始化上下文（需要在 Application 中调用）
  public static void init(Context context) {
    appContext = context.getApplicationContext();
  }

  public interface ApiService {
    @POST("v2/users/sessions")
    Call<ApiResponse<LoginData>> login(@Body LoginRequest request);

    @GET("v3/exam/list?start=0&limit=10")
    Call<ApiResponse<ExamListData>> getExamList();

    @GET("v3/exam/{examId}/overview")
    Call<ApiResponse<ExamOverviewData>> getExamOverview(@Path("examId") long examId);

    @GET("v4/exam/overview")
    Call<ApiResponse<CompareRankData>> getCompareRank(@Query("examId") long examId);
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
        final TrustManager[] trustAllCerts =
            new TrustManager[] {
              new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException {}

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException {}

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                  return new X509Certificate[] {};
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
        builder.addInterceptor(getTokenInterceptor()); // 添加token拦截器

        return builder.build();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    // 创建带token拦截器的OkHttpClient
    private static OkHttpClient getSafeOkHttpClient() {
      OkHttpClient.Builder builder =
          new OkHttpClient.Builder()
              .connectTimeout(30, TimeUnit.SECONDS)
              .readTimeout(30, TimeUnit.SECONDS)
              .writeTimeout(30, TimeUnit.SECONDS)
              .addInterceptor(getTokenInterceptor()); // 添加token拦截器

      return builder.build();
    }

    // Token拦截器
    private static okhttp3.Interceptor getTokenInterceptor() {
      return chain -> {
        okhttp3.Request original = chain.request();

        // 获取请求路径
        String path = original.url().encodedPath();
        // 如果是登录请求，不添加token
        if (path.equals("/v2/users/sessions")) {
          // 登录请求只需要基础header
          okhttp3.Request request =
              original
                  .newBuilder()
                  .header("Content-Type", "application/json")
                  .header("Accept", "application/json")
                  .method(original.method(), original.body())
                  .build();
          return chain.proceed(request);
        } else {
          // 其他请求需要添加token
          String token = null;
          if (appContext != null) {
            DatabaseManager dbm = DatabaseManager.getInstance();
            token = dbm.getToken();
          }

          okhttp3.Request.Builder requestBuilder =
              original
                  .newBuilder()
                  .header("Content-Type", "application/json")
                  .header("Accept", "application/json");

          // 如果有token，添加hfs-token头
          if (token != null && !token.isEmpty()) {
            requestBuilder.header("hfs-token", token);
          }

          okhttp3.Request request =
              requestBuilder.method(original.method(), original.body()).build();

          return chain.proceed(request);
        }
      };
    }

    public static Retrofit getClient() {
      if (retrofit == null) {
        OkHttpClient okHttpClient;

        if (DEBUG_MODE) {
          // 调试模式：不验证SSL证书
          okHttpClient = getUnsafeOkHttpClient();
        } else {
          // 生产模式：正常验证SSL证书
          okHttpClient = getSafeOkHttpClient();
        }

        // 创建Retrofit实例
        retrofit =
            new Retrofit.Builder()
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
