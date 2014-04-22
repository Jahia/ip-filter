package org.jahia.modules.IPFilter.filter;

import org.jahia.modules.IPFilter.webflow.model.IPRule;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by rizak on 22/04/14.
 */
class SiteNameComparator implements Comparator<String> {

    Map<String, List<IPRule>> base;
    public SiteNameComparator(Map<String, List<IPRule>> base)
    {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.
    public int compare(String firstSite, String secondSite)
    {
        if(firstSite.equals("all"))
        {
            return -1;
        }
        if(secondSite.equals("all"))
        {
            return 1;
        }
        return firstSite.compareTo(secondSite);
    }
}
