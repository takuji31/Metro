package jp.takuji31.metro.example;

import io.reactivex.Single;
import jp.takuji31.metro.annotations.APIClient;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by takuji on 2017/04/20.
 */
@APIClient
public interface TestService {
    @GET("/")
    Call<String> getSimpleHtml();

    @GET("/")
    Single<String> getSimpleHtmlAsSingle();
}
