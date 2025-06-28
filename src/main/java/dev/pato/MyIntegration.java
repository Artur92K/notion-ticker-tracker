package dev.pato;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class MyIntegration {

    public static final String UPDATEURL = "https://api.notion.com/v1/pages/";
    public static final String AUTHORIZATION = "Authorization";

    private final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper mapper = new ObjectMapper();
    private final WebClient webClient = WebClient.builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("Notion-Version", "2022-06-28")
//            .defaultHeader(AUTHORIZATION, "Bearer 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")
            .build();

    @PostConstruct
    public void init() {
        update("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", "13a95ada4c4f808e968df485dfe6595a");
    }

    void update(String apikey, String database) {


        JsonNode databaseJsonNode = fetchDatabase(database, apikey);

//        var a = StreamSupport.stream(databaseJsonNode.spliterator(), false)
//                .filter(e -> {
//                    JsonNode jsonNode = e.get("properties").get("Ticker").get("title");
//                    return jsonNode != null && !jsonNode.isEmpty(); // Filter out invalid entries
//                })
//                .collect(Collectors.toMap(
//                        e -> e.get("id").asText(), // Key: pageId
//                        e -> e.get("properties")
//                                .get("Ticker")
//                                .get("title")
//                                .get(0)
//                                .get("plain_text")
//                                .asText() // Value: name
//                ));
//
//
//        ExecutorService executor = Executors.newFixedThreadPool(10); // Limit concurrency to 10 threads
//
//        Map<String, Model.ChartResponse> asd = Flux.fromIterable(a.entrySet())
//                .parallel()
//                .runOn(Schedulers.fromExecutor(executor)) // Use custom thread pool
//                .map(entry -> {
//                    String id = entry.getKey();
//                    String name = entry.getValue();
//                    Model.ChartResponse response = getStockPrices(name); // Call the synchronous method
//                    return Tuples.of(id, response);
//                })
//                .filter(s -> s.getT2().getChart().getResult()!=null)
//                .sequential()
//                .collectMap(Tuple2::getT1, Tuple2::getT2)
//                .block();
//
//

        databaseJsonNode.forEach(e ->

        {
            String pageId = e.get("id").asText();
            JsonNode jsonNode = e.get("properties").get("Ticker").get("title");
            if (jsonNode.isNull() || jsonNode.isEmpty()) {
                return;
            }
            String name = jsonNode.get(0).get("plain_text").asText();
            Model.ChartResponse stockPrices = getStockPrices(name);

            var result = stockPrices.getChart().getResult();
            if (result == null) {
                return;
            }
            result.stream()
                    .map(Model.Result::getMeta)
                    .filter(dMeta -> dMeta.getSymbol().equals(name))
                    .findFirst()
                    .ifPresent(meta -> {
                        var regularMarketPrice = meta.getRegularMarketPrice();
                        var currency = meta.getCurrency();
                        log.info("Current price is {}, {}, {} ", meta.getSymbol(), regularMarketPrice, meta.getCurrency());

                        createJson(Double.parseDouble(regularMarketPrice), currency, meta.getExchangeName())
                                .ifPresent(r -> webClient
                                        .patch()
                                        .uri(UPDATEURL + pageId)
                                        .header(AUTHORIZATION, apikey) // Add your header here
                                        .bodyValue(r)
                                        .exchangeToMono(g -> g.bodyToMono(String.class))
                                        .block());
                    });


        });
    }

    private JsonNode fetchDatabase(String database, String apikey) {
        HttpHeaders headers = getHeaders(apikey);
        HttpEntity<String> entity = new HttpEntity<>("{}", headers);
        String url = "https://api.notion.com/v1/databases/" + database + "/query";
        return mapper.convertValue(restTemplate.exchange(url, HttpMethod.POST, entity, Map.class).getBody().get("results"), JsonNode.class);

    }


    private Model.ChartResponse getStockPrices(String name) {

        String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=1h&range=1h", name);

        var response = webClient.get()
                .uri(url)
                .exchangeToMono(g -> g.bodyToMono(String.class))
                .block();

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return mapper.readValue(response, Model.ChartResponse.class);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private HttpHeaders getHeaders(String apikey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apikey);
        headers.set("Notion-Version", "2022-06-28");
        headers.set("Content-Type", "application/json");
        return headers;
    }

    public static Optional<String> createJson(double number, String currency, String name) {
        try {
            // Create the root node
            ObjectNode rootNode = mapper.createObjectNode();
            ObjectNode propertiesNode = mapper.createObjectNode();

            // Create the Value node
            ObjectNode valueNode = mapper.createObjectNode();
            valueNode.put("number", number);
            propertiesNode.set("Unit Price", valueNode);

            // Create the Currency node
            ObjectNode currencyNode = mapper.createObjectNode();
            ArrayNode currencyRichTextArray = mapper.createArrayNode();
            ObjectNode currencyTextNode = mapper.createObjectNode();
            ObjectNode currencyContentNode = mapper.createObjectNode();

            currencyContentNode.put("content", currency);
            currencyTextNode.set("text", currencyContentNode);
            currencyRichTextArray.add(currencyTextNode);

            currencyNode.set("rich_text", currencyRichTextArray);
            propertiesNode.set("Currency", currencyNode);

            // Set properties to root node
            rootNode.set("properties", propertiesNode);


            return Optional.ofNullable(mapper.writeValueAsString(rootNode));
        } catch (Exception e) {
            log.info("Error while creating json for {}", name, e);
            return Optional.empty();
        }
    }


}

