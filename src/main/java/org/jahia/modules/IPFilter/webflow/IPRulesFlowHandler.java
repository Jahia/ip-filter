package org.jahia.modules.IPFilter.webflow;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import org.jahia.api.Constants;
import org.jahia.bin.ActionResult;
import org.jahia.modules.IPFilter.webflow.model.IPRule;
import org.jahia.modules.IPFilter.webflow.model.IPRulesModel;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.filter.IPFilter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

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


    public void createRules(IPRulesModel model, RenderContext renderContext)
    {
        logger.info("IPRulesFlowHandler - Creation function called");
        logger.info("IPRulesFlowHandler - Creation function - Filter Size : "+filter.getIpRules().size());

        if(!model.getName().isEmpty() && !model.getDescription().isEmpty() && !model.getType().isEmpty() && !model.getIpFrom().isEmpty())
        {
            final String name = model.getName();
            final String description = model.getDescription();
            final String type = model.getType();
            final String ipFrom = model.getIpFrom();
            final String ipTo = model.getIpTo();

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
                                }
                                catch (PathNotFoundException e)
                                {
                                    if (session.nodeExists("/settings"))
                                    {
                                        ipRulesNode = session.getNode("/settings").addNode("ip-filters","jnt:ipRestrictionConfiguration");
                                    }
                                    else
                                    {
                                        ipRulesNode = session.getNode("/").addNode("settings", "jnt:globalSettings").addNode("ip-filters", "jnt:ipRestrictionConfiguration");
                                    }
                                }
                                ipRulesNode.addNode(name,"jnt:ipRestriction").setProperty("name",name);
                                ipRulesNode.getNode(name).setProperty("type",type);
                                ipRulesNode.getNode(name).setProperty("description",description);
                                ipRulesNode.getNode(name).setProperty("ipFrom",ipFrom);
                                ipRulesNode.getNode(name).setProperty("ipTo",ipTo);
                                session.save();

                                IPRule newRule = new IPRule(ipRulesNode.getNode(name).getProperty("jcr:uuid").getString(),name, type, description, ipFrom, ipTo);
                                filter.addRule(newRule);
                                return null;
                            }
                        });

                logger.info("IPRulesFlowHandler - Creation function - Rule Created - Filter Size : "+filter.getIpRules().size());
            }
            catch (RepositoryException e)
            {
                logger.error("Failed to create IP Filter Rule Node", e);
            }
        }
        logger.info("IPRulesFlowHandler - Creation function - Exit");
    }

    public void updateRules(IPRulesModel model, RenderContext renderContext)
    {
        logger.info("IPRulesFlowHandler - Update Function Called");
    }

    public void deleteRules(IPRulesModel model, RenderContext renderContext)
    {
        logger.info("IPRulesFlowHandler - Update Function Called");

        /*ipRulesNode.getNode(name).remove();*/
    }

    public IPRulesModel initIPRules()
    {
        logger.info("IPRulesFlowHandler - initIPRules Called");
        IPRulesModel ipRulesModel = new IPRulesModel();
        logger.info("IPRulesFlowHandler - initIPRules - Out");
        return ipRulesModel;
    }
}
