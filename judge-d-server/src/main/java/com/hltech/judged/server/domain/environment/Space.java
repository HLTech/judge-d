package com.hltech.judged.server.domain.environment;

import com.hltech.judged.server.domain.ServiceId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class Space {
    private final String name;
    private final Set<ServiceId> serviceIds;
}
