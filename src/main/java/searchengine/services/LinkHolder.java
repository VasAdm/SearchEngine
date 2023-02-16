package searchengine.services;

import searchengine.model.site.SiteEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class LinkHolder {
	private static volatile HashMap<SiteEntity, Set<String>> linksRepo = new HashMap<>();

	public static boolean addLink(SiteEntity siteEntity, String url) {
		Set<String> tmp = linksRepo.get(siteEntity);
		if (tmp == null) {
			Set<String> set = new HashSet<>();
			set.add(url);
			return linksRepo.put(siteEntity, set) != null;
		} else {
			return tmp.add(url);
		}

	}
}
