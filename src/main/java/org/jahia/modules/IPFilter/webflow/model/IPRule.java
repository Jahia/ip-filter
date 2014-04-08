package org.jahia.modules.IPFilter.webflow.model;

import java.io.Serializable;

/**
 * Created by rizak on 08/04/14.
 */
public class IPRule implements Serializable {

    private String name;
    private String id;
    private String description;
    private String type;
    private String ipFrom;
    private String ipTo;

    public IPRule(String id, String name, String description, String type, String ipFrom, String ipTo)
    {
        this.id=id;
        this.name=name;
        this.description=description;
        this.type=type;
        this.ipFrom=ipFrom;
        this.ipTo=ipTo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRuleName() {
        return name;
    }

    public void setRuleName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIpFrom() {
        return ipFrom;
    }

    public void setIpFrom(String ipFrom) {
        this.ipFrom = ipFrom;
    }

    public String getIpTo() {
        return ipTo;
    }

    public void setIpTo(String ipTo) {
        this.ipTo = ipTo;
    }
}
