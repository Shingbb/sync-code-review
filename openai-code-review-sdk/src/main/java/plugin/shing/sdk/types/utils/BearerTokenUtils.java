package plugin.shing.sdk.types.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Bearer Token 实用程序
 *
 * @author shing
 */
public class BearerTokenUtils {

    private BearerTokenUtils() {
        throw new IllegalStateException("Utility class");
    }

    // 过期时间；默认30分钟
    private static final long expireMillis = 30 * 60 * 1000L;
    // 缓存服务
    public static Cache<String, String> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(expireMillis - (60 * 1000L), TimeUnit.MILLISECONDS)
            .build();

    /**
     * 根据 apiKeySecret 生成 Token
     *
     * @param apiKeySecret apiKeySecret，例如：828902ec516c45307619708d3e780ae1.w5eKiLvhnLP8MtIf
     * @return Token
     */
    public static String getToken(String apiKeySecret) {
        // 将 apiKeySecret 分割为 apiKey 和 apiSecret
        String[] split = apiKeySecret.split("\\.");
        // 调用 getToken 方法生成 Token
        return getToken(split[0], split[1]);
    }

    /**
     * 对 ApiKey 进行签名
     *
     * @param apiKey    登录创建 ApiKey <a href="https://open.bigmodel.cn/usercenter/apikeys">apikeys</a>
     * @param apiSecret apiKey的后半部分 828902ec516c45307619708d3e780ae1.w5eKiLvhnLP8MtIf 取 w5eKiLvhnLP8MtIf 使用
     * @return Token
     */
    public static String getToken(String apiKey, String apiSecret) {
        // 从缓存中获取 Token
        String token = cache.getIfPresent(apiKey);
        if (null != token) return token;
        // 创建 Token
        Algorithm algorithm = Algorithm.HMAC256(apiSecret.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> payload = new HashMap<>();
        payload.put("api_key", apiKey);
        payload.put("exp", System.currentTimeMillis() + expireMillis);
        payload.put("timestamp", Calendar.getInstance().getTimeInMillis());
        Map<String, Object> headerClaims = new HashMap<>();
        headerClaims.put("alg", "HS256");
        headerClaims.put("sign_type", "SIGN");
        token = JWT.create().withPayload(payload).withHeader(headerClaims).sign(algorithm);
        // 将 Token 存入缓存
        cache.put(apiKey, token);
        return token;
    }

}
