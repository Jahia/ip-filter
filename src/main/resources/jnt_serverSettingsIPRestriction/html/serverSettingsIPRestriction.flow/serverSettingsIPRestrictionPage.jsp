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
<%@ include file="../../../getUser.jspf"%>

<template:addResources type="css" resources="bootstrap-switch.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="javascript" resources="admin-bootstrap.js"/>
<template:addResources type="javascript" resources="bootstrap-switch.js"/>
<template:addResources type="javascript" resources="datatables/jquery.dataTables.js,i18n/jquery.dataTables-${currentResource.locale}.js,datatables/dataTables.bootstrap-ext.js"/>
<template:addResources>
    <script type="text/javascript">
        //View Javascript Code Here

        $(document).ready(function () {
            $('#ipRules_table').dataTable({
                "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                "iDisplayLength":10,
                "bPaginate": false,
                "aaSorting": [] //this option disable sort by default, the user steal can use column names to sort the table
            });
        });
    </script>
</template:addResources>
<h3><fmt:message key="jmix_ipRestriction.management"/></h3>

<h5>Rules creation</h5>
<div class="container">
    <div class="row-fluid">
        <div class="alert alert-info">
            <div class="span12">
                <form:form name="updateRules" class="createRulesForm form-horizontal" action="${flowExecutionUrl}" method="post" modelAttribute="ipRulesModel">
                    <div class="control-group">
                        <form:label class="control-label" path="name"><fmt:message key="jmix_ipRestriction.form.name"/> : </form:label>
                        <div class="controls">
                            <form:input path="name"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <form:label class="control-label" path="description"><fmt:message key="jmix_ipRestriction.form.description"/> : </form:label>
                        <div class="controls">
                            <form:input path="description"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <form:label class="control-label" path="type"><fmt:message key="jmix_ipRestriction.form.type"/> : </form:label>
                        <div class="controls">
                            <form:input path="type"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <form:label class="control-label" path="ipFrom"><fmt:message key="jmix_ipRestriction.form.fromIP"/> : </form:label>
                        <div class="controls">
                            <form:input path="ipFrom"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <form:label class="control-label" path="ipTo"><fmt:message key="jmix_ipRestriction.form.toIP"/> : </form:label>
                        <div class="controls">
                            <form:input path="ipTo"/>
                        </div>
                    </div>
                    <div class="form-action">
                        <div class="control-group">
                            <div class="controls">
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
            <div class="clearfix"></div>
         </div>
    </div>
</div>

<h5>Existing rules</h5>
<div class="container">
    <div class="box-1">
        <div class="row-fluid">
            <div class="span12">
                <form:form name="updateRules" class="updateRulesForm form-horizontal" action="${flowExecutionUrl}" method="post" modelAttribute="ipRulesModel">
                <table cellpadding="0" cellspacing="0" border="0" class="table table-hover table-bordered" id="ipRules_table">
                    <thead>
                    <tr>
                        <th class="col-sm-4">
                            Name
                        </th>
                        <c:forEach var="property" items="${bulkEditorModel.availableProperties}">
                            <th class="col-sm-2">
                                    ${property}
                            </th>
                        </c:forEach>
                    </tr>
                    </thead>
                    <tbody>
                    <c:if test="${!empty bulkEditorModel.bulkContents}">
                        <c:forEach var="bulkContent" items="${bulkEditorModel.bulkContents}">
                            <tr>
                                <td>${bulkContent.jcrPropertiesMap["j:nodename"]}</td>
                                <c:forEach var="property" items="${bulkEditorModel.availableProperties}">
                                    <td>
                                        <div id="${property}_${bulkContent.jcrPropertiesMap["j:nodename"]}" onclick="switchRow('${property}_${bulkContent.jcrPropertiesMap["j:nodename"]}')" class="editable">
                                                ${bulkContent.displayPropertiesMap[property]}
                                        </div>
                                        <div id="${property}_${bulkContent.jcrPropertiesMap["j:nodename"]}_form" class="hide editable_form">
                                            <h5>Form Input </h5>
                                            <form:input path="change" disabled="true" onblur="formSubmit()"/>
                                            <form:hidden path="changeContentPath" value="${bulkContent.jcrPropertiesMap['j:fullpath']}" disabled="true"/>
                                            <form:hidden path="changeContentProperty" value="${property}" disabled="true"/>
                                        </div>
                                    </td>
                                </c:forEach>
                            </tr>
                        </c:forEach>
                    </c:if>
                    </tbody>
                </table>
                <div class="form-action">
                    <div class="control-group">
                        <div class="controls">
                            <button type="submit" name="_eventId_ipRulesUpdate" class="btn btn-primary"><fmt:message key="save"/></button>
                            <button type="submit" name="_eventId_ipRulesCancel"  class="btn">
                                <i class="icon-ban-circle"></i>
                                <fmt:message key="cancel"/>
                            </button>
                        </div>
                    </div>
                </div>
                </form:form>

            </div>
        </div>
    </div>
</div>