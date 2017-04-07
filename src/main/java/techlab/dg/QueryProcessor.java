package techlab.dg;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.infinispan.Cache;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.Search;
import org.infinispan.manager.CacheContainer;
import org.infinispan.query.dsl.FilterConditionContext;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryBuilder;
import org.infinispan.query.dsl.QueryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class QueryProcessor implements Processor {
	
	final Logger logger = LoggerFactory.getLogger(QueryProcessor.class);
	
	//cache container to be injected with another spring bean	
	private RemoteCacheManager cacheContainer;

	

	public RemoteCacheManager getCacheContainer() {
		return cacheContainer;
	}



	public void setCacheContainer(RemoteCacheManager cacheContainer) {
		this.cacheContainer = cacheContainer;
	}



	@Override
	public void process(Exchange ex) throws Exception {
		
		//Get the targeted cachename from the exchange header
		RemoteCache<String, Object> cache = cacheContainer.getCache(ex.getIn().getHeader("cacheName", String.class));
		
		//Verify if the requested type exists using java reflection
		QueryFactory<Query> queryFactory = Search.getQueryFactory(cache);
		Class c  = Class.forName(ex.getIn().getHeader("type",String.class));
		QueryBuilder<Query> qb = queryFactory.from(c);

		FilterConditionContext ctx=null;

		//inspect the searched class in order to get the fields that can be queried
		BeanInfo info = Introspector.getBeanInfo( c,Object.class);
		
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

		Query q = qb.build();


		ex.getIn().setBody(q.list());

	}

}
