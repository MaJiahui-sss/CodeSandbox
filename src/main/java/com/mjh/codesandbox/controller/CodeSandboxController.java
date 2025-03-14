package com.mjh.codesandbox.controller;

import com.mjh.codesandbox.sandbox.CodeSandbox;
import com.mjh.codesandbox.sandbox.impl.DockerCodeSandbox;
import com.mjh.codesandbox.sandbox.impl.DockerCodeSandboxFromTemplate;
import com.mjh.codesandbox.sandbox.model.CodeSandboxRequest;
import com.mjh.codesandbox.sandbox.model.CodeSandboxResponse;
import org.aopalliance.reflect.Code;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class CodeSandboxController {

    CodeSandbox codeSandbox;

    @PostMapping("/executeCode")
    public CodeSandboxResponse executeCode(@RequestBody CodeSandboxRequest codeSandboxRequest){
        codeSandbox=new DockerCodeSandboxFromTemplate();
        if(codeSandboxRequest==null)return null;
        CodeSandboxResponse codeSandboxResponse =null;
        try {
            codeSandboxResponse = codeSandbox.execute(codeSandboxRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return codeSandboxResponse;

    }
}
