<%--
  -- Page layout template to which are inserted all other pages into "body" attribute.
  --%>
<%@ page import="org.springframework.web.util.UriComponentsBuilder" %>
<%@ page import="cz.cesnet.shongo.client.web.ClientWebUrl" %>
<%@ page import="cz.cesnet.shongo.client.web.support.interceptors.NavigationInterceptor" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="cs" xml:lang="cs">

<%-- Tag Libraries --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="tag" uri="/WEB-INF/client-web.tld" %>

<%-- Variables --%>
<tiles:importAttribute/>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<c:set var="name" value="${configuration.getName(requestContext.locale.language)}"/>
<tag:url var="reportUrl" value="<%= ClientWebUrl.REPORT %>">
    <tag:param name="back-url" value="${requestScope.requestUrl}"/>
</tag:url>
<tag:url var="changelogUrl" value="<%= ClientWebUrl.CHANGELOG %>"/>

<%
    // URL for changing language
    String requestUrl = (String) request.getAttribute(NavigationInterceptor.REQUEST_URL_REQUEST_ATTRIBUTE);
    UriComponentsBuilder languageUrlBuilder = UriComponentsBuilder.fromUriString(requestUrl);
    languageUrlBuilder.replaceQueryParam("lang", ":lang");
    pageContext.setAttribute("languageUrl", languageUrlBuilder.build().toUriString());
%>

<%-- Header --%>
<head>
    <title>
        ${name}
        <c:choose>
            <c:when test="${title.getClass().name == 'java.lang.String' && not empty title}">
                <spring:message code="${title}" var="title"/>
                - ${title}
            </c:when>
            <c:otherwise>
                <c:set var="title">
                    <c:forEach items="${title}" var="titleItem" varStatus="titleStatus">
                        <c:if test="${titleItem != null}">
                            <c:set var="titleItem"><tiles:insertAttribute value="${titleItem}"/></c:set>
                            <c:choose>
                                <c:when test="${empty titleItem}"/>
                                <c:when test="${!titleItem.startsWith('T(')}">
                                    <c:if test="${!titleStatus.first}"> - </c:if>
                                    <spring:message code="${titleItem}"/>
                                </c:when>
                                <c:when test="${titleItem.length() > 3}">
                                    <c:if test="${!titleStatus.first}"> - </c:if>
                                    ${titleItem.substring(2, titleItem.length() - 1)}
                                </c:when>
                            </c:choose>
                        </c:if>
                    </c:forEach>
                </c:set>
                <c:if test="${not empty title}">
                    - ${title}
                </c:if>
            </c:otherwise>
        </c:choose>
    </title>

    <c:forEach items="${css}" var="file">
        <link rel="stylesheet" href="${contextPath}/css/${file}"/>
    </c:forEach>
    <c:forEach items="${js}" var="file">
        <script src="${contextPath}/js/${file}"></script>
    </c:forEach>
    <c:if test="${requestContext.locale.language != 'en'}">
        <c:forEach items="${i18n}" var="file">
            <script src="${contextPath}/js/${file}.${requestContext.locale.language}.js"></script>
        </c:forEach>
    </c:if>
    <script type="text/javascript">
        $(function () {
            $('.timezone').qtip({
                position: {
                    my: 'top right',
                    at: 'bottom center'
                },
                style: {
                    classes: 'qtip-app'
                }
            });
        });
    </script>
</head>

<body id="top">

