package rw.gov.bnr.bnrlicensingportal.security;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class RequestContext {

    public String clientIp() {
        try {
            var attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            String forwarded = attrs.getRequest().getHeader("X-Forwarded-For");
            return forwarded != null ? forwarded.split(",")[0].trim() : attrs.getRequest().getRemoteAddr();
        } catch (IllegalStateException e) {
            return "SYSTEM";
        }
    }

    public String userAgent() {
        try {
            var attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attrs.getRequest().getHeader("User-Agent");
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
