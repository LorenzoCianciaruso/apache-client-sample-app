import org.apache.http.HttpHost;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        String url = "http://httpbin.org/get";

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(200);

        HttpHost httpBin = new HttpHost(url, 80);
        cm.setMaxPerRoute(new HttpRoute(httpBin), 200);

        CloseableHttpClient client1 = HttpClients.custom()
                .setConnectionManager(cm)
                .build();

        CloseableHttpClient client2 = HttpClients.custom()
                .setConnectionManager(cm)
                .build();

        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)
                .setExpectContinueEnabled(true)
                .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
                .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
                .build();

        RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig)
                .setConnectTimeout(800)
                .setConnectionRequestTimeout(800)
                .build();

        HttpGet request = new HttpGet(url);
        request.setConfig(requestConfig);

        Runner runner1 = new Runner(request, client1, "client-1", cm);
        Runner runner2 = new Runner(request, client2, "client-2", cm);

        Thread thread1 = new Thread(runner1);
        Thread thread2 = new Thread(runner2);

        thread1.start();
        thread2.start();
    }
}
