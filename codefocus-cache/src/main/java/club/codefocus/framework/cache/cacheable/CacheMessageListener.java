package club.codefocus.framework.cache.cacheable;

import club.codefocus.framework.cache.handler.RedisHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;


/**
 * @Auther: jackl
 * @Date: 2019/11/13 13:10
 * @Description:
 */
@Slf4j
public class CacheMessageListener implements MessageListener {

	private CodeFocusCacheManager codeFocusCacheManager;

	public CacheMessageListener(CodeFocusCacheManager codeFocusCacheManager) {
		super();
		this.codeFocusCacheManager = codeFocusCacheManager;
	}

	@Override
	public void onMessage(Message message, byte[] pattern) {
		byte[] body = message.getBody();//请使用valueSerializer
		String itemValue = new String(body);
		try {
			ObjectMapper objectMapper=new ObjectMapper();
			CacheMessage cacheMessage = objectMapper.readValue(itemValue ,CacheMessage.class);
			log.debug("onMessage:{};cacheName:{}",itemValue,cacheMessage.getCacheName());
			codeFocusCacheManager.clearLocal(cacheMessage.getCacheName(), cacheMessage.getKey());
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
	}


}
