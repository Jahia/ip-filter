<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ page import="javax.jcr.RepositoryException" %>
<%@ page import="javax.jcr.Value" %>
<%@ page import="java.util.HashSet" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
<%@ taglib prefix="facet" uri="http://www.jahia.org/tags/facetLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="ipRulesModel" type="org.jahia.modules.IPFilter.webflow.model.IPRulesModel"--%>

<template:addResources type="css" resources="bootstrap-switch.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="javascript" resources="admin-bootstrap.js"/>
<template:addResources type="javascript" resources="bootstrap-switch.js"/>
<template:addResources type="javascript" resources="IPFilterUtils.js"/>
<template:addResources>
    <script type="text/javascript">
        var currentElement;
        var currentForm;
        var philosophiesMap =
        {
            <c:forEach items="${ipRulesModel.sitesPhilosophy}" var="philosophy" varStatus="loopStatus">
            '${philosophy.key}' : '${philosophy.value}'<c:if test="${!loopStatus.last}">,</c:if>
            </c:forEach>
        };

        var philosophiesDisplayMap =
        {
            <c:forEach items="${ipRulesModel.sitesPhilosophy}" var="philosophy" varStatus="loopStatus">
            '${philosophy.key}' : '<fmt:message key="jnt_ipRestriction.ruleType.${philosophy.value}"/>'<c:if test="${!loopStatus.last}">,</c:if>
            </c:forEach>
        };

        $(document).ready(function ()
        {
            ApplyRuleConstraints();
            $(".statusSwitch").bootstrapSwitch();
            $(".updateField").attr("disabled","disabled");
            switchErrorRows();
        });
    </script>
</template:addResources>

<c:set var="ipRuleList" value="${ipRulesModel.ipRuleList}"/>
<jcr:node path="/sites" var="sitesVar" />
<div class="container">
    <h3 class="text-center"><fmt:message key="ipFilter.title"/></h3>
    <h5>Rules creation</h5>
    <div class="row-fluid">
        <div class="alert alert-info">
            <div class="span6">
                <form:form name="updateRules" class="createRulesForm form-horizontal" action="${flowExecutionUrl}" method="post" modelAttribute="ipRulesModel">
                    <div class="control-group">
                        <form:label class="control-label" path="toBeCreated.name"><fmt:message key="ipFilter.form.name"/> : </form:label>
                        <div class="controls">
                            <form:input path="toBeCreated.name"/>
                            <div class="text-error">
                                <form:errors path="toBeCreated.name"/>
                            </div>
                        </div>
                    </div>
                    <div class="control-group">
                        <form:label class="control-label" path="toBeCreated.description"><fmt:message key="ipFilter.form.description"/> : </form:label>
                        <div class="controls">
                            <form:input path="toBeCreated.description"/>
                        </div>
                    </div>
                    <div class="control-group sites">
                        <form:label class="control-label" path="toBeCreated.siteName"><fmt:message key="ipFilter.form.applyOn"/> : </form:label>
                        <div class="controls">
                            <form:select path="toBeCreated.siteName" onchange="ApplyRuleConstraints()">
                                <form:option value="all"> <fmt:message key="ipFilter.form.all"/></form:option>
                                <c:forEach var="site" items="${jcr:getDescendantNodes(sitesVar, 'jnt:virtualsite')}">
                                    <form:option value="${site.name}"> ${site.displayableName}</form:option>
                                </c:forEach>
                            </form:select>
                            <div class="text-error">
                                <form:errors path="toBeCreated.siteName"/>
                            </div>
                        </div>
                    </div>
                    <div class="control-group ruleType">
                        <form:label class="control-label ruleType" path="toBeCreated.type"><fmt:message key="ipFilter.form.type"/> : </form:label>
                        <div class="controls">
                            <select onchange="ApplyRuleConstraints()">
                                <option value="onlyallow"><fmt:message key="jnt_ipRestriction.ruleType.onlyallow"/></option>
                                <option value="deny"><fmt:message key="jnt_ipRestriction.ruleType.deny"/></option>
                            </select>
                            <input type="text" name="displayType"  class="hide" disabled="disabled"/>
                            <form:hidden path="toBeCreated.type" value="onlyallow"/>
                            <div class="text-error">
                                <form:errors path="toBeCreated.type"/>
                            </div>
                        </div>
                    </div>
                    <div class="control-group">
                        <form:label class="control-label" path="toBeCreated.ipMask"><fmt:message key="ipFilter.form.ipMask"/> : </form:label>
                        <div class="controls">
                            <form:input path="toBeCreated.ipMask" cssClass="ipMask"/>
                            <div class="text-error">
                                <form:errors path="toBeCreated.ipMask"/>
                            </div>
                        </div>
                    </div>
                    <div class="form-action">
                        <div class="control-group">
                            <div class="controls">
                                <form:hidden value="true" path="creationPhase"/>
                                <button type="submit" name="_eventId_ipRulesCreateRules" class="btn btn-primary"><fmt:message key="save"/></button>
                                <button type="submit" name="_eventId_ipRulesCancel"  class="btn">
                                    <i class="icon-ban-circle"></i>
                                    <fmt:message key="cancel"/>
                                </button>
                            </div>
                        </div>
                    </div>
                </form:form>
            </div>
            <div class="span1"></div>
            <div class="span5" style="text-align:justify">
                <dl>
                    <dt class="text-center"><fmt:message key="ipFilter.help.title"/></dt>
                    <dd>

                        <p>
                            <div><fmt:message key="ipFilter.help.ipFilterMaskHelp"/></div>
                        </p>
                        <p>
                            <div><fmt:message key="ipFilter.help.ipFilterMaskHelp2"/></div>
                            <div><fmt:message key="ipFilter.help.ipFilterMaskHelp3"/></div>
                            <div style="margin-top:5px;">&nbsp;<strong><fmt:message key="ipFilter.help.ipFilterMaskHelp4"/></strong></div>
                        </p>
                        <p>
                            <div><fmt:message key="ipFilter.help.ipFilterMaskHelp5"/></div>
                            <div><fmt:message key="ipFilter.help.ipFilterMaskHelp6"/></div>
                        </p>
                        <div><fmt:message key="ipFilter.help.ipFilterMaskHelp7"/></div>
                    </dd>
                </dl>
            </div>
            <div class="clearfix"></div>
         </div>
        <div class="clearfix"></div>
    </div>
