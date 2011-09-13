package org.hibernate.envers.test.integration.collection.norevision;

import org.hibernate.envers.*;

import javax.persistence.*;
import java.io.*;
import java.util.*;

@Audited
@Entity
public class PersonNoRevisionOnChange implements Serializable {
    @Id @GeneratedValue
    private Integer id;
    @AuditMappedBy(mappedBy = "person")
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "person_id")
    @NoRevisionOnChange
	private Set<NameNoRevisionOnChange> names;

    public PersonNoRevisionOnChange() {
        names = new HashSet<NameNoRevisionOnChange>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Set<NameNoRevisionOnChange> getNames() {
        return names;
    }

    public void setNames(Set<NameNoRevisionOnChange> names) {
        this.names = names;
    }
}
