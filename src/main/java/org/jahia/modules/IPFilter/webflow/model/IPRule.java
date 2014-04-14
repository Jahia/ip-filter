package org.jahia.modules.IPFilter.webflow.model;

import java.io.Serializable;

/**
 * Created by rizak on 08/04/14.
 */
public class IPRule implements Serializable
{
    public final static String IPTYPE_RANGE="range";
    public final static String IPTYPE_PATTERN="pattern";
    private static final long serialVersionUID = -558162577353590216L;
    private String description;
    private String id;
    private String ipMask;
    private String name;
    private String siteName;
    private String type;
    private boolean status;

    public int compareTo(IPRule rule) {
        return (name + siteName).compareTo(rule.getName() + rule.getSiteName());
    }

    public IPRule(String description, String id, String ipMask, String name, String siteName, boolean status, String type )
    {
        this.description=description;
        this.id=id;
        this.ipMask=ipMask;
        this.name=name;
        this.siteName=siteName;
        this.type=type;
        this.status=status;
    }

    public IPRule()
    {
        this.description="";
        this.id="";
        this.ipMask="";
        this.name="";
        this.siteName="";
        this.type="";
        this.status=true;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public String getIpMask() {
        return ipMask;
    }

    public String getName() {
        return name;
    }

    public String getSiteName() {
        return siteName;
    }

    public boolean isStatus() {
        return status;
    }

    public String getType() {
        return type;
    }


    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIpMask(String ipMask) {
        this.ipMask = ipMask;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IPRule ipRule = (IPRule) o;

        if (!id.equals(ipRule.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
