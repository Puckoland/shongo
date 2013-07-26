<%--
  -- Page which is displayed when uncaught exception is thrown or when other error happens.
  --%>
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="tag" uri="/WEB-INF/client-web.tld" %>

<div class="error">
    <spring:message code="views.errorNotFound.page"/>
    <pre><%= request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI) %></pre>
    <spring:message code="views.errorNotFound.notFound"/>
</div>