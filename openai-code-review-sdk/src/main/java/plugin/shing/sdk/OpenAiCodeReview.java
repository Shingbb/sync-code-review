package plugin.shing.sdk;

import com.alibaba.fastjson2.JSON;
import plugin.shing.sdk.domain.model.ChatCompletionRequest;
import plugin.shing.sdk.domain.model.ChatCompletionSyncResponse;
import plugin.shing.sdk.domain.model.Model;
import plugin.shing.sdk.types.utils.BearerTokenUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static plugin.shing.sdk.constant.KeyConstant.AI_KEY;

/**
 * OpenAiCodeReview 类用于演示如何在 Java 中使用 OpenAI API 进行代码评审
 *
 * @author shing
 */
public class OpenAiCodeReview {

    public static void main(String[] args) throws IOException, InterruptedException {
        // 打印测试执行信息
        System.out.println("测试执行");

        // 1. 代码检出
        // 使用 ProcessBuilder 执行 git diff 命令获取最近两次提交之间的代码变更
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
        // 设置命令在当前目录下执行
        processBuilder.directory(new File("."));

        // 启动进程
        Process process = processBuilder.start();

        // 读取进程的输出
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        // 用于存储 git diff 输出的每一行
        String line;

        // 构建代码变更信息
        StringBuilder diffCode = new StringBuilder();
        // 循环读取并构建代码变更信息
        while ((line = bufferedReader.readLine()) != null) {
            diffCode.append(line);
        }

        // 等待进程结束并获取退出码
        int exitCode = process.waitFor();
        // 打印退出码
        System.out.println("Exit code: " + exitCode);
        // 打印评审代码
        System.out.println("diff code：" + diffCode);

        // 2. chatglm 代码评审
        String log = codeReview(diffCode.toString());
        System.out.println("code review: " + log);

    }

    /**
     * 代码评审功能，通过AI模型对给出的代码变更进行评审
     * @param diffCode 代码变更的diff内容
     * @return AI模型返回的评审意见
     * @throws IOException 当网络连接或数据读写操作出错时抛出此异常
     */
    private static String codeReview(String diffCode) throws IOException {
        // 本地 key
        String token = BearerTokenUtils.getToken(AI_KEY);

        // 获取HTTP连接
        HttpURLConnection httpURLConnection = getHttpURLConnection(token);

        // 创建聊天完成请求对象，设置模型和提示信息
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequest.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;

            {
                add(new ChatCompletionRequest.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码为: " + diffCode));
                add(new ChatCompletionRequest.Prompt("user", diffCode));
            }
        });

        // 向服务器发送 JSON 格式的请求数据
        try (OutputStream outputStream = httpURLConnection.getOutputStream()) {
            byte[] jsonInpuStringBytes = JSON.toJSONString(chatCompletionRequest).getBytes(StandardCharsets.UTF_8);
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
        return (response.getChoices().get(0).getMessage().getContent());

    }

    /**
     * 获取HTTP连接
     * @param token 载体令牌，用于授权
     * @return 初始化后的HTTP连接对象
     * @throws IOException 当网络连接出错时抛出此异常
     */
    private static HttpURLConnection getHttpURLConnection(String token) throws IOException {
        URL url = new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // 设置HTTP请求方法和属性
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);
        return connection;
    }

}
