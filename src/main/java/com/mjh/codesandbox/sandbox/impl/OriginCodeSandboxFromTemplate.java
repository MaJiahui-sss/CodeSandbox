package com.mjh.codesandbox.sandbox.impl;

import com.mjh.codesandbox.sandbox.CodeSandboxTemplate;
import com.mjh.codesandbox.sandbox.model.CodeSandboxRequest;
import com.mjh.codesandbox.sandbox.model.CodeSandboxResponse;
import com.mjh.codesandbox.sandbox.model.ExecuteMessage;
import com.mjh.codesandbox.sandbox.util.ProcessUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OriginCodeSandboxFromTemplate extends CodeSandboxTemplate {
    public static final long TIME_OUT = 2000l;
    @Override
    public CodeSandboxResponse execute(CodeSandboxRequest codeSandboxRequest) throws IOException, InterruptedException {
        return super.execute(codeSandboxRequest);
    }

    @Override
    public ArrayList<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) throws IOException {
        ArrayList<ExecuteMessage> executeMessageArrayList =new ArrayList<>();

        for(String input:inputList){
            String excuteCmd = String.format("java -Dfile.encoding=utf-8 -cp %s Main %s",userCodeFile.getParentFile().getAbsolutePath(),input);
            Process executeProcess = Runtime.getRuntime().exec(excuteCmd);
            //超时控制
            new Thread(()->{
                try {
                    Thread.sleep(TIME_OUT);
                    System.out.println("当前已超时，将摧毁程序");
                    executeProcess.destroy();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            ExecuteMessage runExecuteMessage = ProcessUtils.getExecuteMessage(executeProcess, "运行");
            executeMessageArrayList.add(runExecuteMessage);
        }
        return executeMessageArrayList;

    }
}
