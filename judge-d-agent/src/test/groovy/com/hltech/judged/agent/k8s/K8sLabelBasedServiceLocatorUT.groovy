package com.hltech.judged.agent.k8s


import io.fabric8.kubernetes.api.model.Container
import io.fabric8.kubernetes.api.model.Initializers
import io.fabric8.kubernetes.api.model.ListMeta
import io.fabric8.kubernetes.api.model.Namespace
import io.fabric8.kubernetes.api.model.NamespaceList
import io.fabric8.kubernetes.api.model.NamespaceSpec
import io.fabric8.kubernetes.api.model.NamespaceStatus
import io.fabric8.kubernetes.api.model.ObjectMeta
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.PodList
import io.fabric8.kubernetes.api.model.PodSpec
import io.fabric8.kubernetes.api.model.PodStatus
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.dsl.FilterWatchListMultiDeletable
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation
import spock.lang.Specification

import static com.google.common.collect.Lists.newArrayList
import static com.google.common.collect.Maps.newHashMap
import static com.google.common.collect.Sets.newHashSet

class K8sLabelBasedServiceLocatorUT extends Specification {

    private String label
    private KubernetesClient kubernetesClient

    def setup(){
        label = "label"
        kubernetesClient = Mock()
    }

    def 'should find all but excluded pods' (){
        given:
            def serviceLocator = new K8sLabelBasedServiceLocator(kubernetesClient, label, newHashSet("excluded"), newHashSet())

            MixedOperation mixedOperation = Mock()
            FilterWatchListMultiDeletable watchList = Mock()
            PodList podList = new PodList("1.0", [
                randomPod("s1", "hltech/s1:1", "default", new HashMap<String, String>()),
                randomPod("s2", "hltech/s2:2", "excluded", new HashMap<String, String>()),
            ] as List,
                "Pod",
                new ListMeta("1.0", "")
            )
            watchList.withLabel(label) >> watchList
            watchList.list() >> podList

            mixedOperation.inAnyNamespace() >> watchList
            kubernetesClient.pods() >> mixedOperation

            NonNamespaceOperation namespacesOperation = Mock()
            namespacesOperation.list() >> new NamespaceList("1.0", [
                newNamespace("default"),
                newNamespace("excluded")
            ] as List, "Namespace", new ListMeta("1.0", ""))
            kubernetesClient.namespaces() >> namespacesOperation
        when:
            def services = serviceLocator.locateServices()
        then:
            services.size() == 1
            services.find {it.name == "s1"} .version == "1"
    }

    def 'should find only inlucded pods' (){
        given:
            def serviceLocator = new K8sLabelBasedServiceLocator(kubernetesClient, label, newHashSet(), newHashSet("included"))

            MixedOperation mixedOperation = Mock()
            FilterWatchListMultiDeletable watchList = Mock()
            PodList podList = new PodList("1.0", [
                randomPod("s1", "hltech/s1:1", "default", new HashMap<String, String>()),
                randomPod("s2", "hltech/s2:2", "included", new HashMap<String, String>()),
            ] as List,
                "Pod",
                new ListMeta("1.0", "")
            )
            watchList.withLabel(label) >> watchList
            watchList.list() >> podList

            mixedOperation.inAnyNamespace() >> watchList
            kubernetesClient.pods() >> mixedOperation

            NonNamespaceOperation namespacesOperation = Mock()
            namespacesOperation.list() >> new NamespaceList("1.0", [
                newNamespace("default"),
                newNamespace("included")
            ] as List, "Namespace", new ListMeta("1.0", ""))
            kubernetesClient.namespaces() >> namespacesOperation
        when:
            def services = serviceLocator.locateServices()
        then:
            services.size() == 1
            services.find {it.name == "s2"} .version == "2"
    }

    def 'should find all correctly configured services'(){
        given:
            def serviceLocator = new K8sLabelBasedServiceLocator(kubernetesClient, label, newHashSet(), newHashSet())
            def service1Name = "service1"
            def service1Version = "1.0"
            def service2Name = "service2"
            def service2Version = "2.0"
            def service3Name = "service3"
            def service3Version = "3.0"
            def service4Name = "service4"
            def service5Name = "service5"
            def service5Version = "service6"

            MixedOperation mixedOperation = Mock()
            FilterWatchListMultiDeletable watchList = Mock()
            PodList podList = new PodList("1.0", [
                randomPod(service1Name, "hltech/" + service1Name + ":" + service1Version, "default", new HashMap<String, String>()),
                randomPod(service2Name, "hltech/" + service2Name + ":" + service2Version, "default", new HashMap<String, String>()),
                randomPod(service3Name, service3Name + ":" + service3Version, "default", new HashMap<String, String>()),
                randomPod(service4Name, service4Name, "default", new HashMap<String, String>()),
                randomPod(service5Name, "hltech/" + service5Name + ":" + service5Version, "default", ['exclude-from-judged-jurisdiction': 'true']),
            ] as List,
                "Pod",
                new ListMeta("1.0", "")
            )
            watchList.withLabel(label) >> watchList
            watchList.list() >> podList

            mixedOperation.inAnyNamespace() >> watchList
            kubernetesClient.pods() >> mixedOperation

            NonNamespaceOperation namespacesOperation = Mock()
            namespacesOperation.list() >> new NamespaceList("1.0", [newNamespace("default")] as List, "Namespace", new ListMeta("1.0", ""))
            kubernetesClient.namespaces() >> namespacesOperation
        when:
            def services = serviceLocator.locateServices()
        then:
            services.size() == 3
            services.find {it.name == "service1"} .version == "1.0"
            services.find {it.name == "service2"} .version == "2.0"
            services.find {it.name == "service3"} .version == "3.0"
    }

    def newNamespace(String namespaceName) {
        def meta = new ObjectMeta()
        meta.setName(namespaceName)
        return new Namespace(
            "1.0",
            "Namespace",
            meta,
            new NamespaceSpec(
                ["kubernetes"] as List
            ),
            new NamespaceStatus(
                "Active"
            )
        )
    }

    def randomPod(String serviceName, String imageName, String namespace, Map<String, String> podLabels) {
        return new Pod(
            "1.0",
            "Pod",
            new ObjectMeta(
                newHashMap(),
                "clusterName",
                "2018",
                5,
                "2018",
                newArrayList(),
                serviceName,
                1,
                new Initializers(newArrayList(), null),
                podLabels,
                serviceName,
                namespace,
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
