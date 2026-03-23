package beetech.app.core.network;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.squareup.okhttp.OkHttpClient;

import io.reactivex.rxjava3.core.Single;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class TestSignalR {

    public static HubConnection createHub(String address) {
        HubConnection hub = HubConnectionBuilder.create("https://sams.vn/hub")
                .withAccessTokenProvider(Single.defer(() -> Single.just("test-token")))
                .build();

        return hub;
    }
}
