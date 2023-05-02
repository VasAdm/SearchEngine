package searchengine.repository;

import java.util.Collection;

public interface RedisRepository {
    Long add(String siteUrl, String pageUrl);

    Long deleteAll(Collection<String> keys);

    Boolean deleteByKey(String key);
}
