package fr.netfit.demo.web;

import fr.netfit.demo.model.Request;
import fr.netfit.demo.model.Response;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class DemoController {

    private final RestTemplate restTemplate;

    @Value("${api.serviceUrl}")
    private String serviceUrl;

    public DemoController(@Qualifier("oAuthRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/demo")
    public ResponseEntity<Response> post(@RequestBody Request request) {
        return restTemplate.postForEntity(serviceUrl, request, Response.class);
    }
}
