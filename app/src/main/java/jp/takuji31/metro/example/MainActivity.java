package jp.takuji31.metro.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.reactivex.Single;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api.takuji31.jp")
            .build();

        MetroService metroService = retrofit.create(MetroService.class);

        Single<User> userSingle = UserRequest
            .builder()
            .userName("takuji31")
            .build()
            .request(metroService);

        Single<Status> statusSingle = UserStatusRequest
            .builder()
            .userName("takuji31")
            .statusId(1234567890L)
            .build()
            .request(metroService);

        // no parameter endpoint request not created
        Single<String> htmlSingle = metroService
            .getSimpleHtmlAsSingle();
    }
}
