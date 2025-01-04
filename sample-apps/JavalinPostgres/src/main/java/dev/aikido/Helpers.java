package dev.aikido;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.nio.file.Path;

public class Helpers {
    public static String executeShellCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            output.append("Error: ").append(e.getMessage());
        }
        return output.toString();
    }

    public static String makeHttpRequest(String urlString) {
        StringBuilder response = new StringBuilder();
        try {
            java.net.URL url = new java.net.URL(urlString);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine).append("\n");
            }
            in.close();
        } catch (IOException e) {
            response.append("Error: ").append(e.getMessage());
        }
        return response.toString();
    }

    public static String makeHttpRequestWithOkHttp(String urlString) {
        StringBuilder response = new StringBuilder();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(urlString).build();
        try (Response resp = client.newCall(request).execute()) {
            if (resp.body() != null) {
                response.append(resp.body().string());
            }
        } catch (IOException e) {
            response.append("Error: ").append(e.getMessage());
        }
        return response.toString();
    }

    public static String readFile(String filePath) {
        StringBuilder content = new StringBuilder();
        File file = Path.of("src/main/resources/blogs", filePath).toFile();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            content.append("Error: ").append(e.getMessage());
        }
        return content.toString();
    }
}
