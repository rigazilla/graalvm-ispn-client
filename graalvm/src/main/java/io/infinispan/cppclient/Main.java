package io.infinispan.cppclient;

import org.graalvm.nativeimage.c.function.CEntryPoint;

import java.util.HashMap;
import java.util.Map;

import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;


//@QuarkusMain
class Main {
    public static void main(String... args) {
        
    }

    @CEntryPoint(name = "transformSimple")
    public static CCharPointer transform(IsolateThread thread, CCharPointer xml, CCharPointer xslt) {
        return CTypeConversion.toCString("string").get();
    }

    @CEntryPoint(name = "getEntry")
    public static CCharPointer get(IsolateThread thread, CCharPointer cacheName, CCharPointer key) {
        try (var rcm = getRcm()) {
            var cacheNameStr = CTypeConversion.toJavaString(cacheName);
            var keyStr = CTypeConversion.toJavaString(key);
            var cache = cacheMap.get(cacheNameStr);
            if (cache == null) {
                cache = rcm.getCache(cacheNameStr);
                cacheMap.put(cacheNameStr, cache);
            }
            return CTypeConversion.toCString(cache.get(keyStr)).get();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CTypeConversion.toCString("string").get();
    }

    public static final String USER = "admin";
    public static final String PASSWORD = "password";
    public static final String HOST = "127.0.0.1";
    public static final int SINGLE_PORT = ConfigurationProperties.DEFAULT_HOTROD_PORT;
 
    public static final String TUTORIAL_CACHE_NAME = "test";
    public static final String TUTORIAL_CACHE_CONFIG =
          "<distributed-cache name=\"CACHE_NAME\">\n"
          + "    <encoding media-type=\"application/x-protostream\"/>\n"
          + "</distributed-cache>";
 
    private static RemoteCacheManager rcm = null;
    private static Map<String, RemoteCache<String,String>> cacheMap = new HashMap<>();
    private static RemoteCacheManager getRcm() {
        if (rcm == null) {
            rcm = connect();
        }
        return rcm;
    }
    /**
     * Connect to the running Infinispan Server in localhost:11222.
     *
     * This method illustrates how to connect to a running Infinispan Server with a downloaded
     * distribution or a container.
     *
     * @return a connected RemoteCacheManager
     */
    public static final RemoteCacheManager connect() {
       ConfigurationBuilder builder = connectionConfig();
 
       RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());
       // Clear the cache in case it already exists from a previous running tutorial
       cacheManager.getCache(TUTORIAL_CACHE_NAME).clear();
 
       // Return the connected cache manager
       return cacheManager;
    }

    /**
     * Returns the configuration builder with the connection information
     *
     * @return a Configuration Builder with the connection config
     */
    public static final ConfigurationBuilder connectionConfig() {
        System.out.println("Current thread: "+Thread.currentThread());
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer().host(HOST).port(SINGLE_PORT).security()
              .authentication()
              //Add user credentials.
              .username(USER)
              .password(PASSWORD);
  
        // Docker 4 Mac Workaround. Don't use BASIC intelligence in production
        builder.clientIntelligence(ClientIntelligence.BASIC);
  
        // Make sure the remote cache is available.
        // If the cache does not exist, the cache will be created
        builder.remoteCache(TUTORIAL_CACHE_NAME)
              .configuration(TUTORIAL_CACHE_CONFIG.replace("CACHE_NAME", TUTORIAL_CACHE_NAME));
        return builder;
     }
}