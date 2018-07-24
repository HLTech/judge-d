package com.hltech.contracts.judged.agent.k8s

import com.google.common.collect.ImmutableMap
import io.fabric8.kubernetes.api.model.Container
import io.fabric8.kubernetes.api.model.Initializers
import io.fabric8.kubernetes.api.model.ListMeta
import io.fabric8.kubernetes.api.model.ObjectMeta
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.PodList
import io.fabric8.kubernetes.api.model.PodSpec
import io.fabric8.kubernetes.api.model.PodStatus
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.dsl.FilterWatchListMultiDeletable
import io.fabric8.kubernetes.client.dsl.MixedOperation
import spock.lang.Specification

import static com.google.common.collect.Lists.newArrayList
import static com.google.common.collect.Maps.newHashMap

class K8sLabelBasedServiceLocatorUT extends Specification {

    private String label
    private K8sLabelBasedServiceLocator serviceLocator
    private KubernetesClient kubernetesClient

    def setup(){
        label = "label"
        kubernetesClient = Mock()
        serviceLocator = new K8sLabelBasedServiceLocator(kubernetesClient, label)
    }

    def ''(){
        given:
            def service1Name = "service1"
            def service1Version = "1.0"
            def service2Name = "service2"
            def service2Version = "2.0"
            def service3Name = "service3"
            def service3Version = "3.0"
            def service4Name = "service4"

            MixedOperation mixedOperation = Mock()
            FilterWatchListMultiDeletable watchList = Mock()
            PodList podList = new PodList("1.0", [
                randomPod(service1Name, "hltech/" + service1Name + ":" + service1Version),
                randomPod(service2Name, "hltech/" + service2Name + ":" + service2Version),
                randomPod(service2Name, service3Name + ":" + service3Version),
                randomPod(service2Name, service4Name),
            ] as List,
                "Pod",
                new ListMeta("1.0", "")
            )
            watchList.withLabel(_) >> watchList
            watchList.list() >> podList

            mixedOperation.inAnyNamespace() >> watchList
            kubernetesClient.pods() >> mixedOperation
        when:
            def services = serviceLocator.locateServices()
        then:
            services.size() == 3
            services.find {it.name == "service1"} .version == "1.0"
            services.find {it.name == "service2"} .version == "2.0"
            services.find {it.name == "service3"} .version == "3.0"
    }

    def randomPod(String serviceName, String imageName) {
        return new Pod(
            "1.0",
            "Pod",
            new ObjectMeta(
                newHashMap(),
                "clisterName",
                "2018",
                5,
                "2018",
                newArrayList(),
                serviceName,
                1,
                new Initializers(newArrayList(), null),
                ImmutableMap.of("1", "2"),
                serviceName,
                "default",
                newArrayList(),
                "1.0",
                "",
                ""
            ),
            new PodSpec(
                1,
                null,
                true,
                newArrayList(
                    new Container(
                        newArrayList(),
                        newArrayList(),
                        newArrayList(),
                        newArrayList(),
                        imageName,
                        "",
                        null,
                        null,
                        "",
                        newArrayList(),
                        null,
                        null,
                        null,
                        true,
                        true,
                        "",
                        "",
                        true,
                        newArrayList(),
                        ""
                    )
                ),
                "",
                newArrayList(),
                true,
                true,
                true,
                "",
                newArrayList(),
                newArrayList(),
                "",
                newHashMap(),
                "always",
                "",
                null,
                "sa",
                "sa",
                "com",
                10,
                newArrayList(),
                newArrayList()
            ),
            new PodStatus(
                newArrayList(),
                newArrayList(),
                "",
                newArrayList(),
                "",
                "running",
                "",
                "qosClass",
                "reason",
                "2018"
            )
        )
    }
}
