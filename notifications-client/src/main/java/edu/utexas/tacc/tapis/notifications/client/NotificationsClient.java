package edu.utexas.tacc.tapis.notifications.client;

import org.apache.commons.lang3.StringUtils;

import edu.utexas.tacc.tapis.client.shared.ITapisClient;
import edu.utexas.tacc.tapis.notifications.client.gen.ApiClient;
import edu.utexas.tacc.tapis.notifications.client.gen.Configuration;
import edu.utexas.tacc.tapis.notifications.client.gen.api.HealthApi;
import edu.utexas.tacc.tapis.notifications.client.gen.api.QueuesApi;
import edu.utexas.tacc.tapis.notifications.client.gen.api.SubscriptionsApi;
import edu.utexas.tacc.tapis.notifications.client.gen.api.TopicsApi;

public class NotificationsClient 
 implements ITapisClient
{

    public static final String TAPIS_JWT_HEADER = "X-Tapis-Token";


    private final HealthApi healthApi;
    private final TopicsApi topicsApi;
    private final SubscriptionsApi subscriptionsApi;
    private final QueuesApi queuesApi;



    public NotificationsClient(String basePath, String jwt) {
        ApiClient apiClient = Configuration.getDefaultApiClient();
        if (!StringUtils.isBlank(basePath)) apiClient.setBasePath(basePath);
        if (!StringUtils.isBlank(jwt)) apiClient.addDefaultHeader(TAPIS_JWT_HEADER, jwt);
        healthApi = new HealthApi();
        subscriptionsApi = new SubscriptionsApi();
        topicsApi = new TopicsApi();
        queuesApi = new QueuesApi();

    }

    public ApiClient getApiClient()
    {
        return Configuration.getDefaultApiClient();
    }

    /**
     * Update base path for default client.
     */
    public NotificationsClient setBasePath(String basePath)
    {
        Configuration.getDefaultApiClient().setBasePath(basePath);
        return this;
    }

    /**
     * Add http header to default client
     */
    public NotificationsClient addDefaultHeader(String key, String val)
    {
        Configuration.getDefaultApiClient().addDefaultHeader(key, val);
        return this;
    }


    public HealthApi health() {
        return healthApi;
    }

    public TopicsApi topics() {
        return topicsApi;
    }

    public SubscriptionsApi subscriptions() {
        return subscriptionsApi;
    }

    public QueuesApi queues() {
        return queuesApi;
    }

    /** Close connections and stop threads that can sometimes prevent JVM shutdown.
     */
    public void close()
    {
        try {
            // Best effort attempt to shut things down.
            var okClient = getApiClient().getHttpClient();
            if (okClient != null) {
                var pool = okClient.connectionPool();
                if (pool != null) pool.evictAll();
            }
        } catch (Exception e) {}      
    }
    

}
