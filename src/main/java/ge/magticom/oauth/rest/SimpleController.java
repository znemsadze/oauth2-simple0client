package ge.magticom.oauth.rest;

import ge.magticom.oauth.rest.dto.ResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;

@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("simple")
public class SimpleController {


    @GetMapping("hello")
    private ResultDto simpleHello(Principal principal){
        log.info(((BearerTokenAuthentication) principal).getTokenAttributes().get("userId").toString());
        return new  ResultDto("Hello "+ principal.getName(),new ArrayList<>());
    }


}
