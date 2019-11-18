package club.codefocus.framework.cache.cacheable;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author  jackl
 * @since 1.0
 */
@Data
public class CacheMessage implements Serializable {

	/** */
	private static final long serialVersionUID = 5987219310442078193L;

	private String cacheName;

	private String key;

	public CacheMessage(){
	}


	public CacheMessage(String json) {
		try {
			CacheMessage param = new ObjectMapper().readValue(json, CacheMessage.class);
			this.cacheName=param.getCacheName();
			this.key=param.getKey();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
