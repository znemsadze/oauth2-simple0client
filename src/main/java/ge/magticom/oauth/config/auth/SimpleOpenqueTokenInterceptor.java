package ge.magticom.oauth.config.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.*;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Instant;
import java.util.*;

public class SimpleOpenqueTokenInterceptor implements OpaqueTokenIntrospector {

    private static final String AUTHORITY_PREFIX = "SCOPE_";
    private static final ParameterizedTypeReference<Map<String, Object>> STRING_OBJECT_MAP = new ParameterizedTypeReference<Map<String, Object>>() {
    };
    private final Log logger = LogFactory.getLog(this.getClass());
    private final RestOperations restOperations;
    private Converter<String, RequestEntity<?>> requestEntityConverter;

    public SimpleOpenqueTokenInterceptor(String introspectionUri, String clientId, String clientSecret) {
        Assert.notNull(introspectionUri, "introspectionUri cannot be null");
        Assert.notNull(clientId, "clientId cannot be null");
        Assert.notNull(clientSecret, "clientSecret cannot be null");
        this.requestEntityConverter = this.defaultRequestEntityConverter(URI.create(introspectionUri));
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(clientId, clientSecret));
        this.restOperations = restTemplate;
    }

    public SimpleOpenqueTokenInterceptor(String introspectionUri, RestOperations restOperations) {
        Assert.notNull(introspectionUri, "introspectionUri cannot be null");
        Assert.notNull(restOperations, "restOperations cannot be null");
        this.requestEntityConverter = this.defaultRequestEntityConverter(URI.create(introspectionUri));
        this.restOperations = restOperations;
    }

    private Converter<String, RequestEntity<?>> defaultRequestEntityConverter(URI introspectionUri) {
        return (token) -> {
            HttpHeaders headers = this.requestHeaders();
            MultiValueMap<String, String> body = this.requestBody(token);
            return new RequestEntity(body, headers, HttpMethod.POST, introspectionUri);
        };
    }

    private HttpHeaders requestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private MultiValueMap<String, String> requestBody(String token) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap();
        body.add("token", token);
        return body;
    }

    public OAuth2AuthenticatedPrincipal introspect(String token) {
        RequestEntity<?> requestEntity = (RequestEntity)this.requestEntityConverter.convert(token);
        if (requestEntity == null) {
            throw new AccessDeniedException("requestEntityConverter returned a null entity");
        } else {
            ResponseEntity<Map<String, Object>> responseEntity = this.makeRequest(requestEntity);
            Map<String, Object> claims = this.adaptToNimbusResponse(responseEntity);
            return this.convertClaimsSet(claims);
        }
    }

    public void setRequestEntityConverter(Converter<String, RequestEntity<?>> requestEntityConverter) {
        Assert.notNull(requestEntityConverter, "requestEntityConverter cannot be null");
        this.requestEntityConverter = requestEntityConverter;
    }

    private ResponseEntity<Map<String, Object>> makeRequest(RequestEntity<?> requestEntity) {
        try {
            return this.restOperations.exchange(requestEntity, STRING_OBJECT_MAP);
        } catch (Exception var3) {
            throw new AccessDeniedException(var3.getMessage() );
        }
    }

    private Map<String, Object> adaptToNimbusResponse(ResponseEntity<Map<String, Object>> responseEntity) {
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new AccessDeniedException("Introspection endpoint responded with " + responseEntity.getStatusCode());
        } else {
            Map<String, Object> claims = (Map)responseEntity.getBody();
            if (claims == null) {
                return Collections.emptyMap();
            } else {
                boolean active = (Boolean)claims.compute("active", (k, v) -> {
                    if (v instanceof String) {
                        return Boolean.parseBoolean((String)v);
                    } else {
                        return v instanceof Boolean ? v : false;
                    }
                });
                if (!active) {
                    this.logger.trace("Did not validate token since it is inactive");
                    throw new AccessDeniedException("Provided token isn't active");
                } else {
                    return claims;
                }
            }
        }
    }

    private OAuth2AuthenticatedPrincipal convertClaimsSet(Map<String, Object> claims) {
        claims.computeIfPresent("aud", (k, v) -> {
            return v instanceof String ? Collections.singletonList(v) : v;
        });
        claims.computeIfPresent("client_id", (k, v) -> {
            return v.toString();
        });
        claims.computeIfPresent("exp", (k, v) -> {
            return Instant.ofEpochSecond(((Number)v).longValue());
        });
        claims.computeIfPresent("iat", (k, v) -> {
            return Instant.ofEpochSecond(((Number)v).longValue());
        });
        claims.computeIfPresent("iss", (k, v) -> {
            return v.toString();
        });
        claims.computeIfPresent("nbf", (k, v) -> {
            return Instant.ofEpochSecond(((Number)v).longValue());
        });
        Collection<GrantedAuthority> authorities = new ArrayList();
        claims.computeIfPresent("authorities", (k, v) -> {
            if (!(v instanceof List)) {
                return v;
            } else {
                 ((List<?>) v).forEach(item->{
                     authorities.add(new SimpleGrantedAuthority(item.toString()));
                 });
                return authorities;
            }
        });
        return new OAuth2IntrospectionAuthenticatedPrincipal(claims.get("user_name").toString(),claims, authorities);
    }
}
