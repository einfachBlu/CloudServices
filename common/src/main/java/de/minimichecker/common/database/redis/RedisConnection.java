package de.minimichecker.common.database.redis;

import java.util.Collection;
import java.util.Map;

public interface RedisConnection {

    /**
     * Set Credentials to connect
     *
     * @param host the Host
     * @param port the port
     * @param password the password
     */
    void init(String host, int port, String password);

    /**
     * Connect to Redis
     */
    void connect();

    /**
     * Disconnect from Redis
     */
    void disconnect();

    /**
     * Check if it is connected to redis
     *
     * @return true if connected or false if not
     */
    boolean isConnected();

    /**
     * Set the value of the Key
     *
     * @param key   the key
     * @param value the new Value to set
     */
    void set(String key, String value);

    /**
     * Set the value of the Key and expire after a specified time
     *
     * @param key           the key
     * @param value         the new Value to set
     * @param expireSeconds the time in seconds when the key should expire
     */
    void set(String key, String value, int expireSeconds);

    /**
     * Remove a Key from Redis
     *
     * @param key the Key to remove
     */
    void remove(String key);

    /**
     * Remove a key from Redis recursively
     *
     * @param key the Key to remove recursively
     */
    void removeRecursive(String key);

    /**
     * Get all child Keys of a given Key (not recursive)
     *
     * @param key the parent Key
     * @return Collection with all Keys
     */
    Collection<String> getKeys(String key);

    /**
     * Get all child Keys of a given Key
     *
     * @param key       the parent Key
     * @param recursive wether it should be returned
     *                  all keys recursive or not
     * @return Collection with all Keys
     */
    Collection<String> getKeys(String key, boolean recursive);

    /**
     * Get the String value of a specified Key
     *
     * @param key the Key where the Value should get from
     * @return String with the value from the Key or null if the key does not exist
     */
    String get(String key);

    /**
     * Get a Map with all Data saved in Redis
     *
     * @return Map with all Keys and their Values
     */
    Map<String, String> getAll();

    /**
     * Check if a specified Key exist in Redis
     *
     * @param key the Key to check
     * @return true if the Key exist or false if not
     */
    boolean contains(String... key);

    /**
     * Get the remaining Time until the Key expire
     *
     * @param key the Key to get the remaining time from
     * @return the time in seconds, until the Key will expire
     */
    int getRemainingTimeFromKey(String key);

    /**
     * Subscribe to Redis Pub/Sub
     *
     * @param listener the listener for actions when there will be
     *                 a message published in any of the channels
     * @param channels the channels which should be subscribed
     *                 to the listener
     */
    void subscribe(RedisListener listener, String... channels);

    /**
     * Publish a Message in Redis Pub/Sub Channel
     *
     * @param channel the channel
     * @param message the message
     */
    void publish(String channel, String message);

    /**
     * Check if a Channel was subscribed by any Application
     *
     * @param channel the Channel to check
     * @return true if someone subscribed the channel or false
     * if no one subscribed
     */
    boolean channelExists(String channel);
}
