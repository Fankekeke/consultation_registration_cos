package cc.mrbird.febs.cos.controller;

import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.service.SseService;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.alibaba.dashscope.utils.JsonUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/cos/ai/")
public class AliAliController {

    @Resource
    private SseService sseService;

    @Resource
    private Generation generation;

    /**
     * 测试demo
     *
     * @param content 用书输入文本内容
     */
    @PostMapping(value = "aliTyqw", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter send(@RequestBody String content) throws Exception {
        SseEmitter emitter = new SseEmitter(); // 创建 SseEmitter 对象

        CompletableFuture.runAsync(() -> {
            try {
                // 用户与模型的对话历史。list中的每个元素形式为{"role":角色, "content": 内容}。
                Message userMessage = Message.builder()
                        .role(Role.USER.getValue())
                        .content(content)
                        .build();

                GenerationParam param = GenerationParam.builder()
                        // 指定用于对话的通义千问模型名
                        .model("qwen-plus")
                        .messages(Arrays.asList(userMessage))
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        // 生成过程中核采样方法概率阈值
                        .topP(0.8)
                        // 阿里云控制台DASHSCOPE获取的api-key
                        .apiKey("sk-")
                        // 启用互联网搜索
                        .enableSearch(true)
                        .build();

                GenerationResult generationResult = generation.call(param);

                // 获取所有 content 内容并放入 List 中
                List<String> allContents = generationResult.getOutput().getChoices().stream()
                        .map(choice -> choice.getMessage().getContent())
                        .collect(Collectors.toList());

                for (String msg : allContents) {
                    emitter.send(msg); // 发送每条消息给客户端
                }

                emitter.complete(); // 完成此次 SSE 连接
            } catch (Exception e) {
                log.error("Error occurred while calling AI model: ", e);
                try {
                    emitter.send("Error: " + e.getMessage());
                } catch (IOException ioException) {
                    log.error("Failed to send error message via SSE", ioException);
                }
                emitter.completeWithError(e); // 出错时完成连接
            }
        });

        return emitter;
    }

}