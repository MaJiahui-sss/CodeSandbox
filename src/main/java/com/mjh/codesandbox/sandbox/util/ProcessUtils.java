package com.mjh.codesandbox.sandbox.util;

import com.mjh.codesandbox.sandbox.model.ExecuteMessage;
import org.springframework.util.StopWatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessUtils {
    public static ExecuteMessage getExecuteMessage(Process process, String type) throws IOException {
        ExecuteMessage executeMessage = new ExecuteMessage();
//        message.setTime(20:01);
//        message.setMemory();

        //2.1等待编译完成，拿到错误码
        int errorCode = 0;
        StopWatch stopWatch = new StopWatch();
        try {

            stopWatch.start();
            errorCode = process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executeMessage.setErrorCode(errorCode);
        if (errorCode == 0) {
            //成功
            System.out.println(type+"成功");
            //拿到的正常输出
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream(),"UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = null;
            StringBuilder sb = new StringBuilder();
            while((line=bufferedReader.readLine())!=null){
                sb.append(line);
            }
            System.out.println(sb.toString());
            executeMessage.setOutput(sb.toString());

        }else{
            System.out.print(type+"失败：");
            System.out.println("错误码为："+errorCode);
            //拿到正常输出
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream(),"UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = null;
            StringBuilder sb = new StringBuilder();
            while((line=bufferedReader.readLine())!=null){
                sb.append(line);
            }
            System.out.println("正常输出为"+sb.toString());
            //拿到错误输出
            InputStreamReader errorInputStreamReader = new InputStreamReader(process.getErrorStream(),"UTF-8");
            BufferedReader errorBufferedReader = new BufferedReader(errorInputStreamReader);
            String errorLine = null;
            StringBuilder errorSB = new StringBuilder();
            while((errorLine=errorBufferedReader.readLine())!=null){
                errorSB.append(errorLine);
            }
            System.out.println("错误输出为"+errorSB.toString());
            executeMessage.setErrorMessage(errorSB.toString());
        }
        stopWatch.stop();
        executeMessage.setTime(stopWatch.getTotalTimeMillis());
        return executeMessage;
    }
}
