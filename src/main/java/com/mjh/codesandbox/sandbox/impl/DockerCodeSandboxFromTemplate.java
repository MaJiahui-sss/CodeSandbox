package com.mjh.codesandbox.sandbox.impl;

import com.mjh.codesandbox.sandbox.CodeSandboxTemplate;
import com.mjh.codesandbox.sandbox.model.CodeSandboxRequest;
import com.mjh.codesandbox.sandbox.model.CodeSandboxResponse;

import java.io.IOException;

public class DockerCodeSandboxFromTemplate extends CodeSandboxTemplate {
    @Override
    public CodeSandboxResponse execute(CodeSandboxRequest codeSandboxRequest) throws IOException, InterruptedException {
        return super.execute(codeSandboxRequest);
    }
}
