package org.jahia.modules.IPFilter.webflow;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import org.jahia.api.Constants;
import org.jahia.bin.ActionResult;
import org.jahia.modules.IPFilter.webflow.model.CustomIpRuleComparator;
import org.jahia.modules.IPFilter.webflow.model.IPRule;
import org.jahia.modules.IPFilter.webflow.model.IPRulesModel;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.render.RenderContext;
import org.jahia.modules.IPFilter.filter.IPFilter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jcr.ItemExistsException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by rizak on 08/04/14.
 */
public class IPRulesFlowHandler implements Serializable
{
    private static final Logger logger = getLogger(IPRulesFlowHandler.class);

    @Autowired
    private transient JCRTemplate jcrTemplate;



    @Autowired
    private transient IPFilter filter;

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }


    public IPRulesModel createRules(IPRulesModel model, RenderContext renderContext)
    {

        logger.debug("IPRulesFlowHandler - createRules - Start");
        if(!model.getToBeCreated().getName().isEmpty() && !model.getToBeCreated().getIpMask().isEmpty() && !model.getToBeCreated().getSiteName().isEmpty() && !model.getToBeCreated().getType().isEmpty())
        {
            final String description = model.getToBeCreated().getDescription();
            final String ipMask = model.getToBeCreated().getIpMask();
            final String name = model.getToBeCreated().getName();
            final String siteName = model.getToBeCreated().getSiteName();
            final String type = model.getToBeCreated().getType();
            try
            {
                jcrTemplate.doExecuteWithSystemSession(null, Constants.EDIT_WORKSPACE,
                        new JCRCallback<ActionResult>() {
                            @Override
                            public ActionResult doInJCR(JCRSessionWrapper session) throws RepositoryException
                            {
                                JCRNodeWrapper ipRulesNode;
                                JCRNodeWrapper createdNode=null;
                                try
                                {
                                    ipRulesNode = session.getNode("/settings/ip-filters/"+siteName);
                                }
                                catch (PathNotFoundException e)
                                {
                                    if (session.nodeExists("/settings/ip-filters"))
                                    {
                                        ipRulesNode = session.getNode("/settings/ip-filters").addNode(siteName,"jnt:ipRestrictionConfiguration");
                                        ipRulesNode.setProperty("j:filterPhilosophy",type);
                                    }
                                    else
                                    {
                                        if(session.nodeExists("/settings"))
                                        {
                                            ipRulesNode = session.getNode("/settings").addNode("ip-filters","jnt:globalSettings").addNode(siteName,"jnt:ipRestrictionConfiguration");
                                            ipRulesNode.setProperty("j:filterPhilosophy",type);
                                        }
                                        else
                                        {
                                            ipRulesNode = session.getNode("/").addNode("settings","jnt:globalSettings").addNode("ip-filters","jnt:globalSettings").addNode(siteName,"jnt:ipRestrictionConfiguration");
                                            ipRulesNode.setProperty("j:filterPhilosophy",type);
                                        }
                                    }
                                }
                                if(!ipRulesNode.hasProperty("j:filterPhilosophy"))
                                {
                                    ipRulesNode.setProperty("j:filterPhilosophy", type);
                                }

                                if(type.equals(ipRulesNode.getProperty("j:filterPhilosophy").getString()))
                                {
                                    try
                                    {
                                        createdNode = ipRulesNode.addNode(name,"jnt:ipRestriction");
                                        ipRulesNode.getNode(name).setProperty("j:description", description);
                                        ipRulesNode.getNode(name).setProperty("j:ipMask", ipMask);
                                        ipRulesNode.getNode(name).setProperty("j:name", name);
                                        ipRulesNode.getNode(name).setProperty("j:type", type);
                                        ipRulesNode.getNode(name).setProperty("j:status", true);
                                    }
                                    catch(ItemExistsException e)
                                    {
                                        logger.info("IPRulesFlowHandler - createRules - Item Already Exists !!!",e);
                                    }
                                }
                                session.save();

                                if(createdNode != null)
                                {
                                    filter.putRule(siteName, new IPRule(description, createdNode.getIdentifier(), ipMask, name, siteName, true, type));
                                }
                                return null;
                            }
                        });

                logger.debug("IPRulesFlowHandler - createRules - Rule Created - Filter Size : " + filter.getRules().size());

            }
            catch (RepositoryException e)
            {
                logger.error("IPRulesFlowHandler - createRules - Failed to create IP Filter Rule Node", e);
            }
        }
        logger.debug("IPRulesFlowHandler - createRules - End");

        model=initIPRules();
        return model;

    }

    public IPRulesModel updateRules(IPRulesModel model, RenderContext renderContext)
    {

        logger.info("IPRulesFlowHandler - updateRules - Start");
        try
        {

            final IPRule ipRule=model.getToBeUpdated();
            logger.info("Update Index : "+model.getRuleIndex());
            jcrTemplate.doExecuteWithSystemSession(null, Constants.EDIT_WORKSPACE,
                    new JCRCallback<ActionResult>()
                    {
                        @Override
                        public ActionResult doInJCR(JCRSessionWrapper session) throws RepositoryException
                        {
                            JCRNodeWrapper ipRuleNode = session.getNodeByIdentifier(ipRule.getId());
                            ipRuleNode.setProperty("j:name",ipRule.getName());
                            ipRuleNode.setProperty("j:description",ipRule.getDescription());
                            ipRuleNode.setProperty("j:ipMask",ipRule.getIpMask());
                            ipRuleNode.setProperty("j:status",ipRule.isStatus());
                            logger.debug("IPRulesFlowHandler - updateRules - Updating the IPRule node : " + ipRule.getId());
                            session.save();
                            filter.putRule(ipRule.getSiteName(), ipRule);
                            return null;
                        }
                    });
        }
        catch (RepositoryException e)
        {
            logger.error("IPRulesFlowHandler - updateRules - Failed to Update IP Filter Rule Node", e);
        }
        logger.info("IPRulesFlowHandler - updateRules - End");
        Collections.sort(model.getIpRuleList(), new CustomIpRuleComparator());
        return model;
    }

    public IPRulesModel deleteRules(IPRulesModel model, RenderContext renderContext)
    {
        logger.debug("IPRulesFlowHandler - deleteRules - Start");

        final IPRule ipRuletoRemove=model.getIpRuleList().get(model.getRuleIndex());
        try
        {
            jcrTemplate.doExecuteWithSystemSession(null, Constants.EDIT_WORKSPACE,
                    new JCRCallback<ActionResult>() {
                        @Override
                        public ActionResult doInJCR(JCRSessionWrapper session) throws RepositoryException
                        {
                            JCRNodeWrapper ipRuleNode = session.getNodeByIdentifier(ipRuletoRemove.getId());
                            JCRNodeWrapper siteFolder = ipRuleNode.getParent();
                            logger.debug("IPRulesFlowHandler - deleteRules - Deleting the IPRule node : " + ipRuletoRemove.getId());
                            ipRuleNode.remove();
                            if(siteFolder.getNodes().getSize() == 0)
                            {
                                siteFolder.remove();
                            }
                            session.save();
                            filter.deleteRule(ipRuletoRemove.getSiteName(), ipRuletoRemove);
                            return null;
                        }
                    });
        }
        catch (RepositoryException e)
        {
            logger.error("IPRulesFlowHandler - deleteRules - Failed to create IP Filter Rule Node", e);
        }
        logger.debug("IPRulesFlowHandler - deleteRules - Delete Function End");
        model.removeRule(ipRuletoRemove);
        model.setUpdatePhase(false);
        model.setCreationPhase(false);
        return model;
    }

    public IPRulesModel initIPRules()
    {
        logger.debug("IPRulesFlowHandler - initIPRules - Start");

        //Init the sites filter philosophy constraints
        try
        {
            return jcrTemplate.doExecuteWithSystemSession(null, Constants.EDIT_WORKSPACE,
                    new JCRCallback<IPRulesModel>()
                    {
                        @Override
                        public IPRulesModel doInJCR(JCRSessionWrapper session) throws RepositoryException
                        {
                            JCRNodeWrapper filterSitesNode;
                            IPRulesModel ipRulesModel = new IPRulesModel();
                            //Getting filter Sites nodes
                            try
                            {
                                filterSitesNode = session.getNode("/settings/ip-filters/");
                            }
                            catch (PathNotFoundException e)
                            {
                                if (session.nodeExists("/settings"))
                                {
                                    filterSitesNode = session.getNode("/settings").addNode("ip-filters","jnt:globalSettings");
                                }
                                else
                                {
                                        filterSitesNode = session.getNode("/").addNode("settings","jnt:globalSettings").addNode("ip-filters","jnt:globalSettings");
                                }
                            }
                            //Getting philosophy constraints from sites
                            for(JCRNodeWrapper filterFolder : filterSitesNode.getNodes())
                            {
                                if(filterFolder.hasProperty("j:filterPhilosophy"))
                                {
                                    ipRulesModel.addSitePhilosophy(filterFolder.getName(),filterFolder.getProperty("j:filterPhilosophy").getString());
                                }
                                for(JCRNodeWrapper filter : filterFolder.getNodes())
                                {
                                   /* filter.remove();
                                    session.save();*/
                                    ipRulesModel.addRule(new IPRule(filter.getProperty("j:description").getString(), filter.getIdentifier(), filter.getProperty("j:ipMask").getString(), filter.getProperty("j:name").getString(), filterFolder.getName(), filter.getProperty("j:status").getBoolean(), filter.getProperty("j:type").getString()));
                                    Collections.sort(ipRulesModel.getIpRuleList(), new CustomIpRuleComparator());
                                }
                                session.save();

                            }
                            logger.debug("IPRulesFlowHandler - initIPRules - End");
                            return ipRulesModel;
                        }
                    });
        }
        catch (RepositoryException e)
        {
            logger.error("IPRulesFlowHandler - initIPRules - Failed to create Sites Philosophy list on Model init", e);
            return new IPRulesModel();
        }
    }
}
