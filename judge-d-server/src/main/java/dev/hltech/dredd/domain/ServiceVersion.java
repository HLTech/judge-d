package dev.hltech.dredd.domain;

import lombok.*;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;

import java.io.Serializable;

import static lombok.AccessLevel.PROTECTED;

@Embeddable
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Access(AccessType.FIELD)
@ToString
public class ServiceVersion implements Serializable {

    private String name;
    private String version;

}
