# Metro
Generate HTTP Request Builder for Retrofit

## Usage

Your service class
```java
@HttpClient
public interface MetroService {
    @GET("/")
    Single<String> getSimpleHtmlAsSingle();

    @GET("/users/{userName}")
    Single<User> user(@Path("userName")String userName);

    @GET("/users/{userName}/status/{status_id}")
    Single<Status> userStatus(@Path("userName")String userName, @Path("status_id")Long statusId);
}
```

```java
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
```