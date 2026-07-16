package com.hltech.judged.server.infrastructure.persistence.contracts;


import lombok.*;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import static lombok.AccessLevel.PROTECTED;

@Getter
@ToString
@Embeddable
@MappedSuperclass
@EqualsAndHashCode
@AllArgsConstructor
@Access(AccessType.FIELD)
@NoArgsConstructor(access = PROTECTED)
class ServiceVersion implements Serializable {
    private String name;
    private String version;
}
