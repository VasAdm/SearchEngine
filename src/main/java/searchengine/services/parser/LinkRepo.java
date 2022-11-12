package searchengine.services.parser;

import java.util.HashSet;
import java.util.Set;

public class LinkRepo {
    private static final Set<String> links = new HashSet<>();

    public static synchronized boolean addLink(String link) {
        return links.add(link);
    }
}
