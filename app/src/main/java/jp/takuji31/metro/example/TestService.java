package jp.takuji31.metro.example;

import io.reactivex.Single;
import jp.takuji31.metro.annotations.HttpClient;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by takuji on 2017/04/20.
 */
@HttpClient
public interface TestService {
    @GET("/")
    Call<String> getSimpleHtml();

    @GET("/")
    Single<String> getSimpleHtmlAsSingle();

    @GET("/users/{username}")
    Single<User> user(@Path("username")String username);
}
