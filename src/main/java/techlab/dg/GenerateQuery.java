package techlab.dg;

import java.beans.IntrospectionException;

import org.apache.camel.Exchange;
import org.apache.camel.component.infinispan.InfinispanQueryBuilder;

public class GenerateQuery {

	public InfinispanQueryBuilder getBuilder(Exchange ex) throws ClassNotFoundException, IntrospectionException {

		InfinispanQueryBuilder qb = new GenericQuery(ex.getIn().getHeader("type",String.class),ex.getIn().getHeaders());
		
		return qb;
	}
}