package dev.hltech.dredd.domain;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;

import java.io.Serializable;

import static lombok.AccessLevel.PROTECTED;

@Embeddable
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Access(AccessType.FIELD)
@MappedSuperclass
public class ServiceVersion implements Serializable {

    private String name;
    private String version;

}
