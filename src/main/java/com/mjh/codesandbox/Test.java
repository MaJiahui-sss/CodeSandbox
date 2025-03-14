package com.mjh.codesandbox;

import com.mjh.codesandbox.sandbox.CodeSandbox;
import com.mjh.codesandbox.sandbox.impl.DockerCodeSandbox;
import com.mjh.codesandbox.sandbox.impl.DockerCodeSandboxFromTemplate;
import com.mjh.codesandbox.sandbox.impl.OriginCodeSandbox;
import com.mjh.codesandbox.sandbox.impl.OriginCodeSandboxFromTemplate;
import com.mjh.codesandbox.sandbox.model.CodeSandboxRequest;
import com.mjh.codesandbox.sandbox.model.CodeSandboxResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
@Slf4j
public class Test {
    public static void main(String[] args) throws IOException, InterruptedException {
        CodeSandboxRequest codeSandboxRequest = new CodeSandboxRequest();
        codeSandboxRequest.setInputList(Arrays.asList(new String[]{"1 2 3","3 4"}));
        codeSandboxRequest.setCode(new String("public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"输入的参数是：\");\n" +
                "        for (int i = 0; i < args.length; i++) {\n" +
                "            System.out.println(\"第\"+i+\"个参数是\"+args[i]);\n" +
                "        }\n" +
                "    }\n" +
                "}\n"));
        codeSandboxRequest.setLanguage("Java");
        CodeSandbox codeSandbox = new DockerCodeSandboxFromTemplate();
        CodeSandboxResponse codeSandboxResponse = codeSandbox.execute(codeSandboxRequest);
        List<String> outputList = codeSandboxResponse.getOutputList();
        for (String s : outputList) {
            System.out.println(s);
            log.info(s);
        }
        System.out.println("测试成功");
        log.info("测试成功");
    }
}
