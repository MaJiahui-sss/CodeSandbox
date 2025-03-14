package com.mjh.codesandbox.sandbox.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.mjh.codesandbox.sandbox.CodeSandbox;
import com.mjh.codesandbox.sandbox.model.CodeSandboxRequest;
import com.mjh.codesandbox.sandbox.model.CodeSandboxResponse;
import com.mjh.codesandbox.sandbox.model.ExecuteMessage;
import com.mjh.codesandbox.sandbox.model.JudgeInfo;
import com.mjh.codesandbox.sandbox.util.ProcessUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OriginCodeSandbox implements CodeSandbox {

    public static final String GOLBAL_CODE_DIR_NAME = "code";
    public static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";
    public static final long Time_OUT = 2000l;
    @Override
    public CodeSandboxResponse execute(CodeSandboxRequest codeSandboxRequest)  {


        try{
            //0.获取信息
            List<String> inputList = codeSandboxRequest.getInputList();
            String code = codeSandboxRequest.getCode();
            String language = codeSandboxRequest.getLanguage();


            CodeSandboxResponse codeSandboxResponse=new CodeSandboxResponse();





            //1.为每一个用户创建独立的代码目录
            //1.1获取jvm的工作目录
            String userDir = System.getProperty("user.dir");
            String globalCodePathName = userDir + File.separator + GOLBAL_CODE_DIR_NAME;
            // 1.2判断全局代码目录是否存在，没有则新建
            if (!FileUtil.exist(globalCodePathName)) {
                FileUtil.mkdir(globalCodePathName);
            }

            // 1.3把用户的代码隔离存放,创建用户专属的目录
            String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
            String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
            File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

            //2.编译代码
            //String changeCharsetCmd="chcp 65001";
            String compileCmd=String.format("javac -encoding utf-8 %s",userCodeFile.getAbsolutePath());
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);

            ExecuteMessage compileExecuteMessage = ProcessUtils.getExecuteMessage(compileProcess, "编译");
            if(compileExecuteMessage!=null&&compileExecuteMessage.getErrorCode()!=0){
                codeSandboxResponse.setStatus(1);
                return codeSandboxResponse;
            }

            //3.执行代码
            //3.1控制台执行命令
            //一次编译多次执行
            ArrayList<ExecuteMessage> executeMessageArrayList =new ArrayList<>();

            for(String input:inputList){
                String excuteCmd = String.format("java -Dfile.encoding=utf-8 -cp %s Main %s",userCodeParentPath,input);
                Process executeProcess = Runtime.getRuntime().exec(excuteCmd);
                //超时控制
                new Thread(()->{
                    try {
                        Thread.sleep(Time_OUT);
                        System.out.println("当前已超时，将摧毁程序");
                        executeProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                ExecuteMessage runExecuteMessage = ProcessUtils.getExecuteMessage(executeProcess, "运行");
                executeMessageArrayList.add(runExecuteMessage);
            }


            //4.整理输出
            Long maxRuntime=0l;
            ArrayList<String> outputList=new ArrayList<>();
            for(ExecuteMessage executeMessage:executeMessageArrayList){
                if(StrUtil.isNotBlank(executeMessage.getErrorMessage())){
                    codeSandboxResponse.setStatus(2);
                    codeSandboxResponse.setMessage(executeMessage.getErrorMessage());
                    break;
                }
                maxRuntime=Math.max(maxRuntime,executeMessage.getTime());
                outputList.add(executeMessage.getOutput());
            }
            //正常执行
            if(outputList.size()==executeMessageArrayList.size()){
                codeSandboxResponse.setStatus(0);
            }
            codeSandboxResponse.setOutputList(outputList);
            JudgeInfo judgeInfo=new JudgeInfo();
            //judgeInfo.setMessage();


            //5.时间和内存的信息设置
            judgeInfo.setMemory(256l);
            judgeInfo.setTime(maxRuntime);
            codeSandboxResponse.setJudgeInfo(judgeInfo);
            //6.清理文件
            if (FileUtil.exist(userCodeParentPath)) {
                boolean del = FileUtil.del(userCodeParentPath);
                System.out.println(del ? "删除成功" : "删除失败");
            }


            return codeSandboxResponse;
        }catch (Exception e){
            e.printStackTrace();
            return getErrorResponse(e);
        }

    }

    //错误处理方法
    private CodeSandboxResponse getErrorResponse(Throwable e){
        CodeSandboxResponse codeSandboxResponse=new CodeSandboxResponse();
        codeSandboxResponse.setStatus(2);
        codeSandboxResponse.setMessage(e.getMessage());
        codeSandboxResponse.setOutputList(new ArrayList<>());

        codeSandboxResponse.setJudgeInfo(new JudgeInfo());
        return codeSandboxResponse;

    }

}

