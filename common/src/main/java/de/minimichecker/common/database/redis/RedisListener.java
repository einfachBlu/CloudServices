package de.minimichecker.common.database.redis;

public interface RedisListener {
    /**
     * Will be called every time when a new message was published in
     * the subscribed channel
     *
     * @param channel the channel
     * @param message the message
     */
    void onMessageReceived(String channel, String message);
}
