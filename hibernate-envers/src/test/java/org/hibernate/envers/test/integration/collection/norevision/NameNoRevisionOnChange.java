package org.hibernate.envers.test.integration.collection.norevision;

import org.hibernate.envers.*;

import javax.persistence.*;
import java.io.*;

@Audited
@Entity
public class NameNoRevisionOnChange implements Serializable {
    @Id
    @GeneratedValue
    private Integer id;

    private String name;
    @ManyToOne
    @JoinColumn(name = "person_id", insertable = false, updatable = false)
    private PersonNoRevisionOnChange person;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PersonNoRevisionOnChange getPerson() {
        return person;
    }

    public void setPerson(PersonNoRevisionOnChange person) {
        this.person = person;
    }
}
