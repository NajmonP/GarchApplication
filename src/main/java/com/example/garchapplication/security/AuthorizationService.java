package com.example.garchapplication.security;

import com.example.garchapplication.repository.CalculationRepository;
import com.example.garchapplication.repository.ConfigurationRepository;
import com.example.garchapplication.repository.GarchModelRepository;
import com.example.garchapplication.repository.TimeSeriesRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("authorization")
public class AuthorizationService {

    private final ConfigurationRepository configurationRepository;
    private final TimeSeriesRepository timeSeriesRepository;
    private final CalculationRepository calculationRepository;
    private final GarchModelRepository garchModelRepository;

    public AuthorizationService(ConfigurationRepository configurationRepository, TimeSeriesRepository timeSeriesRepository, CalculationRepository calculationRepository, GarchModelRepository garchModelRepository) {
        this.configurationRepository = configurationRepository;
        this.timeSeriesRepository = timeSeriesRepository;
        this.calculationRepository = calculationRepository;
        this.garchModelRepository = garchModelRepository;
    }


    public boolean canAccessTimeSeriesDetails(Long timeSeriesId, Authentication authentication) {
        if(timeSeriesRepository.existsByIdAndVisibility(timeSeriesId, "Public") || isAdmin(authentication)) {
            return true;
        }
        return timeSeriesRepository.existsByIdAndUserId(timeSeriesId, getUserId(authentication));
    }

    public boolean canAccessTimeSeries(Long timeSeriesId, Authentication authentication) {
        if(isAdmin(authentication)) {
            return true;
        }
        return timeSeriesRepository.existsByIdAndUserId(timeSeriesId, getUserId(authentication));
    }

    public boolean canAccessConfiguration(Long configurationId, Authentication authentication) {
        return isAdmin(authentication) || configurationRepository.existsByIdAndUserId(configurationId, getUserId(authentication));
    }

    public boolean canAccessCalculation(Long calculationId, Authentication authentication) {
        return isAdmin(authentication) || calculationRepository.existsByIdAndUserId(calculationId, getUserId(authentication));
    }

    public boolean canAccessGarchModel(Long garchModelId, Authentication authentication) {
        return isAdmin(authentication) || garchModelRepository.existsByIdAndConfigurationUserId(garchModelId, getUserId(authentication));
    }

    public boolean isAuthenticated(Authentication authentication) {
        return authentication.isAuthenticated();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
               .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private Long getUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof String s) {
            if ("anonymousUser".equals(s)) {
                return null;
            }
        }

        if (principal instanceof UserDetailsImpl user) {
            return user.getId();
        }

        return null;
    }
}
