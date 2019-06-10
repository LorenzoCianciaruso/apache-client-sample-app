import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;

import static org.apache.http.protocol.HTTP.USER_AGENT;

public class Runner implements Runnable {

    private HttpGet request;
    private HttpClient client;
    private String name;
    private PoolingHttpClientConnectionManager connectionManager;

    private int success = 0;
    private int failures = 0;

    public Runner(HttpGet request, HttpClient client, String name, PoolingHttpClientConnectionManager connectionManager) {
        this.request = request;
        this.client = client;
        this.name = name;
        this.connectionManager = connectionManager;
    }

    @Override
    public void run() {
        while (true) {
            Random randomizer = new Random();
            int random = randomizer.nextInt(1000 * 3);

            // add request header
            request.addHeader("User-Agent", USER_AGENT);
            Optional<HttpResponse> response = Optional.empty();
            try {
                response = Optional.of(client.execute(request));
            } catch (ConnectionPoolTimeoutException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }

            int statusCode = response.map(r -> r.getStatusLine().getStatusCode()).orElse(0);

            if (statusCode == 200) {
                EntityUtils.consumeQuietly(response.get().getEntity());
                success++;
                System.out.println("[" + name + "] SUCCESS!" + success + "/" + failures);
            } else {
                failures++;
                System.out.println("[" + name + "] FAILURE!" + success + "/" + failures);
            }
            System.out.println(connectionManager.getTotalStats());
            request.releaseConnection();

            try {
                Thread.sleep(random);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
