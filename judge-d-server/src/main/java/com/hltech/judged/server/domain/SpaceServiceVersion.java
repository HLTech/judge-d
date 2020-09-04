package com.hltech.judged.server.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;

import static com.google.common.base.Preconditions.checkNotNull;
import static lombok.AccessLevel.PROTECTED;

@Embeddable
@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PROTECTED)
@Access(AccessType.FIELD)
public class SpaceServiceVersion extends ServiceVersion {

    private String space;

    public SpaceServiceVersion(String space, String name, String version) {
        super(name, version);
        this.space = checkNotNull(space, "space name cannot be null");
    }

}
