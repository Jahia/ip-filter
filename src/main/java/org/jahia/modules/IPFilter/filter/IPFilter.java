package org.jahia.modules.IPFilter.filter;


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
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by IntelliJ IDEA.
 * User: pol
 * Date: 29.05.12
 * Time: 14:10
 * To change this template use File | Settings | File Templates.
 */
public class IPFilter extends AbstractFilter implements InitializingBean {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(IPFilter.class);

    private JCRTemplate jcrTemplate;

    private Map<String,String> sitesPhilosophy = new HashMap<String,String>();

    private Map<String,List<IPRule>> rules;

    private String filteringRule;

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    public Map<String, List<IPRule>> getRules() {
        return rules;
    }

    public void putRule(String sitename, IPRule rule)
    {
        logger.debug("Adding Rule Start");
        if(this.rules.get(sitename)!=null)
        {
            if(this.rules.get(sitename).contains(rule))
            {
                //rule already exists in the list just updating its values
                int index = this.rules.get(sitename).indexOf(rule);
                this.rules.get(sitename).get(index).setName(rule.getName());
                this.rules.get(sitename).get(index).setDescription(rule.getDescription());
                this.rules.get(sitename).get(index).setType(rule.getType());
                this.rules.get(sitename).get(index).setIpMask(rule.getIpMask());
                this.rules.get(sitename).get(index).setStatus(rule.isStatus());
            }
            else
            {
                //rule not yet in the list adding it
                this.rules.get(sitename).add(rule);
            }
        }
        else
        {
            //No rules yet for this site creating a list and adding the rule inside
            this.rules.put(sitename, new ArrayList<IPRule>());
            this.rules.get(sitename).add(rule);
            this.sitesPhilosophy.put(sitename,rule.getType());
        }
        logger.debug("Adding Rule End");
    }

    public void deleteRule(String sitename, IPRule rule)
    {
        logger.debug("Adding Rule Start");
        this.rules.get(sitename).remove(rule);
        if(rules.get(sitename).size() == 0)
        {
            rules.remove(sitename);
            sitesPhilosophy.remove(sitename);
        }
        logger.debug("Adding Rule End");
    }

    public void initFilter()
    {
        rules = new HashMap<String, List<IPRule>>();

        if(jcrTemplate!=null)
        {
            try
            {
                jcrTemplate.doExecuteWithSystemSession(null, Constants.EDIT_WORKSPACE,
                        new JCRCallback<ActionResult>() {
                            @Override
                            public ActionResult doInJCR(JCRSessionWrapper session) throws RepositoryException
                            {
                                JCRNodeWrapper filtersFolder;

                                try
                                {
                                    filtersFolder = session.getNode("/settings/ip-filters");
                                    if(filtersFolder.hasNodes())
                                    {
                                        for(JCRNodeWrapper filtersSiteNode : filtersFolder.getNodes())
                                        {
                                            sitesPhilosophy.put(filtersSiteNode.getName(),filtersSiteNode.getProperty("j:filterPhilosophy").getString());
                                            for(JCRNodeWrapper ruleNode : filtersSiteNode.getNodes())
                                            {
                                                IPRule currentRule = new IPRule(ruleNode.getProperty("j:description").getString(),ruleNode.getIdentifier(),ruleNode.getProperty("j:ipMask").getString(),ruleNode.getProperty("j:name").getString(),filtersSiteNode.getName(),ruleNode.getProperty("j:status").getBoolean(), ruleNode.getProperty("j:type").getString());
                                                putRule(filtersSiteNode.getName(), currentRule);
                                            }
                                        }
                                    }
                                }
                                catch (PathNotFoundException e)
                                {
                                    logger.debug("No I.P Filter Rules defined", e);
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

    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception
    {
       logger.debug("- IPFilter - prepare - Start");
        boolean inRange = false;
        boolean filterNeeded=false;
        String currentAddress = renderContext.getRequest().getRemoteAddr();
        JCRNodeWrapper siteNode = renderContext.getSite();
        List<IPRule> rulesToApply=null;
        if(rules.containsKey(siteNode.getName()))
        {
            rulesToApply = rules.get(siteNode.getName());
            if(sitesPhilosophy.containsKey("all") && sitesPhilosophy.get("all").equals(sitesPhilosophy.get(siteNode.getName())))
            {
                rulesToApply.addAll(rules.get("all"));
            }
        }
        else
        {
            if(sitesPhilosophy.containsKey("all"))
            {
                rulesToApply=rules.get("all");
            }
        }
        String filterType = null;
        if(sitesPhilosophy.containsKey(siteNode.getName()))
        {
            filterType = sitesPhilosophy.get(siteNode.getName());
        }
        else
        {
            if(sitesPhilosophy.containsKey("all"))
            {
                filterType =sitesPhilosophy.get("all");
            }
        }

        logger.debug("- IPFilter - prepare - Site : " + siteNode.getName());
        logger.debug("- IPFilter - prepare - current Address : " + currentAddress);
        logger.debug("- IPFilter - prepare - rules size : " + rules.size());
        logger.debug("- IPFilter - prepare - site philosophy : " + sitesPhilosophy.get(siteNode.getName()));
        logger.debug("- IPFilter - prepare - filter Type : " + filterType);
        if (filterType != null && rulesToApply!=null)
        {
            logger.debug("filterType is [" + filterType + "]");
            if (currentAddress != null)
            {
                if ("deny".equals(filterType))
                {
                    for(IPRule currentRule : rulesToApply)
                    {

                        if(currentRule.isStatus())
                        {
                            filteringRule+=currentRule.getIpMask();
                            inRange = inRange || isInRange(currentAddress, currentRule,siteNode.getName());
                            filterNeeded=true;
                        }
                    }
                    if(filterNeeded)
                    {
                        if (inRange)
                        {
                            logger.warn("IPFilter - prepare - Deny rule: IP [" + currentAddress + "] is in subnet [" + filteringRule + "]");
                            throw new JahiaForbiddenAccessException();
                        }
                    }
                }
                else
                {// onlyallow
                    for(IPRule currentRule : rulesToApply)
                    {
                        if(currentRule.isStatus())
                        {
                            filteringRule+=currentRule.getIpMask();
                            inRange = inRange || isInRange(currentAddress, currentRule,siteNode.getName());
                            filterNeeded=true;
                        }
                    }
                    if(filterNeeded)
                    {
                        if (!inRange) {
                            logger.warn("IPFilter - prepare - Only Allow rule:  IP [" + currentAddress + "] not in subnet [" + filteringRule + "]");
                            throw new JahiaForbiddenAccessException();
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isInRange(String address, IPRule rule, String siteName)
    {
        String range=rule.getIpMask().replace("localhost","127.0.0.1");
        String subnetCidr = range.trim();
        if (subnetCidr != null && !"".equals(subnetCidr))
        {
            int idx = subnetCidr.indexOf("/32");
            if (idx != -1) {
                String standaloneIp = subnetCidr.substring(0, idx);
                return standaloneIp.equals(address);
            }
            else if (subnetCidr.indexOf("/") == -1)
            {
                return subnetCidr.equals(address);
            }
            else
            {
                try
                {
                    SubnetUtils subnetUtils = new SubnetUtils(subnetCidr);
                    if (subnetUtils.getInfo().isInRange(address))
                    {
                        return true;
                    }
                }
                catch (IllegalArgumentException iae)
                {
                    logger.error("Could not parse [" + subnetCidr + "]. Please use as CIDR-notation.");
                }
            }
        }
        return false;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        initFilter();
    }
}

