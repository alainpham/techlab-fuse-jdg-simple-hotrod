package techlab.model;

import java.io.Serializable;
import java.util.Date;

import org.infinispan.protostream.annotations.ProtoDoc;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoDoc("@Indexed")
public class Event implements Serializable{
	private static final long serialVersionUID = 1L;
	private String uid;
	private Date timestmp;
	private String name;
	private String content;
	
	@ProtoField(number = 1)
	public String getUid() {
		return uid;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	@ProtoDoc("@IndexedField(index = true, store = false)")
	@ProtoField(number = 2)
	public Date getTimestmp() {
		return timestmp;
	}
	
	public void setTimestmp(Date timestmp) {
		this.timestmp = timestmp;
	}
	
	@ProtoDoc("@IndexedField(index = true, store = false)")
	@ProtoField(number = 3)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@ProtoField(number = 4)
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
}
