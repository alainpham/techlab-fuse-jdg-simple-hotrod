package techlab.dg;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.component.infinispan.InfinispanQueryBuilder;
import org.infinispan.query.dsl.FilterConditionContext;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryBuilder;
import org.infinispan.query.dsl.QueryFactory;

public class GenerateQuery {

	public InfinispanQueryBuilder getBuilder(Exchange ex) throws ClassNotFoundException, IntrospectionException {

		//inspect the searched class in order to get the fields that can be queried
		Class c  = Class.forName(ex.getIn().getHeader("type",String.class));
		BeanInfo info = Introspector.getBeanInfo( c,Object.class);

		return new InfinispanQueryBuilder() {
			public Query build(QueryFactory<Query> queryFactory) {

				QueryBuilder<Query> qb = queryFactory.from(c);

				FilterConditionContext ctx=null;

				// for each property of the class we look if a parameter has been set		
				for ( PropertyDescriptor pd : info.getPropertyDescriptors() ){

					Object searchValue = ex.getIn().getHeader(pd.getName());

					//only search the fields that are actually indexed by checking the presence of Field annotation

					//only add search criteria when the parameter has been set in the header and when the property is indexed			
					if (searchValue!=null){

						//if field is a date convert the type explicitly
						if (pd.getPropertyType().equals(Date.class)){
							searchValue = new Date(Long.parseLong((String)searchValue));
						}

						if (ctx==null){ 	//first condition
							ctx = qb.having(pd.getName()).eq(searchValue);
						}else{ 				//additional conditions with and operator
							ctx.and().having(pd.getName()).eq(searchValue);
						}
					}
				}

				return qb.build();

			}
		};
	}
}