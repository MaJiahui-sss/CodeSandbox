package com.mjh.codesandbox.sandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.mjh.codesandbox.sandbox.model.CodeSandboxRequest;
import com.mjh.codesandbox.sandbox.model.CodeSandboxResponse;
import com.mjh.codesandbox.sandbox.model.ExecuteMessage;
import com.mjh.codesandbox.sandbox.model.JudgeInfo;
import com.mjh.codesandbox.sandbox.util.ProcessUtils;
import org.springframework.util.StopWatch;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CodeSandboxTemplate implements CodeSandbox{
    public static final String GOLBAL_CODE_DIR_NAME = "code";
    public static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";
    public static final long TIME_OUT = 5000l;

    /**
     * 保存代码文件
     * @param code
     * @return
     */
    public File saveFile(String code){
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
        return userCodeFile;
    }

    /**
     * 编译代码
     * @param userCodeFile
     * @return
     * @throws IOException
     */
    public ExecuteMessage compileFile(File userCodeFile) throws IOException {
        System.out.println(userCodeFile.getAbsolutePath());
        String compileCmd=String.format("javac -encoding utf-8 %s",userCodeFile.getAbsolutePath());
        Process compileProcess = Runtime.getRuntime().exec(compileCmd);

        ExecuteMessage compileExecuteMessage = ProcessUtils.getExecuteMessage(compileProcess, "编译");
        return compileExecuteMessage;

    }

    /**
     * 执行代码
     * @param userCodeFile
     * @param inputList
     * @return
     * @throws IOException
     */
    public ArrayList<ExecuteMessage> runFile(File userCodeFile,List<String> inputList) throws IOException, InterruptedException {
        //创建dockerClient
        DockerClient dockerClient = DockerClientBuilder
                .getInstance("tcp://192.168.162.134:2375")
                .build();
        //创建容器
        String image = "openjdk:8-alpine";
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory(100 * 1000 * 1000L);
        hostConfig.withMemorySwap(0L);
        hostConfig.withCpuCount(1L);
        //hostConfig.withSecurityOpts(Arrays.asList("seccomp=安全管理配置字符串"));
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app")));
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withNetworkDisabled(true)
                .withReadonlyRootfs(true)
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .exec();
        System.out.println(createContainerResponse);
        String containerId = createContainerResponse.getId();
        //启动容器
        dockerClient.startContainerCmd(containerId).exec();
        //3.执行代码
        //3.1控制台执行命令
        //一次编译多次执行
        ArrayList<ExecuteMessage> executeMessageArrayList =new ArrayList<>();

        for(String input:inputList){
            //拼接cmd语句
            String[] inputArray = input.split(" ");
            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, inputArray);
            //容器内执行cmd
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdArray)
                    .withAttachStderr(true)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .exec();
            System.out.println("创建执行命令：" + execCreateCmdResponse);
            String execId = execCreateCmdResponse.getId();
            //封装执行结果
            ExecuteMessage executeMessage = new ExecuteMessage();
            final String[] message = {null};
            final String[] errorMessage = {null};
            final long[] time =new long[2];
            final boolean[] timeout = {true};
            ExecStartResultCallback execStartResultCallback=new ExecStartResultCallback(){
                private final StringBuilder messageBuilder = new StringBuilder();
                private final StringBuilder errorMessageBuilder = new StringBuilder();
                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    if (StreamType.STDERR.equals(streamType)) {
                        errorMessageBuilder.append(new String(frame.getPayload()))  ;
                        //System.out.println("输出错误结果：" + errorMessage[0]);
                    } else {
                        messageBuilder.append(new String(frame.getPayload()))  ;
                        //System.out.println("输出结果：" + message[0]);
                    }
                    super.onNext(frame);
                }

                @Override
                public void onComplete() {
                    timeout[0] = false;
                    message[0]=messageBuilder.toString();
                    String errorMessageBuilderString = errorMessageBuilder.toString();
                    if(errorMessageBuilderString!=null&&errorMessageBuilderString!=""){
                        errorMessage[0]=errorMessageBuilderString;
                    }
                    super.onComplete();
                }
            };
            //时间统计
            StopWatch stopWatch = new StopWatch();
            //内存统计
            final long[] maxMemory = {0l};
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {

                @Override
                public void onNext(Statistics statistics) {
                    System.out.println("内存占用：" + statistics.getMemoryStats().getUsage());
                    maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(), maxMemory[0]);
                }

                @Override
                public void close() throws IOException {

                }

                @Override
                public void onStart(Closeable closeable) {

                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {

                }
            });
            //统计开始
            statsCmd.exec(statisticsResultCallback);


            try {
                //开始计时
                stopWatch.start();

                dockerClient.execStartCmd(execId)
                        .exec(execStartResultCallback)
                        .awaitCompletion();
                //.awaitCompletion(TIME_OUT, TimeUnit.MICROSECONDS);//最多等待执行Time_out时间，如果执行不完，程序不再等待
                //终止计时
                stopWatch.stop();
                time[0] = stopWatch.getTotalTimeMillis();
                //终止统计
                statsCmd.close();

            } catch (InterruptedException e){
                System.out.println("程序执行异常");
                throw new RuntimeException(e);
            }

            executeMessage.setOutput(message[0]);
            executeMessage.setErrorMessage(errorMessage[0]);
            executeMessage.setTime(time[0]);
            executeMessage.setMemory(maxMemory[0]);
            executeMessageArrayList.add(executeMessage);


        }
        //删除容器
        Thread.sleep(500l);
        dockerClient.removeContainerCmd(containerId)
                .withForce(true)
                .exec();
        System.out.println(containerId+"容器已删除");
        return executeMessageArrayList;

    }


    public CodeSandboxResponse getOutputResponse(CodeSandboxResponse codeSandboxResponse,List<ExecuteMessage> executeMessageArrayList){

        Long maxRuntime=0l;
        Long maxMemory=0l;
        ArrayList<String> outputList=new ArrayList<>();
        for(ExecuteMessage executeMessage:executeMessageArrayList){
            if(StrUtil.isNotBlank(executeMessage.getErrorMessage())){
                //存在运行出错
                codeSandboxResponse.setStatus(2);
                codeSandboxResponse.setMessage(executeMessage.getErrorMessage());
                break;
            }

            if(executeMessage.getTime()!=null)maxRuntime=Math.max(maxRuntime,executeMessage.getTime());
            if(executeMessage.getMemory()!=null)maxMemory=Math.max(maxMemory,executeMessage.getMemory());
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
        judgeInfo.setMemory(maxMemory);
        judgeInfo.setTime(maxRuntime);
        codeSandboxResponse.setJudgeInfo(judgeInfo);

        return codeSandboxResponse;
    }

    /**
     * 清理文件
     * @param userCodeFile
     * @return
     */
    public boolean deleteFile(File userCodeFile){

        if (FileUtil.exist(userCodeFile.getParentFile().getAbsolutePath())) {
            boolean del = FileUtil.del(userCodeFile.getParentFile().getAbsolutePath());

            System.out.println(del ? "删除成功" : "删除失败");
            return del;
        }
        return false;
    }

    @Override
    public CodeSandboxResponse execute(CodeSandboxRequest codeSandboxRequest) throws IOException, InterruptedException {
        try{
            //0.获取信息
            List<String> inputList = codeSandboxRequest.getInputList();
            String code = codeSandboxRequest.getCode();
            String language = codeSandboxRequest.getLanguage();
            CodeSandboxResponse codeSandboxResponse=new CodeSandboxResponse();
            //1.为每一个用户创建独立的代码目录
            File userCodeFile = saveFile(code);
            //2.编译代码
            ExecuteMessage compileExecuteMessage = compileFile(userCodeFile);
            if(compileExecuteMessage!=null&&compileExecuteMessage.getErrorCode()!=0){
                codeSandboxResponse.setStatus(1);//编译失败
                return codeSandboxResponse;
            }
            //3执行代码
            ArrayList<ExecuteMessage> executeMessageArrayList = runFile(userCodeFile, inputList);
            //4.整理输出
            codeSandboxResponse = getOutputResponse(codeSandboxResponse, executeMessageArrayList);
            //5.清理文件
            boolean del = deleteFile(userCodeFile);
            if(!del){
                System.out.println("文件删除失败");
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
