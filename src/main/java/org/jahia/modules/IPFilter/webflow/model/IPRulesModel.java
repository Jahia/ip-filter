package org.jahia.modules.IPFilter.webflow.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rizak on 08/04/14.
 */
public class IPRulesModel implements Serializable
{
    private String name;
    private String description;
    private String type;
    private String ipFrom;
    private String ipTo;
    private List<IPRule> ipRules;

    public IPRulesModel()
    {
        ipRules = new ArrayList<IPRule>();
    }

    public IPRulesModel(String name, String description, String type, String ipFrom, String ipTo)
    {
        ipRules = new ArrayList<IPRule>();
        this.name=name;
        this.description=description;
        this.type=type;
        this.ipFrom=ipFrom;
        this.ipTo=ipTo;
    }


    public String getDescription() {
        return description;
    }

    public String getIpFrom() {
        return ipFrom;
    }

    public String getIpTo() {
        return ipTo;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }


    public void setDescription(String description) {
        this.description = description;
    }

    public void setIpFrom(String ipFrom) {
        this.ipFrom = ipFrom;
    }

    public void setIpTo(String ipTo) {
        this.ipTo = ipTo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }


}
