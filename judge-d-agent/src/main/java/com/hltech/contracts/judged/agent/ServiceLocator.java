package com.hltech.contracts.judged.agent;

import java.util.Set;

public interface ServiceLocator {

    Set<JudgeDPublisher.ServiceForm> locateServices();

}
