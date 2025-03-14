package com.mjh.codesandbox.sandbox.util;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DockerClientBuilder;
import org.bouncycastle.asn1.x509.RSAPublicKeyStructure;

import java.util.List;

public  class DockerUtil {
    public static DockerClient dockerClient ;
    {
        dockerClient = DockerClientBuilder
                .getInstance("tcp://192.168.162.134:2375")
                .build();
    }


    /**
     * 拉取镜像
     * @param dockerClient
     * @param image
     * @throws InterruptedException
     */
    public static void PullImage(DockerClient dockerClient,String image) throws InterruptedException {

        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback(){
            @Override
            public void onNext(PullResponseItem item) {
                System.out.println("下载镜像：  "+item.toString());
                super.onNext(item);
            }
        };
        pullImageCmd.exec(pullImageResultCallback)
                .awaitCompletion();
        System.out.println("下载完成");
    }

    /**
     * 创建容器并返回容器id
     * @param dockerClient
     * @param image
     * @return
     * @throws InterruptedException
     */
    public static String  CreateContainer(DockerClient dockerClient,String image) throws InterruptedException {

        CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd(image);
        CreateContainerResponse createContainerResponse = createContainerCmd.exec();
        System.out.println("创建容器结果："+createContainerResponse.toString());
        return createContainerResponse.getId();
    }

    /**
     * 列出所有的容器
     * @param dockerClient
     * @throws InterruptedException
     */
    public  static void ListContainers(DockerClient dockerClient) throws InterruptedException {
        ListContainersCmd listContainersCmd = dockerClient.listContainersCmd();
        List<Container> containerList = listContainersCmd.withShowAll(true).exec();
        for (Container container : containerList) {
            System.out.println("每个容器信息: "+container.toString());
        }
    }

    /**
     * 启动容器
     * @param dockerClient
     * @param containerId
     * @throws InterruptedException
     */
    public static void StartContainer(DockerClient dockerClient,String containerId) throws InterruptedException {
        dockerClient.startContainerCmd(containerId).exec();
        System.out.println("————————————————容器启动————————————————");
    }

    /**
     * 删除容器
     * @param dockerClient
     * @param containerId
     * @throws InterruptedException
     */
    public static void RemoveContainer(DockerClient dockerClient,String containerId) throws InterruptedException {
        dockerClient.removeContainerCmd(containerId)
                .withForce(true)
                .exec();
        System.out.println("容器已删除");
    }

    /**
     * 删除镜像
     * @param dockerClient
     * @param image
     * @throws InterruptedException
     */
    public static void RemoveImage(DockerClient dockerClient,String image) throws InterruptedException {
        RemoveImageCmd removeImageCmd = dockerClient.removeImageCmd(image);
        removeImageCmd.exec();
        System.out.println(image+"镜像删除");
    }
}
