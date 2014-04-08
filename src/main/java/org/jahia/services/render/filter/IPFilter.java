package org.jahia.services.render.filter;


import org.apache.commons.net.util.SubnetUtils;
import org.jahia.api.Constants;
import org.jahia.bin.ActionResult;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.modules.IPFilter.webflow.model.IPRule;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: pol
 * Date: 29.05.12
 * Time: 14:10
 * To change this template use File | Settings | File Templates.
 */
public class IPFilter extends AbstractFilter {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(IPFilter.class);

    @Autowired
    private JCRTemplate jcrTemplate;

    private List<IPRule> ipRules;

    public List<IPRule> getIpRules() {
        return ipRules;
    }

    public void setIpRules(List<IPRule> ipRules) {
        this.ipRules = ipRules;
    }

    public void addRule(IPRule rule)
    {
        this.ipRules.add(rule);
    }

    public IPFilter()
    {
        initFilter();
    }

    public void initFilter()
    {
        ipRules = new ArrayList<IPRule>();
        if(jcrTemplate!=null)
        {
            try
            {
                jcrTemplate.doExecuteWithSystemSession(null, Constants.EDIT_WORKSPACE,
                        new JCRCallback<ActionResult>() {
                            @Override
                            public ActionResult doInJCR(JCRSessionWrapper session) throws RepositoryException
                            {
                                JCRNodeWrapper ipRulesNode;

                                try
                                {
                                    ipRulesNode = session.getNode("/settings/ip-filters");
                                    if(ipRulesNode.hasNodes())
                                    {
                                        for(JCRNodeWrapper child : ipRulesNode.getNodes())
                                        {
                                            ipRules.add(new IPRule(child.getProperty("jcr:uuid").getString(),child.getProperty("name").getString(), child.getProperty("description").getString(),child.getProperty("type").getString(),child.getProperty("ipFrom").getString(),child.getProperty("ipTo").getString()));
                                        }
                                    }
                                }
                                catch (PathNotFoundException e)
                                {
                                    logger.info("No IP Filter Rules defined");
                                }
                                return null;
                            }
                        });
            }
            catch(RepositoryException e)
            {
                logger.error("Failed to initiate the IP Filter Rules", e);
            }
        }
    }

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        String currentAddress = renderContext.getRequest().getRemoteAddr();
        JCRNodeWrapper siteNode = renderContext.getSite();
        String filterType = siteNode.hasProperty("filterType") ? siteNode.getProperty("filterType").getString() : null;
        if (filterType != null)
        {
            logger.debug("filterType is [" + filterType + "]");
            if (currentAddress != null) {
                String ipRangeList = siteNode.hasProperty("ipRangeList") ? siteNode.getProperty("ipRangeList").getString() : null;
                if (ipRangeList != null) {
                    boolean inRange = isInRange(currentAddress, ipRangeList);
                    if ("deny".equals(filterType)) {
                        if (inRange) {
                            logger.warn("IPFilter - Deny rule: IP [" + currentAddress + "] is in subnet [" + ipRangeList + "]");
                            throw new JahiaForbiddenAccessException();
                        }
                    } else { // onlyallow
                        if (!inRange) {
                            logger.warn("IPFilter - Only Allow rule:  IP [" + currentAddress + "] not in subnet [" + ipRangeList + "]");
                            throw new JahiaForbiddenAccessException();
                        }
                    }
                }
            } else {
                logger.debug("IPFilter - Bypass IP filtering for localhost");
            }
        }
        return null;
    }

    private boolean isInRange(String address, String ipRangeList) {
        if (ipRangeList == null || address == null) {
            return false;
        }
        String delimiter = ",";
        String[] ranges = ipRangeList.split(delimiter);
        for (String range : ranges) {
            String subnetCidr = range.trim();
            if (subnetCidr != null && !"".equals(subnetCidr)) {
                int idx = subnetCidr.indexOf("/32");
                if (idx != -1) {
                    String standaloneIp = subnetCidr.substring(0, idx);
                    return standaloneIp.equals(address);
                } else if (subnetCidr.indexOf("/") == -1){
                    return subnetCidr.equals(address);
                } else {
                    try {
                        SubnetUtils subnetUtils = new SubnetUtils(subnetCidr);
                        if (subnetUtils.getInfo().isInRange(address)) {
                            return true;
                        }
                    } catch (IllegalArgumentException iae) {
                        logger.error("Could not parse [" + subnetCidr + "]. Please use as CIDR-notation.");
                    }
                }
            }
        }
        return false;
    }
}

