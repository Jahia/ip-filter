package org.jahia.modules.IPFilter.webflow.model;


import org.apache.commons.lang.StringUtils;
import org.jahia.modules.IPFilter.IPRule;
import org.jahia.utils.i18n.Messages;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.message.MessageResolver;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.Serializable;
import java.util.*;

/**
 * This class defines the IPRule Model for the module webflow view.
 * it contains also the IP Rule creation and edit forms validation.
 * Created by Rahmed on 08/04/14.
 */
public class IPRulesModel implements Serializable {

    private static final long serialVersionUID = -603296318726735098L;
    private static final String BUNDLE = "resources.ip-filter";

    private int ruleIndex;

    private IPRule toBeCreated;
    private IPRule toBeUpdated;

    private boolean creationPhase;
    private boolean updatePhase;

    private Map<String, String> sitesPhilosophy;
    private List<IPRule> ipRuleList;

    private String selectedSite;

    public IPRulesModel() {
        toBeCreated = new IPRule();
        toBeUpdated = new IPRule();
        sitesPhilosophy = new HashMap<String, String>();
        ipRuleList = new ArrayList<IPRule>();
        selectedSite = "all";
    }

    public List<IPRule> getIpRuleList() {
        return ipRuleList;
    }

    public int getRuleIndex() {
        return ruleIndex;
    }

    public String getSelectedSite() {
        return selectedSite;
    }

    public Map<String, String> getSitesPhilosophy() {
        return sitesPhilosophy;
    }

    public IPRule getToBeCreated() {
        return toBeCreated;
    }


    public IPRule getToBeUpdated() {
        return toBeUpdated;
    }

    public boolean isCreationPhase() {
        return creationPhase;
    }


    public boolean isUpdatePhase() {
        return updatePhase;
    }


    public void addRule(IPRule ipRule) {
        if (!ipRuleList.contains(ipRule)) {
            this.ipRuleList.add(ipRule);
        }
    }

    public void addSitePhilosophy(String site, String philosophy) {
        this.sitesPhilosophy.put(site, philosophy);
    }

    public void removeRule(IPRule ipRule) {
        String currentSite = ipRule.getSiteName();
        int siteRuleLeft = 0;
        if (ipRuleList.contains(ipRule)) {
            this.ipRuleList.remove(ipRule);
        }
        for (IPRule currentRule : this.ipRuleList) {
            if (currentRule.getSiteName().equals(currentSite)) {
                siteRuleLeft++;
            }
        }
        if (siteRuleLeft == 0) {
            this.sitesPhilosophy.remove(currentSite);
        }
    }

    public void setCreationPhase(boolean creationPhase) {
        this.creationPhase = creationPhase;
    }

    public void setIpRuleList(List<IPRule> ipRuleList) {
        this.ipRuleList = ipRuleList;
    }

    public void setRuleIndex(int ruleIndex) {
        this.ruleIndex = ruleIndex;
    }

    public void setSelectedSite(String selectedSite) {
        this.selectedSite = selectedSite;
    }

    public void setToBeCreated(IPRule ipRule) {
        this.toBeCreated = ipRule;
    }

    public void setToBeUpdated(IPRule toBeUpdated) {
        this.toBeUpdated = toBeUpdated;
    }

    public void setUpdatePhase(boolean updatePhase) {
        this.updatePhase = updatePhase;
    }

    public MessageResolver getMessage(String source, String bundleKey) {
        Locale locale = LocaleContextHolder.getLocale();
        return new MessageBuilder().error().source(source).defaultText(Messages.get(BUNDLE, bundleKey, locale)).build();
    }

    /**
     * This method validate data in the IPRulesModel object
     *
     * @param context the context is used for getting and updating messageContext
     * @return boolean the validation result
     */
    public boolean validateServerSettingsIPRestrictionPage(ValidationContext context) {

        MessageContext messages = context.getMessageContext();
        boolean valid = true;

        //Creation Form
        if (creationPhase) {
            if (StringUtils.isBlank(toBeCreated.getName())) {
                messages.addMessage(getMessage("toBeCreated.name", "ipFilter.form.error.name.empty"));
                valid = false;
            }

            if (!StringUtils.isBlank(toBeCreated.getName()) && !validateRuleName(toBeCreated.getName(), toBeCreated.getSiteName())) {
                messages.addMessage(getMessage("toBeCreated.name", "ipFilter.form.error.name.alreadyExist"));
                valid = false;
            }

            if (StringUtils.isBlank(toBeCreated.getIpMask())) {
                messages.addMessage(getMessage("toBeCreated.ipMask", "ipFilter.form.error.ipMask.empty"));
                valid = false;
            }

            if (!StringUtils.isBlank(toBeCreated.getIpMask()) && !validateRuleMask(toBeCreated.getIpMask(), toBeCreated.getSiteName())) {
                messages.addMessage(getMessage("toBeCreated.ipMask", "ipFilter.form.error.ipMask.alreadyExists"));
                valid = false;
            }

            if (StringUtils.isBlank(toBeCreated.getSiteName())) {
                messages.addMessage(getMessage("toBeCreated.siteName", "ipFilter.form.error.siteName.empty"));
                valid = false;
            }

            if (StringUtils.isBlank(toBeCreated.getType())) {
                messages.addMessage(getMessage("toBeCreated.type", "ipFilter.form.error.type.empty"));
                valid = false;
            }
        }

        //Update Form
        if (updatePhase) {
            toBeUpdated = ipRuleList.get(ruleIndex);

            if (StringUtils.isBlank(toBeUpdated.getName())) {
                messages.addMessage(getMessage("toBeUpdated.name", "ipFilter.form.error.name.empty"));
                valid = false;
            }

            if (StringUtils.isBlank(toBeUpdated.getIpMask())) {
                messages.addMessage(getMessage("toBeUpdated.ipMask", "ipFilter.form.error.ipMask.empty"));
                valid = false;
            }

            if (StringUtils.isBlank(toBeUpdated.getSiteName())) {
                messages.addMessage(getMessage("toBeUpdated.siteName", "ipFilter.form.error.siteName.empty"));
                valid = false;
            }

            if (StringUtils.isBlank(toBeUpdated.getType())) {
                messages.addMessage(getMessage("toBeUpdated.type", "ipFilter.form.error.type.empty"));
                valid = false;
            }
        }
        creationPhase = false;
        updatePhase = false;
        return valid;
    }

    /**
     * This method validate the rulename basing on the existing rule list.
     * A rule name is consider invalid if the list already contain a rule with this name for the same site
     *
     * @param name     the rule name to validate
     * @param sitename the site on which the rule applies
     * @return boolean The validation result
     */
    public boolean validateRuleName(String name, String sitename) {
        boolean valid = true;
        for (IPRule currentRule : ipRuleList) {
            if (name.equals(currentRule.getName()) && sitename.equals(currentRule.getSiteName())) {
                valid = false;
            }
        }
        return valid;
    }

    /**
     * This method validate a rule mask basing on the existing rule list.
     * A rule mask is consider invalid if the list already contain a rule with this mask for the same site
     *
     * @param mask     the rule mask to validate
     * @param sitename the site on which the rule applies
     * @return boolean The validation result
     */
    public boolean validateRuleMask(String mask, String sitename) {
        boolean valid = true;
        for (IPRule currentRule : ipRuleList) {
            if (mask.equals(currentRule.getIpMask()) && sitename.equals(currentRule.getSiteName())) {
                valid = false;
            }
        }
        return valid;
    }
}
