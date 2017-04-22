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
public interface MetroService {
    @GET("/")
    Single<String> getSimpleHtmlAsSingle();

    @GET("/users/{userName}")
    Single<User> user(@Path("userName")String userName);

    @GET("/users/{userName}/status/{status_id}")
    Single<Status> userStatus(@Path("userName")String userName, @Path("status_id")Long statusId);
}
