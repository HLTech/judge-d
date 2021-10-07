package com.hltech.judged.agent.k8s

import io.fabric8.kubernetes.api.model.ContainerBuilder
import io.fabric8.kubernetes.api.model.ListMetaBuilder
import io.fabric8.kubernetes.api.model.Namespace
import io.fabric8.kubernetes.api.model.NamespaceList
import io.fabric8.kubernetes.api.model.NamespaceSpec
import io.fabric8.kubernetes.api.model.NamespaceStatusBuilder
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.PodList
import io.fabric8.kubernetes.api.model.PodSpecBuilder
import io.fabric8.kubernetes.api.model.PodStatusBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.dsl.FilterWatchListMultiDeletable
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation
import spock.lang.Specification

import static com.google.common.collect.Sets.newHashSet

class K8sLabelBasedServiceLocatorUT extends Specification {

    def label = 'label'
    def kubernetesClient = Mock(KubernetesClient)

    def 'should find all but excluded pods'() {
        given:
            def serviceLocator = new K8sLabelBasedServiceLocator(kubernetesClient, label, newHashSet('excluded'), newHashSet())

            def mixedOperation = Mock(MixedOperation)
            def watchList = Mock(FilterWatchListMultiDeletable)
            def podList = new PodList('1.0', [
                buildPod('s1', 'hltech/s1:1', 'default', [:]),
                buildPod('s2', 'hltech/s2:2', 'excluded', [:]),
            ],
                'Pod',
                buildListMeta()
            )
            watchList.withLabel(label) >> watchList
            watchList.list() >> podList

            mixedOperation.inAnyNamespace() >> watchList
            kubernetesClient.pods() >> mixedOperation

            NonNamespaceOperation namespacesOperation = Mock()
            namespacesOperation.list() >> new NamespaceList('1.0', [
                newNamespace('default'),
                newNamespace('excluded')
            ],
                'Namespace',
                buildListMeta()
            )
            kubernetesClient.namespaces() >> namespacesOperation
        when:
            def services = serviceLocator.locateServices()
        then:
            services.size() == 1
            services.find { it.name == 's1' }.version == '1'
    }

    def 'should find only included pods'() {
        given:
            def serviceLocator = new K8sLabelBasedServiceLocator(kubernetesClient, label, newHashSet(), newHashSet('included'))

            def mixedOperation = Mock(MixedOperation)
            def watchList = Mock(FilterWatchListMultiDeletable)
            def podList = new PodList('1.0', [
                buildPod('s1', 'hltech/s1:1', 'default', [:]),
                buildPod('s2', 'hltech/s2:2', 'included', [:]),
            ],
                'Pod',
                buildListMeta()
            )
            watchList.withLabel(label) >> watchList
            watchList.list() >> podList

            mixedOperation.inAnyNamespace() >> watchList
            kubernetesClient.pods() >> mixedOperation

            NonNamespaceOperation namespacesOperation = Mock()
            namespacesOperation.list() >> new NamespaceList('1.0', [
                newNamespace('default'),
                newNamespace('included')
            ],
                'Namespace',
                buildListMeta()
            )
            kubernetesClient.namespaces() >> namespacesOperation
        when:
            def services = serviceLocator.locateServices()
        then:
            services.size() == 1
            services.find { it.name == 's2' }.version == '2'
    }

    def 'should find all correctly configured services'() {
        given:
            def serviceLocator = new K8sLabelBasedServiceLocator(kubernetesClient, label, newHashSet(), newHashSet())
            def service1Name = 'service1'
            def service1Version = '1.0'
            def service2Name = 'service2'
            def service2Version = '2.0'
            def service3Name = 'service3'
            def service3Version = '3.0'
            def service4Name = 'service4'
            def service5Name = 'service5'
            def service5Version = 'service6'

            def mixedOperation = Mock(MixedOperation)
            def watchList = Mock(FilterWatchListMultiDeletable)
            def podList = new PodList('1.0', [
                buildPod(service1Name, 'hltech/' + service1Name + ':' + service1Version, 'default', new HashMap<String, String>()),
                buildPod(service2Name, 'hltech/' + service2Name + ':' + service2Version, 'default', new HashMap<String, String>()),
                buildPod(service3Name, service3Name + ':' + service3Version, 'default', new HashMap<String, String>()),
                buildPod(service4Name, service4Name, 'default', new HashMap<String, String>()),
                buildPod(service5Name, 'hltech/' + service5Name + ':' + service5Version, 'default', ['exclude-from-judged-jurisdiction': 'true']),
            ],
                'Pod',
                buildListMeta()
            )
            watchList.withLabel(label) >> watchList
            watchList.list() >> podList

            mixedOperation.inAnyNamespace() >> watchList
            kubernetesClient.pods() >> mixedOperation

            NonNamespaceOperation namespacesOperation = Mock()
            namespacesOperation.list() >> new NamespaceList('1.0', [newNamespace('default')], 'Namespace', buildListMeta())
            kubernetesClient.namespaces() >> namespacesOperation
        when:
            def services = serviceLocator.locateServices()
        then:
            services.size() == 3
            services.find { it.name == 'service1' }.version == '1.0'
            services.find { it.name == 'service2' }.version == '2.0'
            services.find { it.name == 'service3' }.version == '3.0'
    }

    def newNamespace(String namespaceName) {
        def meta = new ObjectMetaBuilder()
            .withName(namespaceName)
            .withNamespace(namespaceName)
            .build()
        return new Namespace(
            '1.0',
            'Namespace',
            meta,
            new NamespaceSpec(['kubernetes']),
            buildNamespaceStatus('Active')
        )
    }

    def buildPod(String serviceName, String imageName, String namespace, Map<String, String> podLabels) {
        return new Pod(
            '1.0',
            'Pod',
            buildObjectMeta(serviceName, namespace, podLabels),
            buildPodSpec(imageName),
            buildPodStatus('running')
        )
    }

    def buildListMeta() {
        new ListMetaBuilder()
            .withResourceVersion('1.0')
            .build()
    }

    def buildObjectMeta(String serviceName, String namespace, Map<String, String> podLabels) {
        new ObjectMetaBuilder()
            .withName(serviceName)
            .withNamespace(namespace)
            .withLabels(podLabels)
            .build()
    }

    def buildPodSpec(String imageNme) {
        new PodSpecBuilder()
            .withContainers([buildContainer(imageNme)])
            .build()
    }

    def buildContainer(String imageName) {
        new ContainerBuilder()
            .withImage(imageName)
            .build()
    }

    def buildPodStatus(String phase) {
        new PodStatusBuilder()
            .withPhase(phase)
            .build()
    }

    def buildNamespaceStatus(String phase) {
        new NamespaceStatusBuilder()
            .withPhase(phase)
            .build()
    }
}
