package testconnection;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class PurgomalumFilter {

    private static final String API_URL = "https://www.purgomalum.com/service/json";

    public static String filterText(String text) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            String encodedText = java.net.URLEncoder.encode(text, "UTF-8");
            HttpGet request = new HttpGet(API_URL + "?text=" + encodedText);
            CloseableHttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                String responseBody = EntityUtils.toString(entity);
                return responseBody; // Contains the filtered text
            } else {
                throw new RuntimeException("Failed to receive response from API");
            }
        } finally {
            httpClient.close();
        }
    }

    public static void main(String[] args) throws Exception {
        String originalText = "Fuck you";
        String filteredText = filterText(originalText);
        System.out.println("Original text: " + originalText);
        System.out.println("Filtered text: " + filteredText);
    }
}
