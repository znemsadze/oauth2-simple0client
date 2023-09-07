package ge.magticom.oauth.rest;

import ge.magticom.oauth.rest.dto.ResultDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.jar.Attributes;

@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("simple")
public class SimpleController {


    private static getAttributeFromContext getGetAttributeFromContext(BearerTokenAuthentication principal) {
        ArrayList<String> authorities = new ArrayList<>();
        principal.getAuthorities().forEach(item -> {
            authorities.add(item.getAuthority());
        });
        ArrayList<String> attributes = new ArrayList<>();
        principal.getTokenAttributes().forEach((x, y) -> {
            attributes.add(x + "|" + y);
        });
        getAttributeFromContext result = new getAttributeFromContext(authorities, attributes);
        return result;
    }

    @GetMapping("hello")
    public ResultDto simpleHello(Principal principal) {
        getAttributeFromContext result = getGetAttributeFromContext((BearerTokenAuthentication) principal);
        return new ResultDto("Hello " + principal.getName(), result.authorities(), result.attributes());
    }

    @PreAuthorize("hasAnyAuthority('GOOGLE_MANAGER')")
    @GetMapping("unauthorized")
    public ResultDto simpleHelloWithAuthorities(Principal principal) {
        getAttributeFromContext result = getGetAttributeFromContext((BearerTokenAuthentication) principal);
        return new ResultDto("Access Granted TO " + principal.getName(), result.authorities(), result.attributes());
    }

    @PreAuthorize("hasAnyAuthority('FMS_REQUESTS_CREATE_FUEL_REQUEST','MODIFY_ACCOUNT_MANAGER')")
    @GetMapping("authorities")
    public ResultDto simpleHelloAttributes(Principal principal) {
        getAttributeFromContext result = getGetAttributeFromContext((BearerTokenAuthentication) principal);
        return new ResultDto("Hello " + principal.getName(), result.attributes(), result.attributes());
    }

    private record getAttributeFromContext(ArrayList<String> authorities, ArrayList<String> attributes) {
    }

}
