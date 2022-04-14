package de.tsearch.lorva.web;

import de.tsearch.lorva.database.postgres.entity.Broadcaster;
import de.tsearch.lorva.database.postgres.repository.BroadcasterRepository;
import de.tsearch.tclient.AuthorizationClient;
import de.tsearch.tclient.http.respone.TokenResponse;
import de.tsearch.tclient.http.respone.TokenValidateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("")
public class LoginController {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${redirect.success}")
    private String redirectUrlSuccess;

    @Value("${redirect.error}")
    private String redirectUrlError;

    @Value("${twitch.redirect-uri}")
    private String twitchRedirectUri;

    @Autowired
    private AuthorizationClient authorizationClient;

    @Autowired
    private BroadcasterRepository broadcasterRepository;

    @GetMapping(value = "login", params = {"code", "scope"})
    public RedirectView login(@RequestParam(value = "code") String code,
                              @RequestParam(value = "scope") List<String> scopes,
                              @RequestParam(value = "state", required = false) String state,
                              RedirectAttributes attributes) {
        Optional<TokenResponse> tokenResponseOptional = authorizationClient.getTokenFromAuthorizationCode(code, twitchRedirectUri);
        if (tokenResponseOptional.isEmpty())
            return login("server-error", "Cannot get access token from code", state, attributes);

        TokenResponse tokenResponse = tokenResponseOptional.get();
        Optional<TokenValidateResponse> validateResponseOptional = authorizationClient.validateToken(tokenResponse.getAccessToken());
        if (validateResponseOptional.isEmpty())
            return login("server-error", "Cannot validate access token", state, attributes);

        TokenValidateResponse validateResponse = validateResponseOptional.get();
        Broadcaster broadcaster;
        Optional<Broadcaster> broadcasterOptional = broadcasterRepository.findById(validateResponse.getUserId());
        if (broadcasterOptional.isPresent()) {
            broadcaster = broadcasterOptional.get();
        } else {
            broadcaster = new Broadcaster();
            broadcaster.setId(validateResponse.getUserId());
            broadcaster.setDisplayName(validateResponse.getLogin());
        }

        broadcaster.setTwitchAuthorised(true);
        broadcasterRepository.save(broadcaster);

        authorizationClient.revokeToken(tokenResponse.getAccessToken());

        attributes.addAttribute("login", validateResponse.getLogin());

        return new RedirectView(redirectUrlSuccess);
    }

    @GetMapping(value = "login", params = {"error", "error_description"})
    public RedirectView login(@RequestParam(value = "error") String error,
                              @RequestParam(value = "error_description") String description,
                              @RequestParam(value = "state", required = false) String state,
                              RedirectAttributes attributes) {
        attributes.addAttribute("error", error);
        attributes.addAttribute("description", description);
        return new RedirectView(redirectUrlError);
    }
}
