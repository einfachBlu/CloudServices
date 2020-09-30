package de.blu.common.storage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.config.LogsConfig;
import de.blu.common.database.redis.RedisConnection;
import lombok.Getter;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Singleton
@Getter
public final class LogsStorage {

    private static final long MAX_FILE_LENGTH = 5 * 1024 * 1024L;

    @Inject
    private LogsConfig logsConfig;

    @Inject
    private RedisConnection redisConnection;

    @Inject
    private ExecutorService executorService;

    public boolean isEnabled() {
        return this.getLogsConfig().isEnabled();
    }

    public String getLogUrl(UUID gameServerUniqueId) {
        if (!this.getRedisConnection().contains("logs." + gameServerUniqueId.toString())) {
            return "";
        }

        // Return the url of the log
        return this.getRedisConnection().get("logs." + gameServerUniqueId.toString());
    }

    public String postUrl(UUID gameServerUniqueId, File logFile, int lines) {
        if (logFile == null) {
            return "";
        }

        String url = "";
        try {
            byte[] data;
            if (lines == -1) {
                data = Files.readAllBytes(logFile.toPath());
            } else {
                data = this.readBottomLines(logFile, lines);
                if (data == null) {
                    data = Files.readAllBytes(logFile.toPath());
                }
            }

            url = this.postToHastebin(data);
            this.getRedisConnection().set("logs." + gameServerUniqueId.toString(), url, (int) this.getLogsConfig().getCacheTime());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Return the url of the created log
        return url;
    }

    private byte[] readBottomLines(final File file, final int lines) throws IOException {
        try {
            try (ReversedLinesFileReader reader = new ReversedLinesFileReader(file)) {
                byte[][] buffer = new byte[lines][];
                int linesToRead = lines;

                String line;
                int currentLine = 0;
                while ((line = reader.readLine()) != null && currentLine++ < lines) {
                    byte[] lineBytes = line.getBytes(StandardCharsets.UTF_8);
                    buffer[--linesToRead] = lineBytes;
                }

                int unreadLines = lines - linesToRead;
                int bufferLen = Arrays.stream(buffer).skip(linesToRead).mapToInt(b -> b.length).sum();
                byte[] returnArray = new byte[bufferLen + unreadLines];
                int returnArrayIndex = 0;
                for (int i = linesToRead; i < buffer.length; i++) {
                    byte[] lineArray = buffer[i];
                    System.arraycopy(buffer[i], 0, returnArray, returnArrayIndex, lineArray.length);
                    returnArrayIndex += lineArray.length;
                    returnArray[returnArrayIndex++] = '\n';
                }

                return returnArray;
            }
        } catch (Exception e2) {
            // Can happen on spigot servers when the ReversedLinesFileReader does not exist there
            return null;
        }
    }

    private String postToHastebin(final byte[] data) throws IOException {
        URL url = new URL(this.getLogsConfig().getPostUrl());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("POST");

        OutputStream outputStream = urlConnection.getOutputStream();
        outputStream.write(data);
        outputStream.flush();
        outputStream.close();

        urlConnection.connect();

        if (urlConnection.getResponseCode() != 200) {
            throw new IOException("Server returned not 200 response code");
        }

        try (InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String result = bufferedReader.lines().collect(Collectors.joining("\n"));

            JSONObject response = (JSONObject) JSONValue.parse(result);

            return this.getLogsConfig().getPasteUrl() + response.get("key");
        }
    }
}
