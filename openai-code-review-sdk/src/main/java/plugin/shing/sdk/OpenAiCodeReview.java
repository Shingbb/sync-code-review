package plugin.shing.sdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * OpenAiCodeReview 类用于演示如何在 Java 中使用 OpenAI API 进行代码评审
 * @author shing
 */
public class OpenAiCodeReview {
    /**
     * 程序入口
     * @param args 命令行参数
     * @throws IOException 当文件读写错误时抛出
     * @throws InterruptedException 当线程被中断时抛出
     */
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
        System.out.println("评审代码：" + diffCode);

        // 输出版本
        System.out.println("检出代码V2.0");
    }

}
