package fr.netfit.demo.interceptor;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import java.io.IOException;

public class OAuth2ClientRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private final OAuth2AuthorizedClientManager manager;
    private final OAuth2AuthorizeRequest authorizeRequest;

    public OAuth2ClientRestTemplateInterceptor(OAuth2AuthorizedClientManager manager,
                                               String username, String password,
                                               ClientRegistration clientRegistration) {
        this.manager = manager;
        this.authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(clientRegistration.getRegistrationId())
                .attribute(OAuth2ParameterNames.USERNAME, username)
                .attribute(OAuth2ParameterNames.PASSWORD, password)
                .principal(new AnonymousAuthenticationToken("key", "anonymous",
                        AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")))
                .build();
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        OAuth2AuthorizedClient client = manager.authorize(authorizeRequest);
        if (client == null) {
            throw new IllegalStateException("Can't access the API without an authorized client");
        }

        String accessToken = client.getAccessToken() != null ? client.getAccessToken().getTokenValue() : null;
        if (accessToken == null) {
            throw new IllegalStateException("Can't access the API without an access token");
        }

        request.getHeaders().add("Authorization", "Bearer " + accessToken);
        return execution.execute(request, body);
    }

}
