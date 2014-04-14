package org.jahia.modules.IPFilter.webflow.model;

import java.util.Comparator;

/**
 * Created by rizak on 13/04/14.
 */
public class CustomIpRuleComparator implements Comparator<IPRule>
{
    @Override
    public int compare(IPRule ipRule1, IPRule ipRule2)
    {
        if(ipRule1 == null || ipRule2 == null)
        {
            return -1;
        }
        if(ipRule1 == ipRule2)
        {
            return 0;
        }
        if(ipRule1.equals(ipRule2))
        {
            return 0;
        }
        else
        {
            return (ipRule1.getSiteName()+ipRule1.getName()).compareTo(ipRule2.getSiteName()+ipRule2.getName());
        }
    }
}
