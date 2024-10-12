package plugin.shing.sdk.test;

import com.alibaba.fastjson2.JSON;
import org.junit.Test;
import plugin.shing.sdk.domain.model.ChatCompletionSyncResponse;
import plugin.shing.sdk.types.utils.BearerTokenUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static plugin.shing.sdk.constant.KeyConstant.AI_KEY;

/**
 * @author shing
 */
public class ApiTest {
    public static void main(String[] args) {
        // 本地 key
        String token = BearerTokenUtils.getToken(AI_KEY);
        System.out.println(token);
    }

    @Test
    public void test_http() throws IOException {

        // 本地 key
        String token = BearerTokenUtils.getToken(AI_KEY);

        // 创建 URL 对象，指定请求的 URL 地址
        URL url = new URL("  https://open.bigmodel.cn/api/paas/v4/chat/completions");
        // 打开连接
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

        // 设置请求方法为 POST
        httpURLConnection.setRequestMethod("POST");
        // 设置 Authorization 请求头，携带 Bearer Token
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        // 设置 Content-Type 请求头，指定请求体的类型为 JSON
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        // 启用输出流，以便向服务器发送数据
        httpURLConnection.setDoOutput(true);

        // 待执行的代码
        String code = "1+1";

        // 构造 JSON 格式的请求体
        String jsonInpuString = "{"
                + "\"model\":\"glm-4-flash\","
                + "\"messages\": ["
                + "    {"
                + "        \"role\": \"user\","
                + "        \"content\": \"你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码为: " + code + "\""
                + "    }"
                + "]"
                + "}";

        // 向服务器发送 JSON 格式的请求数据
        try (OutputStream outputStream = httpURLConnection.getOutputStream()) {
            byte[] jsonInpuStringBytes = jsonInpuString.getBytes(StandardCharsets.UTF_8);
            outputStream.write(jsonInpuStringBytes);
        }

        // 获取服务器的响应码
        int responseCode = httpURLConnection.getResponseCode();
        System.out.println(responseCode); // 打印响应码

        // 读取服务器的响应数据
        BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8));

        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        // 关闭资源
        in.close();
        httpURLConnection.disconnect();

        // 打印响应内容
        System.out.println(content);

        // 解析返回的JSON内容为ChatCompletionSyncResponse对象
        ChatCompletionSyncResponse response = JSON.parseObject(content.toString(), ChatCompletionSyncResponse.class);

        // 打印聊天完成响应的消息内容
        System.out.println(response.getChoices().get(0).getMessage().getContent());


    }
}
