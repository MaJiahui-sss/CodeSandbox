package com.mjh.codesandbox.sandbox;

import com.mjh.codesandbox.sandbox.model.CodeSandboxRequest;
import com.mjh.codesandbox.sandbox.model.CodeSandboxResponse;

import java.io.IOException;

public interface CodeSandbox {
    public CodeSandboxResponse execute(CodeSandboxRequest codeSandboxRequest) throws IOException, InterruptedException;
}
