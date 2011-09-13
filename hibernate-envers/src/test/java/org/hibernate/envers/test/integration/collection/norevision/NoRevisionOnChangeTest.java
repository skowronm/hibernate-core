package org.hibernate.envers.test.integration.collection.norevision;

import org.hibernate.ejb.*;
import org.hibernate.envers.test.*;
import org.junit.*;

import javax.persistence.*;
import java.util.*;

public class NoRevisionOnChangeTest extends AbstractEntityTest {
	protected Integer personId;

	@Override
	public void configure(Ejb3Configuration cfg) {
		cfg.addAnnotatedClass(PersonNoRevisionOnChange.class);
		cfg.addAnnotatedClass(NameNoRevisionOnChange.class);
		cfg.setProperty("org.hibernate.envers.revision_on_collection_change", "true");
	}

	@Test
	@Priority(10)
	public void initData() {
		EntityManager em = getEntityManager();

		// Rev 1
		em.getTransaction().begin();
		PersonNoRevisionOnChange p = new PersonNoRevisionOnChange();
		NameNoRevisionOnChange n = new NameNoRevisionOnChange();
		n.setName("name1");
		p.getNames().add(n);
		em.persist(p);
		em.getTransaction().commit();

		// Rev 2
		em.getTransaction().begin();
		n.setName("Changed name");
		em.persist(p);
		em.getTransaction().commit();

		// Rev 3
		em.getTransaction().begin();
		NameNoRevisionOnChange n2 = new NameNoRevisionOnChange();
		n2.setName("name2");
		p.getNames().add(n2);
		em.getTransaction().commit();

		personId = p.getId();
	}

	@Test
	public void testPersonRevisionCount() {
		assert getAuditReader().getRevisions(PersonNoRevisionOnChange.class, personId)
				.equals(Arrays.asList(1));
	}
}
