package org.jahia.modules.IPFilter.webflow;

import org.jahia.api.Constants;
import org.jahia.modules.IPFilter.IPRule;
import org.jahia.modules.IPFilter.filter.IPFilter;
import org.jahia.modules.IPFilter.webflow.model.CustomIpRuleComparator;
import org.jahia.modules.IPFilter.webflow.model.IPRulesModel;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.message.MessageResolver;

import javax.jcr.ItemExistsException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.io.Serializable;
import java.util.Collections;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * This class is the Webflow FlowHandler used to manage the IPRulesModel forms Data (Create, update and delete I.P Rules nodes in JCR and reload the filter)
 * Created by Rahmed on 08/04/14.
 */
public class IPRulesFlowHandler implements Serializable {
    private static final long serialVersionUID = -4717495631211740649L;

    private static final Logger logger = getLogger(IPRulesFlowHandler.class);
    private static final String BUNDLE = "resources.ip-filter";

    @Autowired
    private transient JCRTemplate jcrTemplate;

    @Autowired
    private transient IPFilter filter;

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    /**
     * This methode handle the rule creation. It creates JCR nodes corresponding to the rule under the path /settings/ip-filters/<model.siteName>
     *
     * @param model The webflow model object containing the rule to create data
     * @return IPRulesModel : the ipRuleModel cleared to reset the form in the Jsp after the node creation
     */
    public IPRulesModel createRules(IPRulesModel model, MessageContext messages) throws RepositoryException {
        if (logger.isDebugEnabled()) {
            logger.debug("IPRulesFlowHandler - createRules - Start");
        }
        boolean created = true;
        //Check the model rule validity
        if (!model.getToBeCreated().getName().isEmpty() && !model.getToBeCreated().getIpMask().isEmpty() && !model.getToBeCreated().getSiteName().isEmpty() && !model.getToBeCreated().getType().isEmpty()) {
            final String description = model.getToBeCreated().getDescription();
            final String ipMask = model.getToBeCreated().getIpMask();
            final String name = model.getToBeCreated().getName();
            final String siteName = model.getToBeCreated().getSiteName();
            final String type = model.getToBeCreated().getType();
            final MessageResolver itemAlreadyExistsMessage = model.getMessage("toBeCreated.name", "ipFilter.form.error.name.format");
            try {
                MessageResolver creationResult = jcrTemplate.doExecuteWithSystemSession(null, Constants.EDIT_WORKSPACE,
                        new JCRCallback<MessageResolver>() {
                            @Override
                            public MessageResolver doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                JCRNodeWrapper ipRulesNode;
                                JCRNodeWrapper createdNode = null;
                                boolean jcrOk = true;
                                try {
                                    ipRulesNode = session.getNode("/settings/ip-filters/" + siteName);
                                } catch (PathNotFoundException e) {
                                    //rule folder has to be created
                                    if (session.nodeExists("/settings/ip-filters")) {
                                        ipRulesNode = session.getNode("/settings/ip-filters").addNode(siteName, "jnt:ipRestrictionConfiguration");
                                        ipRulesNode.setProperty("j:filterPhilosophy", type);
                                    } else {
                                        if (session.nodeExists("/settings")) {
                                            ipRulesNode = session.getNode("/settings").addNode("ip-filters", "jnt:globalSettings").addNode(siteName, "jnt:ipRestrictionConfiguration");
                                            ipRulesNode.setProperty("j:filterPhilosophy", type);
                                        } else {
                                            ipRulesNode = session.getNode("/").addNode("settings", "jnt:globalSettings").addNode("ip-filters", "jnt:globalSettings").addNode(siteName, "jnt:ipRestrictionConfiguration");
                                            ipRulesNode.setProperty("j:filterPhilosophy", type);
                                        }
                                    }
                                }
                                if (!ipRulesNode.hasProperty("j:filterPhilosophy")) {
                                    ipRulesNode.setProperty("j:filterPhilosophy", type);
                                }

                                if (type.equals(ipRulesNode.getProperty("j:filterPhilosophy").getString())) {
                                    try {//Node can be created
                                        createdNode = ipRulesNode.addNode(name, "jnt:ipRestriction");
                                        ipRulesNode.getNode(name).setProperty("j:description", description);
                                        ipRulesNode.getNode(name).setProperty("j:ipMask", ipMask);
                                        ipRulesNode.getNode(name).setProperty("j:name", name);
                                        ipRulesNode.getNode(name).setProperty("j:type", type);
                                        ipRulesNode.getNode(name).setProperty("j:active", true);
                                    } catch (ItemExistsException e) {//Node with this name already exists
                                        logger.error("Item with this name value : " + name + "Already Exists", e);
                                        jcrOk = false;
                                    }
                                }
                                session.save();
                                if (createdNode != null) {
                                    //Reloading the filter
                                    filter.initFilter();
                                }
                                return jcrOk ? null : itemAlreadyExistsMessage;
                            }
                        }
                );
                if (creationResult != null) {//Name already exists
                    messages.addMessage(creationResult);
                    created = false;
                }
            } catch (RepositoryException e) {//Any other node creation Issue
                if (e.getCause().toString().contains("IllegalNameException")) {
                    messages.addMessage(model.getMessage("toBeCreated.name", "ipFilter.form.error.name.format"));
                    logger.error("IPRulesFlowHandler - createRules - Failed to create IP Filter Rule Node", e);
                    created = false;
                } else {
                    messages.addMessage(model.getMessage("creationPhase", "ipFilter.form.error.create"));
                    logger.error("IPRulesFlowHandler - createRules - Failed to create IP Filter Rule Node", e);
                    created = false;
                }
            }
        }
        //Resetting the form in the jsp if the node has been created
        if (created) {
            model = initIPRules();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("IPRulesFlowHandler - createRules - End");
        }
        return model;
    }

    /**
     * This methode updates a Rule in JCR and reload the filter.
     *
     * @param model the model containing the data on the rule to update
     * @return IPRulesModel : The model containing the updated list of rule sorted as expected
     */
    public IPRulesModel updateRules(IPRulesModel model, MessageContext messages) {
        if (logger.isDebugEnabled()) {
            logger.debug("IPRulesFlowHandler - updateRules - Start");
        }
        try {
            final IPRule ipRule = model.getToBeUpdated();
            if (logger.isDebugEnabled()) {
                logger.debug("Update Index : " + model.getRuleIndex());
            }
            //Update Rule node in JCR
            MessageResolver updateResult = jcrTemplate.doExecuteWithSystemSession(null, Constants.EDIT_WORKSPACE,
                    new JCRCallback<MessageResolver>() {
                        @Override
                        public MessageResolver doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            JCRNodeWrapper ipRuleNode = session.getNodeByIdentifier(ipRule.getId());
                            ipRuleNode.setProperty("j:name", ipRule.getName());
                            ipRuleNode.setProperty("j:description", ipRule.getDescription());
                            ipRuleNode.setProperty("j:ipMask", ipRule.getIpMask());
                            ipRuleNode.setProperty("j:active", ipRule.isActive());
                            if (logger.isDebugEnabled()) {
                                logger.debug("IPRulesFlowHandler - updateRules - Updating the IPRule node : " + ipRule.getId());
                            }
                            session.save();
                            //Reload the filter after JCR Update
                            filter.initFilter();
                            return null;
                        }
                    }
            );
            if (updateResult != null) {
                messages.addMessage(updateResult);
            }
        } catch (RepositoryException e) {
            logger.error("IPRulesFlowHandler - updateRules - Failed to Update IP Filter Rule Node", e);
            messages.addMessage(model.getMessage("updatePhase", "ipFilter.form.error.update"));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("IPRulesFlowHandler - updateRules - End");
        }
        Collections.sort(model.getIpRuleList(), new CustomIpRuleComparator());
        return model;
    }

    /**
     * This methode delete an IP Filtering rule in JCR
     *
     * @param model the model containing the data on the rule to delete
     * @return IPRulesModel : the model on which the rule has been deleted
     */
    public IPRulesModel deleteRules(IPRulesModel model, MessageContext messages) {
        boolean deleted = true;
        if (logger.isDebugEnabled()) {
            logger.debug("IPRulesFlowHandler - deleteRules - Start");
        }

        final IPRule ipRuletoRemove = model.getIpRuleList().get(model.getRuleIndex());
        //Delete JCR Node
        try {
            MessageResolver deleteResult = jcrTemplate.doExecuteWithSystemSession(null, Constants.EDIT_WORKSPACE,
                    new JCRCallback<MessageResolver>() {
                        @Override
                        public MessageResolver doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            JCRNodeWrapper ipRuleNode = session.getNodeByIdentifier(ipRuletoRemove.getId());
                            JCRNodeWrapper siteFolder = ipRuleNode.getParent();
                            if (logger.isDebugEnabled()) {
                                logger.debug("IPRulesFlowHandler - deleteRules - Deleting the IPRule node : " + ipRuletoRemove.getId());
                            }
                            ipRuleNode.remove();
                            if (siteFolder.getNodes().getSize() == 0) {
                                siteFolder.remove();
                            }
                            session.save();
                            //Reload the filter
                            filter.initFilter();
                            return null;
                        }
                    }
            );
            if (deleteResult != null) {
                messages.addMessage(deleteResult);
                deleted = false;
            }
        } catch (RepositoryException e) {
            logger.error("IPRulesFlowHandler - deleteRules - Failed to create IP Filter Rule Node", e);
            messages.addMessage(model.getMessage("updatePhase", "ipFilter.form.error.delete"));
            deleted = false;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("IPRulesFlowHandler - deleteRules - Delete Function End");
        }
        model.removeRule(ipRuletoRemove);
        if (deleted) {
            model.setUpdatePhase(true);
            model.setCreationPhase(false);
        }
        return model;
    }

    /**
     * This methode initialize the Webflow model with the Ip Rules JCR nodes
     *
     * @return IPRuleModel : the initialized model
     */
    public IPRulesModel initIPRules() {
        if (logger.isDebugEnabled()) {
            logger.debug("IPRulesFlowHandler - initIPRules - Start");
        }

        //Init the sites filter philosophy constraints
        try {
            return jcrTemplate.doExecuteWithSystemSession(null, Constants.EDIT_WORKSPACE,
                    new JCRCallback<IPRulesModel>() {
                        @Override
                        public IPRulesModel doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            JCRNodeWrapper filterSitesNode;
                            IPRulesModel ipRulesModel = new IPRulesModel();
                            //Getting filter Sites nodes
                            try {
                                filterSitesNode = session.getNode("/settings/ip-filters/");
                            } catch (PathNotFoundException e) {//Folders has to be created
                                if (session.nodeExists("/settings")) {
                                    filterSitesNode = session.getNode("/settings").addNode("ip-filters", "jnt:globalSettings");
                                } else {
                                    filterSitesNode = session.getNode("/").addNode("settings", "jnt:globalSettings").addNode("ip-filters", "jnt:globalSettings");
                                }
                            }
                            //Getting philosophy constraints from sites
                            for (JCRNodeWrapper filterFolder : filterSitesNode.getNodes()) {
                                if (filterFolder.hasProperty("j:filterPhilosophy")) {
                                    ipRulesModel.addSitePhilosophy(filterFolder.getName(), filterFolder.getProperty("j:filterPhilosophy").getString());
                                }
                                for (JCRNodeWrapper filter : filterFolder.getNodes()) {
                                    ipRulesModel.addRule(new IPRule(filter.getProperty("j:description").getString(), filter.getIdentifier(), filter.getProperty("j:ipMask").getString(), filter.getProperty("j:name").getString(), filterFolder.getName(), filter.getProperty("j:active").getBoolean(), filter.getProperty("j:type").getString()));
                                    Collections.sort(ipRulesModel.getIpRuleList(), new CustomIpRuleComparator());
                                }
                                session.save();

                            }
                            if (logger.isDebugEnabled()) {
                                logger.debug("IPRulesFlowHandler - initIPRules - End");
                            }
                            return ipRulesModel;
                        }
                    }
            );
        } catch (RepositoryException e) {
            logger.error("IPRulesFlowHandler - initIPRules - Failed to create Sites Philosophy list on Model init", e);
            return new IPRulesModel();
        }
    }
}
