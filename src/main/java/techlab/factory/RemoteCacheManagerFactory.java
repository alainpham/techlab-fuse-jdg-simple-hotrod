package techlab.factory;

import java.io.IOException;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.annotations.ProtoSchemaBuilder;
import org.infinispan.protostream.annotations.ProtoSchemaBuilderException;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;

import techlab.model.Event;

public class RemoteCacheManagerFactory {

	ConfigurationBuilder clientBuilder;

	public RemoteCacheManagerFactory(String hostname, int port) {
		clientBuilder = new ConfigurationBuilder();
		clientBuilder.addServer()
		.host(hostname)
		.port(port)
		.marshaller(new ProtoStreamMarshaller());
	}

	public RemoteCacheManager newRemoteCacheManager() throws ProtoSchemaBuilderException, IOException {
		RemoteCacheManager remoteCacheManager = new RemoteCacheManager(clientBuilder.build());

		SerializationContext ctx = ProtoStreamMarshaller.getSerializationContext(remoteCacheManager);

		ProtoSchemaBuilder protoSchemaBuilder = new ProtoSchemaBuilder();

		//		
		String eventSchema = protoSchemaBuilder
				.fileName("event.proto")
				.packageName("techlab")
				.addClass(Event.class)
				.build(ctx);

		RemoteCache<String, String> metadataCache = remoteCacheManager.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
		metadataCache.put("memo.proto", eventSchema);

		String errors = metadataCache.get(ProtobufMetadataManagerConstants.ERRORS_KEY_SUFFIX);
		if (errors != null) {
			throw new IllegalStateException("Some Protobuf schema files contain errors:\n" + errors);
		}

		return remoteCacheManager;
	}
}
