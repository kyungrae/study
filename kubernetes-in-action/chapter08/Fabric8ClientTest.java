package me.hama;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.Arrays;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        KubernetesClient client = new DefaultKubernetesClient();

        PodList pods = client.pods().inNamespace("default").list();
        pods.getItems().stream()
                .forEach(s -> System.out.println("Found pod: " + s.getMetadata().getName()));

        System.out.println("Creating a pod");

        Pod pod = client.pods().inNamespace("default").create(
                new PodBuilder()
                        .withNewMetadata()
                        .withName("programmatically-created-pod")
                        .endMetadata()
                        .withNewSpec()
                        .addNewContainer()
                        .withName("main")
                        .withImage("busybox")
                        .withCommand(Arrays.asList("sleep", "99999"))
                        .endContainer()
                        .endSpec()
                        .build()
        );
        System.out.println("Created pod: " + pod);

        client.pods().inNamespace("default")
                .withName("programmatically-created-pod")
                .edit(p ->
                        new PodBuilder(p)
                                .editMetadata()
                                .addToLabels("foo", "bar")
                                .endMetadata()
                                .build()
                );
        System.out.println("Added lablel foo-bar to pod");

        System.out.println("Waiting 1 minute before deleting pod...");
        Thread.sleep(60000);
        
        client.pods().inNamespace("default")
                .withName("programmatically-created-pod")
                .delete();
        System.out.println("Deleted the pod");
    }
}
