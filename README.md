# CodeSandbox 项目 README

## 一、项目概述
CodeSandbox 是一个用于在线运行和测试 Java 代码的沙箱项目。该项目提供了安全、隔离的环境来执行用户提交的代码，同时支持代码的编译、运行以及对执行结果的详细反馈，包括执行时间和内存消耗等信息。项目包含多种代码沙箱实现，如原生沙箱（OriginCodeSandbox）、基于 Docker 的沙箱（DockerCodeSandbox）等。

## 二、项目结构
### 主要目录和文件
- `src/main/java/com/mjh/codesandbox/sandbox`：包含代码沙箱的核心实现类。
  - `impl`：存放不同类型的代码沙箱实现，如 `OriginCodeSandbox`、`DockerCodeSandbox` 等。
  - `model`：定义了项目中使用的数据模型，如 `JudgeInfo`、`ExecuteMessage` 等。
  - `util`：包含工具类，如 `ProcessUtils` 用于处理进程执行信息。
  - `Security`：包含安全管理相关的类，如 `MySecurityManeger` 用于权限控制。
  - `CodeSandboxTemplate.java`：代码沙箱的模板类，封装了通用的代码执行流程。
- `target`：Maven 构建输出目录，包含编译后的类文件和测试报告。
- `.idea`：IntelliJ IDEA 项目配置文件。
- `pom.xml`：Maven 项目配置文件，定义了项目的依赖和构建配置。

## 三、环境要求
- **Java 版本**：Java 8
- **Maven**：用于项目的依赖管理和构建。
- **Docker**：如果使用基于 Docker 的沙箱（DockerCodeSandbox），需要安装 Docker 并启动 Docker 服务。

## 四、项目依赖
项目使用 Maven 管理依赖，主要依赖包括：
- Spring Boot Web：用于构建 Web 应用。
- Spring Boot Test：用于单元测试。
- Lombok：简化 Java 代码的编写。
- Docker Java：用于与 Docker 进行交互。

详细依赖信息可查看 `pom.xml` 文件。

## 五、使用方法
### 1. 克隆项目
```bash
git clone <项目仓库地址>
cd CodeSandbox
```

### 2. 构建项目
使用 Maven 构建项目：
```bash
mvn clean package
```

### 3. 运行测试
项目包含单元测试，可以使用以下命令运行测试：
```bash
mvn test
```
测试结果将输出到 `target/surefire-reports` 目录下。

### 4. 代码沙箱使用示例
以下是一个简单的使用代码沙箱的示例：
```java
import com.mjh.codesandbox.sandbox.CodeSandbox;
import com.mjh.codesandbox.sandbox.impl.OriginCodeSandbox;
import com.mjh.codesandbox.sandbox.model.CodeSandboxRequest;
import com.mjh.codesandbox.sandbox.model.CodeSandboxResponse;

import java.util.Arrays;
import java.util.List;

public class CodeSandboxExample {
    public static void main(String[] args) {
        // 创建代码沙箱实例
        CodeSandbox codeSandbox = new OriginCodeSandbox();

        // 准备代码和输入
        String code = "public class Main { public static void main(String[] args) { System.out.println(\"Hello, World!\"); } }";
        List<String> inputList = Arrays.asList("");

        // 创建代码沙箱请求
        CodeSandboxRequest codeSandboxRequest = new CodeSandboxRequest();
        codeSandboxRequest.setCode(code);
        codeSandboxRequest.setInputList(inputList);
        codeSandboxRequest.setLanguage("java");

        // 执行代码
        CodeSandboxResponse codeSandboxResponse = codeSandbox.execute(codeSandboxRequest);

        // 输出执行结果
        System.out.println("执行状态：" + codeSandboxResponse.getStatus());
        System.out.println("输出结果：" + codeSandboxResponse.getOutputList());
        System.out.println("判题信息：" + codeSandboxResponse.getJudgeInfo());
    }
}
```

## 六、代码沙箱实现说明
### 1. 原生代码沙箱（OriginCodeSandbox）
- **原理**：在本地系统上直接执行 Java 代码，通过创建独立的代码目录来隔离用户代码，使用 `Runtime.getRuntime().exec()` 方法执行编译和运行命令。
- **特点**：简单易用，但缺乏对资源的严格控制和隔离。

### 2. 基于 Docker 的代码沙箱（DockerCodeSandbox）
- **原理**：使用 Docker 容器来执行 Java 代码，通过创建 Docker 容器并挂载用户代码目录，在容器内执行编译和运行命令，同时可以对容器的资源进行限制。
- **特点**：提供了更好的资源隔离和安全性，但需要安装和配置 Docker。

### 3. 代码沙箱模板（CodeSandboxTemplate）
- **原理**：封装了代码沙箱的通用执行流程，包括保存代码文件、编译代码、执行代码、整理输出和清理文件等步骤。
- **特点**：提高了代码的复用性和可维护性。

## 七、注意事项
- 如果使用基于 Docker 的沙箱，需要确保 Docker 服务正常运行，并且 `DockerCodeSandbox` 中配置的 Docker 地址（`tcp://192.168.162.134:2375`）与实际环境一致。
- 项目中使用的字符编码为 UTF-8，确保系统和 IDE 的编码设置一致，避免出现编码问题。


以上 README 文件详细介绍了 CodeSandbox 项目的基本信息、使用方法、代码沙箱实现等内容，希望能帮助你更好地理解和使用该项目。
