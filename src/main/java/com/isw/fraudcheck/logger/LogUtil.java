package com.isw.fraudcheck.logger;

import java.util.Arrays;
import java.util.Set;

public class LogUtil {
    private static final Set<String> SENSITIVE_PARAMS = Set.of(
            "password", "confirmPassword", "oldPassword", "newPassword",
            "cardNumber", "cvv", "pin", "secret", "token"
    );

    public static Object[] maskArgs(String[] paramNames, Object[] args) {
        if (paramNames == null || args == null) return args;

        Object[] masked = Arrays.copyOf(args, args.length);
        for (int i = 0; i < paramNames.length; i++) {
            if (SENSITIVE_PARAMS.contains(paramNames[i])) {
                masked[i] = "******";
            } else {
                masked[i] = maskSensitiveFields(args[i]);
            }
        }
        return masked;
    }

    // Handles DTOs — masks fields inside the object
    private static Object maskSensitiveFields(Object arg) {
        if (arg == null) return null;

        try {
            // Work on a string representation to avoid mutating the real object
            String json = arg.toString();
            for (String field : SENSITIVE_PARAMS) {
                // Matches: password=someValue, or "password":"someValue"
                json = json.replaceAll(
                        "(?i)(" + field + "=)[^,)]+",   "$$1******"
                ).replaceAll(
                        "(?i)(\"" + field + "\":\")[^\"]+\"", "$$1******\""
                );
            }
            return json;
        } catch (Exception e) {
            return "***UNREADABLE***";
        }
    }
}