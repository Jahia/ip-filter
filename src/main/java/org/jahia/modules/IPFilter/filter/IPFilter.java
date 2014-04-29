package org.jahia.modules.IPFilter.filter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.modules.IPFilter.IPRule;
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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * This class define a filter that will apply I.P Filtering on Digital factory pages.
 * It is initiated with the /settings/ip-filters nodes and updated by the module webflow.
 * Created by Rahmed.
 * Date: 15.04.14
 * Time: 14:10
 */
public class IPFilter extends AbstractFilter implements InitializingBean {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(IPFilter.class);

    private JCRTemplate jcrTemplate;

    private Map<String, String> sitesPhilosophy = new HashMap<String, String>();

    private Map<String, List<IPRule>> rules;

    private String filteringRule = "";

    private ReadWriteLock filterLock = new ReentrantReadWriteLock();

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    /**
     * This function add a rule under a site key in the rule Map buffer of the Filter
     *
     * @param sitename : Site on which apply the rule
     * @param rule     : The rule object
     */
    private void putRule(Map<String, List<IPRule>> ruleMap, String sitename, IPRule rule) {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding Rule Start");
        }
        //Rules already exists for this site
        if (ruleMap.get(sitename) != null) {//This rule already exists (Rule Update)
            if (ruleMap.get(sitename).contains(rule)) {//rule already exists in the list just updating its values
                int index = ruleMap.get(sitename).indexOf(rule);
                ruleMap.get(sitename).get(index).setName(rule.getName());
                ruleMap.get(sitename).get(index).setDescription(rule.getDescription());
                ruleMap.get(sitename).get(index).setType(rule.getType());
                ruleMap.get(sitename).get(index).setIpMask(rule.getIpMask());
                ruleMap.get(sitename).get(index).setActive(rule.isActive());
            } else {//rule not yet in the list adding it
                ruleMap.get(sitename).add(rule);
            }
        } else {//No rules yet for this site creating a list and adding the rule inside
            ruleMap.put(sitename, new ArrayList<IPRule>());
            ruleMap.get(sitename).add(rule);
            this.sitesPhilosophy.put(sitename, rule.getType());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Adding Rule End");
        }
    }


    /**
     * This function makes the filter buffer map initialization, reading JCR nodes.
     */
    public void initFilter() throws RepositoryException {
        filterLock.writeLock().lock();
        try {
            if (jcrTemplate != null) {
                try {
                    rules = jcrTemplate.doExecuteWithSystemSession(null, Constants.EDIT_WORKSPACE,
                            new JCRCallback<Map<String, List<IPRule>>>() {
                                @Override
                                public Map<String, List<IPRule>> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                    JCRNodeWrapper filtersFolder;
                                    Map<String, List<IPRule>> newRules = new HashMap<String, List<IPRule>>();
                                    try {
                                        //Getting the filter folder node
                                        filtersFolder = session.getNode("/settings/ip-filters");

                                        if (filtersFolder.hasNodes()) {//filters are defines
                                            for (JCRNodeWrapper filtersSiteNode : filtersFolder.getNodes()) {
                                                sitesPhilosophy.put(filtersSiteNode.getName(), filtersSiteNode.getProperty("j:filterPhilosophy").getString());
                                                for (JCRNodeWrapper ruleNode : filtersSiteNode.getNodes()) {
                                                    if (ruleNode.getProperty("j:active").getBoolean()) {
                                                        IPRule currentRule = new IPRule(ruleNode.getProperty("j:description").getString(), ruleNode.getIdentifier(), ruleNode.getProperty("j:ipMask").getString(), ruleNode.getProperty("j:name").getString(), filtersSiteNode.getName(), ruleNode.getProperty("j:active").getBoolean(), ruleNode.getProperty("j:type").getString());
                                                        putRule(newRules, filtersSiteNode.getName(), currentRule);
                                                    }
                                                }
                                            }
                                        }
                                    } catch (PathNotFoundException e) {
                                        logger.debug("No I.P Filter Rules defined", e);
                                    }
                                    return newRules;
                                }
                            }
                    );
                } catch (RepositoryException e) {
                    logger.error("Failed to initiate the IP Filter Rules", e);
                    throw new RepositoryException();
                }
            }
        } finally {
            filterLock.writeLock().unlock();
        }
    }

    /**
     * This function read the IP rules buffer and apply filter on the site called by the request if needed
     *
     * @param renderContext The render context
     * @param resource      The resource to render
     * @param chain         The render chain
     * @return Content to stop the chain, or null to continue
     * @throws Exception
     */
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        if (!renderContext.getUser().isRoot()) {
            filterLock.readLock().lock();
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("- IPFilter - prepare - Start");
                }

                boolean inRange = false;

                String currentAddress = renderContext.getRequest().getRemoteAddr();
                JCRNodeWrapper siteNode = renderContext.getSite();

                List<IPRule> rulesToApply = new ArrayList<IPRule>();
                //Initializing the ip rule list for this site
                if (rules.containsKey("all")) {
                    rulesToApply.addAll(rules.get("all"));
                }
                if (rules.containsKey(siteNode.getName())) {
                    rulesToApply.addAll(rules.get(siteNode.getName()));
                }

                for (IPRule rule : rulesToApply) {//browsing the rule list
                    String filterType = sitesPhilosophy.get(rule.getSiteName());
                    if (logger.isDebugEnabled()) {
                        logger.debug("- IPFilter - prepare - Site : " + siteNode.getName());
                        logger.debug("- IPFilter - prepare - current Address : " + currentAddress);
                        logger.debug("- IPFilter - prepare - rules size : " + rules.size());
                        logger.debug("- IPFilter - prepare - site philosophy : " + sitesPhilosophy.get(siteNode.getName()));
                        logger.debug("- IPFilter - prepare - filter Type : " + filterType);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("filterType is [" + filterType + "]");
                    }
                    if ("deny".equals(filterType)) {

                        filteringRule = rule.getIpMask();
                        inRange = inRange || isInRange(currentAddress, rule);
                        if (inRange) {
                            logger.warn("IPFilter - prepare - Deny rule: IP [" + currentAddress + "] is in subnet [" + filteringRule + "]");
                            throw new JahiaForbiddenAccessException();
                        }
                    } else {// onlyallow
                        filteringRule = rule.getIpMask();
                        inRange = inRange || isInRange(currentAddress, rule);

                        if (!inRange) {
                            logger.warn("IPFilter - prepare - Only Allow rule:  IP [" + currentAddress + "] not in subnet [" + filteringRule + "]");
                            throw new JahiaForbiddenAccessException();
                        }
                    }
                }
            } finally {
                filterLock.readLock().unlock();
            }
        }
        return null;
    }

    /**
     * This function check if an address is in the range defined by an IP rule
     *
     * @param address The address to check
     * @param rule    The ip rule from which IP Mask is taken to check the address
     * @return boolean true if the address is in the range
     */
    private boolean isInRange(String address, IPRule rule) {
        String range = rule.getIpMask().replace("localhost", "127.0.0.1");
        String subnetCidr = range.trim();
        if (!StringUtils.isEmpty(subnetCidr)) {
            if (subnetCidr.contains("/32")) {
                String standaloneIp = subnetCidr.substring(0, subnetCidr.indexOf("/32"));
                return standaloneIp.equals(address);
            } else if (!subnetCidr.contains("/")) {
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
        return false;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        initFilter();
    }
}

