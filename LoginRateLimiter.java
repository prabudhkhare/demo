package src;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class LoginRateLimiter {
    private static final String LOGIN_LIMIT_MESSAGE = "login limit reached";
    private static final int MAX_NUMBER_OF_IP_REQUEST = 5;
    private static final int MAX_NUMBER_OF_IP_REQUEST_WITHIN_HOUR = 15;
    private static final int MAX_NUMBER_OF_COOKIE_REQUEST = 2;
    private static final int MAX_NUMBER_OF_USERNAME_REQUEST = 10;
    private static final int SECONDS_IN_HOUR = 3600;
    private static final int SECONDS_IN_MINUTE = 60;
    /*
     * clientIpList, cookieIdList , usernameList for saving previous records in memory.
     * */
    private static final Map<String, List<LocalDateTime>> clientIpMap = new HashMap<>();
    private static final Map<String, List<LocalDateTime>> cookieIdMap = new HashMap<>();
    private static final Map<String, List<LocalDateTime>> usernameMap = new HashMap<>();

    public static void loginRateLimiter(String clientIp, String cookieId, String username) throws Exception {
        clientIpValidation(clientIp);
        cookieIdValidation(cookieId);
        usernameValidation(username);
        persistClientIPInMemory(clientIp);
        if (cookieId != null) {
            persistCookieIdImMemory(cookieId);
        }
        persistUsernameInMemory(username);
    }

    private static void clientIpValidation(String clientIp) throws Exception {
        throwIfLimitReached(clientIpMap.get(clientIp), MAX_NUMBER_OF_IP_REQUEST, SECONDS_IN_MINUTE);
        throwIfLimitReached(clientIpMap.get(clientIp), MAX_NUMBER_OF_IP_REQUEST_WITHIN_HOUR, SECONDS_IN_HOUR);
    }

    private static void cookieIdValidation(String cookieId) throws Exception {
        throwIfLimitReached(cookieIdMap.get(cookieId), MAX_NUMBER_OF_COOKIE_REQUEST, 10);
    }

    private static void usernameValidation(String username) throws Exception {
        throwIfLimitReached(usernameMap.get(username), MAX_NUMBER_OF_USERNAME_REQUEST, SECONDS_IN_HOUR);
    }

    private static void persistUsernameInMemory(String username) {
        updateMapForKey(usernameMap, username);
    }

    private static void persistCookieIdImMemory(String cookieId) {
        updateMapForKey(cookieIdMap, cookieId);
    }

    private static void persistClientIPInMemory(String clientIp) {
        updateMapForKey(clientIpMap, clientIp);
    }

    private static void throwIfLimitReached(List<LocalDateTime> list, int limit, int timeLimitInSeconds) throws Exception {
        if (list != null && list.size() >= limit) {
            LocalDateTime dt = getNthElementAfterDescSort(list, limit);
            long seconds = ChronoUnit.SECONDS.between(dt, LocalDateTime.now());

            if (seconds < timeLimitInSeconds) {
                throw new Exception(LOGIN_LIMIT_MESSAGE);
            }
        }
    }

    private static LocalDateTime getNthElementAfterDescSort(List<LocalDateTime> list, int n) {

        return list.stream()
                .sorted(Comparator.reverseOrder())
                .limit(n)
                .reduce((first, second) -> second).get();
    }

    private static void updateMapForKey(Map<String, List<LocalDateTime>> map, String key) {
        List<LocalDateTime> existingList = map.get(key);
        if (existingList != null) {
            existingList.add(LocalDateTime.now());
            map.put(key, existingList);
        } else {
            List<LocalDateTime> dtList = new ArrayList<>();
            dtList.add(LocalDateTime.now());
            map.put(key, dtList);
        }
    }
}
