package searchengine.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Collection;

@Repository
public class RedisService implements RedisRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private SetOperations<String, String> setOperations;

    @Autowired
    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init() {
        setOperations = redisTemplate.opsForSet();
    }

    public synchronized Long add(String siteUrl, String pageUrl) {
        return setOperations.add(siteUrl, pageUrl);
    }

    public Long deleteAll(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    public Boolean deleteByKey(String key) {
        return redisTemplate.delete(key);
    }
}