</div>
<div class="container">
    <div class="row-fluid">
        <div class="span12">
            <h5>Existing rules</h5>
            <c:choose>
                <c:when test="${empty ipRuleList}">
                    <div class="box-1">
                        <fmt:message key="ipFilter.form.noRuleFound"/>
                    </div>
                </c:when>
                <c:otherwise>
                        <div class="selectSite">
                            <form:form name="updateRules" class="selectSiteForm form-horizontal" action="${flowExecutionUrl}" method="post" modelAttribute="ipRulesModel">
                                <form:select path="selectedSite" onchange="$('.selectSiteEventId').prop('disabled',false);$('.selectSiteForm').submit()">
                                    <form:option value="all"> <fmt:message key="ipFilter.form.all"/></form:option>
                                    <c:forEach var="site" items="${jcr:getDescendantNodes(sitesVar, 'jnt:virtualsite')}">
                                        <form:option value="${site.name}"> ${site.displayableName}</form:option>
                                    </c:forEach>
                                </form:select>
                                <input type="hidden" class="selectSiteEventId" name="_eventId" value="ipRulesSelectSite" disabled="disabled"/>
                            </form:form>
                        </div>

                        <c:forEach items="${ipRuleList}" var="ipRule" varStatus="keys">
                            <c:choose>
                                <c:when test="${ipRule.siteName eq 'all'}">
                                    <fmt:message key="ipFilter.form.all" var="siteName"/>
                                </c:when>
                                <c:otherwise>
                                    <jcr:node var="site" path="/sites/${ipRule.siteName}"/>
                                    <c:set var="siteName" value="${site.displayableName}"/>
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${(ipRulesModel.selectedSite == ipRule.siteName) or (ipRulesModel.selectedSite == 'all')}">
                                <form:form name="updateRules" class="updateRulesForm form-horizontal" action="${flowExecutionUrl}" method="post" modelAttribute="ipRulesModel">
                                        <div id="filter_${keys.count}" class="row-fluid">
                                            <div class="box-1">
                                                <div class="span11">
                                                     <div class="span2"><strong><fmt:message key="ipFilter.form.name"/></strong> : ${ipRule.name}&nbsp;</div>
                                                     <div class="span2">
                                                         <strong><fmt:message key="ipFilter.form.applyOn"/></strong> : ${siteName}&nbsp;
                                                     </div>
                                                     <div class="span2"><strong><fmt:message key="ipFilter.form.description"/></strong> : ${ipRule.description}&nbsp;</div>
                                                     <div class="span2"><strong><fmt:message key="ipFilter.form.type"/></strong> : ${ipRule.type}&nbsp;</div>
                                                     <div class="span2">
                                                         <strong><fmt:message key="ipFilter.form.ipMask"/></strong> : ${ipRule.ipMask}&nbsp;
                                                     </div>
                                                     <div class="span1">
                                                         <c:choose>
                                                             <c:when test="${ipRule.status}">
                                                                 <span class="text-success"><strong><fmt:message key="ipFilter.form.active"/></strong></span>&nbsp;
                                                             </c:when>
                                                             <c:otherwise>
                                                                 <span class="text-error"><fmt:message key="ipFilter.form.inactive"/></span>&nbsp;
                                                             </c:otherwise>
                                                         </c:choose>
                                                     </div>
                                                </div>
                                                <div class="span1">
                                                     <span>
                                                         <button class="btn btn-primary" type="button" onclick="switchRow('filter_${keys.count}')">
                                                             <fmt:message key="label.clickToEdit"/>
                                                         </button>
                                                     </span>
                                                </div>
                                                <div class="clearfix"></div>
                                            </div>
                                        </div>
                                    <div id="filter_${keys.count}_form" class="row-fluid hide" style="margin-top:15px;">
                                        <fmt:message key='ipFilter.form.active' var="activeLabel"/>
                                        <fmt:message key='ipFilter.form.inactive' var="inactiveLabel"/>
                                        <div class="alert alert-info">
                                                ${siteName}
                                            <div class="control-group">
                                                <form:label class="control-label" path="ipRuleList[${keys.count-1}].name"><fmt:message key="ipFilter.form.name"/> : </form:label>
                                                <div class="controls">
                                                    <form:input path="ipRuleList[${keys.count-1}].name" cssClass="updateField"/>
                                                    <div>
                                                        <c:if test="${ipRulesModel.ruleIndex eq keys.count-1}"><form:errors path="toBeUpdated.name" cssClass="fieldError" parentId="filter_${keys.count}"/></c:if>
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="control-group">
                                                <form:label class="control-label" path="ipRuleList[${keys.count-1}].description"><fmt:message key="ipFilter.form.description"/> : </form:label>
                                                <div class="controls">
                                                    <form:input path="ipRuleList[${keys.count-1}].description" cssClass="updateField"/>
                                                </div>
                                            </div>
                                            <div class="control-group">
                                                <form:label class="control-label" path="ipRuleList[${keys.count-1}].ipMask"><fmt:message key="ipFilter.form.ipMask"/> : </form:label>
                                                <div class="controls">
                                                    <form:input path="ipRuleList[${keys.count-1}].ipMask" cssClass="updateField"/>
                                                    <div>
                                                        <c:if test="${ipRulesModel.ruleIndex eq keys.count-1}"><form:errors path="toBeUpdated.ipMask" cssClass="fieldError" parentId="filter_${keys.count}"/></c:if>
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="control-group">
                                                <div class="controls">
                                                    <form:checkbox cssClass="updateField statusSwitch" path="ipRuleList[${keys.count-1}].status" data-size="mini" data-on-text="${activeLabel}" data-off-text="${inactiveLabel}" data-on-color="success" data-off-color="danger"/>
                                                </div>
                                            <div class="clear"></div>
                                            </div>
                                            <div class="form-action">
                                                <div class="control-group">
                                                    <div class="controls">
                                                        <form:hidden path="ruleIndex" cssClass="formIndex" value="${keys.count-1}"/>
                                                        <form:hidden value="true" path="updatePhase" cssClass="updateField"/>
                                                        <button type="submit" name="_eventId_ipRulesUpdateRules" class="btn btn-primary" ><fmt:message key="save"/></button>
                                                        <button type="submit" name="_eventId_ipRulesCancel"  class="btn">
                                                            <i class="icon-ban-circle"></i>
                                                            <fmt:message key="cancel"/>
                                                        </button>
                                                        <button type="submit" name="_eventId_ipRulesDeleteRules" class="btn btn-danger" ><fmt:message key="delete"/></button>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </form:form>
                            </c:if>
                        </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>
