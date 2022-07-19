package fr.netfit.demo.config;

import fr.netfit.demo.interceptor.OAuth2ClientRestTemplateInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.springframework.security.oauth2.core.AuthorizationGrantType.PASSWORD;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_POST;


@Configuration
public class OAuth2RestTemplateConfig {

    @Value("${api.client-id}")
    private String clientId;

    @Value("${api.token-uri}")
    private String tokenUri;

    @Value("${api.registration-id}")
    private String registrationId;

    @Value("${api.client-secret}")
    private String clientSecret;

    @Value("${api.username}")
    private String username;

    @Value("${api.password}")
    private String password;

    @Bean("oAuthRestTemplate")
    public RestTemplate oAuthRestTemplate(RestTemplateBuilder restTemplateBuilder,
                                          OAuth2AuthorizedClientManager manager) {

        return restTemplateBuilder
                .additionalInterceptors(new OAuth2ClientRestTemplateInterceptor(manager, username, password, getClientRegistration()))
                .setReadTimeout(Duration.ofSeconds(5))
                .setConnectTimeout(Duration.ofSeconds(1))
                .build();
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager() {

        ClientRegistrationRepository clients = new InMemoryClientRegistrationRepository(getClientRegistration());

        OAuth2AuthorizedClientService service = new InMemoryOAuth2AuthorizedClientService(clients);

        // Using AuthorizedClientServiceOAuth2AuthorizedClientManager instead of the DefaultOAuth2AuthorizedClientManager
        // to support asynchrone execution through the @Async annotation
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clients, service);

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .password()
                        .build();


        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        // Assuming the `username` and `password` are supplied as `HttpServletRequest` parameters,
        // map the `HttpServletRequest` parameters to `OAuth2AuthorizationContext.getAttributes()`
        authorizedClientManager.setContextAttributesMapper(contextAttributesMapper());

        return authorizedClientManager;
    }

    private Function<OAuth2AuthorizeRequest, Map<String, Object>> contextAttributesMapper() {
        return authorizeRequest -> {
            Map<String, Object> contextAttributes = new HashMap<>();
            String name = authorizeRequest.getAttribute(OAuth2ParameterNames.USERNAME);
            String pwd = authorizeRequest.getAttribute(OAuth2ParameterNames.PASSWORD);
            if (StringUtils.hasText(name) && StringUtils.hasText(pwd)) {
                contextAttributes = new HashMap<>();

                // `PasswordOAuth2AuthorizedClientProvider` requires both attributes
                contextAttributes.put(OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, name);
                contextAttributes.put(OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, pwd);
            }
            return contextAttributes;
        };
    }

    private ClientRegistration getClientRegistration() {
        return ClientRegistration.withRegistrationId(registrationId)
                .authorizationGrantType(PASSWORD)
                .clientAuthenticationMethod(CLIENT_SECRET_POST)
                .clientSecret(clientSecret)
                .tokenUri(tokenUri)
                .clientId(clientId)
                .build();
    }
}
