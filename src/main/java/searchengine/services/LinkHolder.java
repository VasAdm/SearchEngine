package searchengine.services;

import java.util.HashSet;

public class LinkHolder {
    private static volatile HashSet<String> linksRepo = new HashSet<>();

    public static boolean addLink(String link) {
        return !linksRepo.add(link);
    }
}
