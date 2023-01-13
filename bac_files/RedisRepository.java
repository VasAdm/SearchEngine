package searchengine.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Collection;

@Repository
public class RedisRepository {
    private final RedisTemplate<Integer, String> redisTemplate;
    private SetOperations<Integer, String> setOperations;

    @Autowired
    public RedisRepository(RedisTemplate<Integer, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init() {
        setOperations = redisTemplate.opsForSet();
    }

    public Long add(Integer siteUrl, String pageUrl) {
        return setOperations.add(siteUrl, pageUrl);
    }

    public Long deleteAll(Collection<Integer> keys) {
        return redisTemplate.delete(keys);
    }

    public Boolean deleteByKey(Integer key) {
        return redisTemplate.delete(key);
    }
}
