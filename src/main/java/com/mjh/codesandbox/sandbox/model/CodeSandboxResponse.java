package com.mjh.codesandbox.sandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CodeSandboxResponse {
    private List<String> outputList;

    /**
     * 接口信息
     */
    private String message;

    /**
     * 执行状态
     */
    private Integer status;//  0成功 1编译错误 2运行错误 3答案错误

    /**
     * 判题信息
     */
    private JudgeInfo judgeInfo;

    public List<String> getOutputList() {
        return outputList;
    }

    public void setOutputList(List<String> outputList) {
        this.outputList = outputList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public JudgeInfo getJudgeInfo() {
        return judgeInfo;
    }

    public void setJudgeInfo(JudgeInfo judgeInfo) {
        this.judgeInfo = judgeInfo;
    }
}