<div class="content">

    <%-- Page navigation header --%>
    <div class="navbar navbar-static-top block">
        <div class="navbar-inner">
            <%-- Left panel - application name and main links --%>
            <div class="main">
                <a class="brand" href="/">${name}</a>
                <div class="pull-left">
                    <ul class="nav" role="navigation">
                        <%-- Button which represents collapsed main links --%>
                        <li>
                            <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                                <div>
                                    <span class="icon-bar"></span>
                                    <span class="icon-bar"></span>
                                    <span class="icon-bar"></span>
                                </div>
                                <span>&nbsp;<spring:message code="views.layout.menu"/></span>
                            </button>
                        </li>
                    </ul>
                </div>
                <div class="nav-collapse collapse pull-left">
                    <ul class="nav" role="navigation">
                        <li>
                            <tag:url var="reservationRequestListUrl" value="<%= ClientWebUrl.RESERVATION_REQUEST_LIST %>"/>
                            <a href="${reservationRequestListUrl}"><spring:message code="navigation.reservationRequest"/></a>
                        </li>
                        <c:if test="${sessionScope.SHONGO_USER.administratorMode}">
                            <li>
                                <tag:url var="roomListUrl" value="<%= ClientWebUrl.ROOM_LIST_VIEW %>"/>
                                <a href="${roomListUrl}"><spring:message code="navigation.roomList"/></a>
                            </li>
                        </c:if>
                        <security:authorize access="isAuthenticated()">
                            <tag:url var="userSettingsUrl" value="<%= ClientWebUrl.USER_SETTINGS %>">
                                <tag:param name="back-url" value="${requestScope.requestUrl}"/>
                            </tag:url>
                            <li>
                                <a class="menuitem" href="${userSettingsUrl}"><spring:message code="views.layout.settings"/></a>
                            </li>
                        </security:authorize>
                        <li>
                            <tag:url var="helpUrl" value="<%= ClientWebUrl.HELP %>"/>
                            <a href="${helpUrl}"><spring:message code="navigation.help"/></a>
                        </li>
                    </ul>
                </div>
            </div>

            <%-- Right panel - user, timezone, language --%>
            <ul class="nav pull-right">

                <%-- Login button --%>
                <security:authorize access="!isAuthenticated()">
                    <li>
                        <tag:url var="loginUrl" value="<%= ClientWebUrl.LOGIN %>"/>
                        <a href="${loginUrl}"><spring:message code="views.layout.login"/></a>
                    </li>
                </security:authorize>

                <%-- Logged user information --%>
                <security:authorize access="isAuthenticated()">
                    <tag:url var="userSettingsUrl" value="<%= ClientWebUrl.USER_SETTINGS %>">
                        <tag:param name="back-url" value="${requestScope.requestUrl}"/>
                    </tag:url>
                    <tag:url var="advancedUserInterfaceUrl" value="<%= ClientWebUrl.USER_SETTINGS_ATTRIBUTE %>">
                        <tag:param name="name" value="userInterface"/>
                        <tag:param name="value" value="${sessionScope.SHONGO_USER.advancedUserInterface ? 'BEGINNER' : 'ADVANCED'}"/>
                        <tag:param name="back-url" value="${requestScope.requestUrl}"/>
                    </tag:url>
                    <tag:url var="logoutUrl" value="<%= ClientWebUrl.LOGOUT %>"/>
                    <li class="dropdown">
                        <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                            <b class="icon-cog"></b>
                            <b><security:authentication property="principal.fullName"/></b><c:if test="${sessionScope.SHONGO_USER.administratorMode}">&nbsp;(<spring:message code="views.layout.user.administratorMode"/>)</c:if>
                            <b class="caret"></b>
                        </a>
                        <ul class="dropdown-menu" role="menu">
                            <li>
                                <a class="menuitem" href="${userSettingsUrl}"><spring:message code="views.layout.settings"/>...</a>
                            </li>
                            <li class="divider"></li>
                            <li>
                                <a class="menuitem" href="${advancedUserInterfaceUrl}">
                                    <c:if test="${sessionScope.SHONGO_USER.advancedUserInterface}"><span class="icon-ok"></span></c:if><%--
                                    --%><spring:message code="views.userSettings.advancedUserInterface"/>
                                </a>
                            </li>
                            <security:authorize access="hasPermission(ADMINISTRATION)">
                                <tag:url var="administratorModeUrl" value="<%= ClientWebUrl.USER_SETTINGS_ATTRIBUTE %>">
                                    <tag:param name="name" value="administratorMode"/>
                                    <tag:param name="value" value="${!sessionScope.SHONGO_USER.administratorMode}"/>
                                    <tag:param name="back-url" value="${requestScope.requestUrl}"/>
                                </tag:url>
                                <li>
                                    <a class="menuitem" href="${administratorModeUrl}">
                                        <c:if test="${sessionScope.SHONGO_USER.administratorMode}"><span class="icon-ok"></span></c:if><%--
                                    --%><spring:message code="views.userSettings.administratorMode"/>
                                    </a>
                                </li>
                            </security:authorize>
                            <li class="divider"></li>
                            <li>
                                <a class="menuitem" href="${logoutUrl}"><spring:message code="views.layout.logout"/></a>
                            </li>
                        </ul>
                    </li>
                    <security:authorize access="!hasPermission(RESERVATION)">
                        <tag:url var="helpUrl" value="<%= ClientWebUrl.HELP %>"/>
                        <script type="text/javascript">
                            $(function () {
                                $('#warning').qtip({
                                    content: { text: "<spring:message code="views.layout.user.reservationDisabled" arguments="${helpUrl}#loa" javaScriptEscape="true"/>" },
                                    position: { my: 'top right', at: 'bottom center' },
                                    style: { classes: 'qtip-app' },
                                    hide: { fixed: true, delay: 300 }
                                });
                            });
                        </script>
                        <li class="navbar-text" style="margin-left: 0px;">
                            <b id="warning" class="icon-warning-sign" style="color: #f71;"></b>
                        </li>
                    </security:authorize>
                </security:authorize>

                <%-- Timezone --%>
                <li>
                    <spring:eval expression="T(cz.cesnet.shongo.client.web.models.TimeZoneModel).formatTimeZone(sessionScope.SHONGO_USER.timeZone)" var="timeZone"/>
                    <%-- Timezone Help --%>
                    <c:set var="timeZoneHelp">
                        <table>
                            <tr><td align='left' colspan='2'><b style='text-align: left;'><spring:message code="views.layout.timezone"/></b></td></tr>
                            <%-- Current --%>
                            <tr>
                                <td style='text-align: right; vertical-align: top;'><spring:message code="views.layout.timezone.current"/>:</td>
                                <td style='text-align: left;'>
                                    <b>${timeZone}</b>
                                    <spring:eval var="timeZoneName" expression="T(cz.cesnet.shongo.client.web.models.TimeZoneModel).formatTimeZoneName(sessionScope.SHONGO_USER.timeZone, sessionScope.SHONGO_USER.locale)"/>
                                    <c:if test="${not empty timeZoneName}">
                                        (${timeZoneName})
                                    </c:if>
                                    <%-- Difference between Current and Home --%>
                                    <c:if test="${sessionScope.SHONGO_USER.homeTimeZone != null && sessionScope.SHONGO_USER.homeTimeZone != sessionScope.SHONGO_USER.timeZone}">
                                        , <spring:message code="views.layout.timezone.diff"/>:&nbsp;<tag:format value="${sessionScope.SHONGO_USER.timeZoneOffset}" style="time"/>
                                    </c:if>
                                </td>
                            </tr>
                            <%-- Home --%>
                            <c:if test="${sessionScope.SHONGO_USER.homeTimeZone != null && sessionScope.SHONGO_USER.homeTimeZone != sessionScope.SHONGO_USER.timeZone}">
                                <tr>
                                    <td style='text-align: right; vertical-align: top;'><spring:message code="views.layout.timezone.home"/>:</td>
                                    <td align='left'>
                                        <b><spring:eval expression="T(cz.cesnet.shongo.client.web.models.TimeZoneModel).formatTimeZone(sessionScope.SHONGO_USER.homeTimeZone)"/></b>
                                        <spring:eval var="homeTimeZoneName" expression="T(cz.cesnet.shongo.client.web.models.TimeZoneModel).formatTimeZoneName(sessionScope.SHONGO_USER.homeTimeZone, sessionScope.SHONGO_USER.locale)"/>
                                        <c:if test="${not empty homeTimeZoneName}">
                                            (${homeTimeZoneName})
                                        </c:if>
                                    </td>
                                </tr>
                            </c:if>
                        </table>
                    </c:set>
                    <spring:eval expression="T(cz.cesnet.shongo.client.web.models.TimeZoneModel).formatTimeZone(sessionScope.SHONGO_USER.timeZone)" var="timeZone"/>
                    <span class="navbar-text timezone" title="${timeZoneHelp}">${timeZone}</span>
                </li>

                <%-- Language selection --%>
                <li>
                    <span class="navbar-text">
                        <a id="language-english" href="${languageUrl.replaceAll(":lang", "en")}"><img class="language" src="${contextPath}/img/i18n/en.png" alt="English" title="English"/></a>
                        <a id="language-czech" href="${languageUrl.replaceAll(":lang", "cs")}"><img class="language" src="${contextPath}/img/i18n/cz.png" alt="Česky" title="Česky"/></a>
                    </span>
                </li>
            </ul>

            <%-- Breadcrumbs --%>
            <c:if test="${requestScope.breadcrumb != null}">
                <ul class="breadcrumb">
                    <c:forEach items="${requestScope.breadcrumb.iterator()}" var="item" varStatus="status">
                        <c:choose>
                            <c:when test="${!status.last}">
                                <li>
                                    <a href="${contextPath}${item.url}"><spring:message code="${item.titleCode}"/></a>
                                    <span class="divider">/</span>
                                </li>
                            </c:when>
                            <c:otherwise>
                                <li class="active"><spring:message code="${item.titleCode}"/></li>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                    <li class="pull-right">
                        <a href="${reportUrl}"><spring:message code="views.layout.report"/></a>
                    </li>
                </ul>
            </c:if>
        </div>
    </div>

    <%-- Page content --%>
    <div class="block push">
        <div class="container" id="page-content">
            <c:choose>
                <c:when test="${heading == 'title'}">
                    <h1>${title}</h1>
                </c:when>
                <c:when test="${heading != ''}">
                    <h1>${heading}</h1>
                </c:when>
            </c:choose>
            <tiles:insertAttribute name="body"/>
        </div>
    </div>

    <%-- Page footer --%>
    <div class="footer block">
        <p class="muted">
            <a href="${changelogUrl}">Powered by Shongo&nbsp;<spring:message code="system.version"/></a>
            &copy; 2012 - 2014&nbsp;&nbsp;&nbsp;
            <a title="CESNET" href="http://www.cesnet.cz/">
                <img src="${contextPath}/img/cesnet.gif" alt="CESNET, z.s.p.o."/>
            </a>
        </p>
    </div>

</div>

</body>

</html>
