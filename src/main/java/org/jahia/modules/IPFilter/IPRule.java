package org.jahia.modules.IPFilter;

import java.io.Serializable;

/**
 * This class defines the object IP Rule with all its methodes and properties
 * Created by Rahmed on 08/04/14.
 */
public class IPRule implements Serializable {
    private static final long serialVersionUID = -558162577353590216L;
    private String description;
    private String id;
    private String ipMask;
    private String name;
    private String siteName;
    private String type;
    private boolean active;

    /**
     * This function override the IPRule Object comparaison to order IP Rules by name/sitename
     * It is used in by the list of IPRules in the Module view
     *
     * @param rule : The other IP rule with which compare this object
     * @return
     */
    public int compareTo(IPRule rule) {
        return (name + siteName).compareTo(rule.getName() + rule.getSiteName());
    }

    /**
     * IP Rule full constructor
     *
     * @param description
     * @param id
     * @param ipMask
     * @param name
     * @param siteName
     * @param active
     * @param type
     */
    public IPRule(String description, String id, String ipMask, String name, String siteName, boolean active, String type) {
        this.description = description;
        this.id = id;
        this.ipMask = ipMask;
        this.name = name;
        this.siteName = siteName;
        this.type = type;
        this.active = active;
    }

    /**
     * IP Rule empty constructor
     */
    public IPRule() {
        this.description = "";
        this.id = "";
        this.ipMask = "";
        this.name = "";
        this.siteName = "";
        this.type = "";
        this.active = true;
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

    public boolean isActive() {
        return active;
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

    public void setActive(boolean status) {
        this.active = status;
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
