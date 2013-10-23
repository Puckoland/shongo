<%--
  -- Child reservation requests.
  --%>
<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ tag import="cz.cesnet.shongo.client.web.ClientWebUrl" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="tag" uri="/WEB-INF/client-web.tld" %>

<%@attribute name="detailUrl" required="true" %>

<script type="text/javascript">
    angular.provideModule('tag:reservationRequestChildren', ['ngPagination', 'ngSanitize']);
</script>

<tag:url var="childListUrl" value="<%= ClientWebUrl.RESERVATION_REQUEST_DETAIL_CHILDREN %>">
    <tag:param name="reservationRequestId" value=":id" escape="false"/>
</tag:url>
<tag:url var="childDetailUrl" value="${detailUrl}">
    <tag:param name="reservationRequestId" value="{{childReservationRequest.id}}" escape="false"/>
</tag:url>
<tag:url var="childRoomManagementUrl" value="<%= ClientWebUrl.ROOM_MANAGEMENT %>">
    <tag:param name="roomId" value="{{childReservationRequest.roomId}}" escape="false"/>
</tag:url>

<div ng-controller="PaginationController"
     ng-init="init('reservationRequestDetail.children', '${childListUrl}', {id: '${reservationRequest.id}'})">
    <spring:message code="views.pagination.records.all" var="paginationRecordsAll"/>
    <spring:message code="views.button.refresh" var="paginationRefresh"/>
    <pagination-page-size class="pull-right" unlimited="${paginationRecordsAll}" refresh="${paginationRefresh}">
        <spring:message code="views.pagination.records"/>
    </pagination-page-size>
    <h2><spring:message code="views.reservationRequestDetail.children"/></h2>

    <div class="spinner" ng-hide="ready || errorContent"></div>
    <span ng-controller="HtmlController" ng-show="errorContent" ng-bind-html="html(errorContent)"></span>
    <table class="table table-striped table-hover" ng-show="ready">
        <thead>
        <tr>
            <th width="320px"><pagination-sort column="SLOT">
                <spring:message code="views.reservationRequest.slot"/></pagination-sort>
            </th>
            <th><pagination-sort column="STATE">
                <spring:message code="views.reservationRequest.state"/></pagination-sort>
            </th>
            <th><spring:message code="views.room.aliases"/></th>
            <th style="min-width: 85px; width: 85px;">
                <spring:message code="views.list.action"/>
                <pagination-sort-default class="pull-right"><spring:message code="views.pagination.defaultSorting"/></pagination-sort-default>
            </th>
        </tr>
        </thead>
        <tbody>
        <tr ng-repeat="childReservationRequest in items">
            <td>{{childReservationRequest.slot}}</td>
            <td class="reservation-request-state">
                <tag:help label="{{childReservationRequest.stateMessage}}"
                          labelClass="{{childReservationRequest.state}}"
                          tooltipId="child-reservation-request-state-tooltip-{{$index}}">
                    <span>{{childReservationRequest.stateHelp}}</span>
                </tag:help>
            </td>
            <td ng-controller="HtmlController">
                <div ng-switch on="isEmpty(childReservationRequest.roomAliasesDescription)" style="display: inline-block;">
                    <div ng-switch-when="false">
                        <c:set var="executableAliases">
                            <span ng-bind-html="html(childReservationRequest.roomAliases)"></span>
                        </c:set>
                        <tag:help label="${executableAliases}"
                                  tooltipId="executableAliases-tooltip-{{$index}}">
                            <span ng-bind-html="html(childReservationRequest.roomAliasesDescription)"></span>
                        </tag:help>
                    </div>
                    <span ng-switch-when="true"
                          ng-bind-html="roomAliases(childReservationRequest)"></span>
                </div>
            </td>
            <td>
                <tag:listAction code="show" url="${childDetailUrl}" tabindex="2"/>
                <span ng-show="childReservationRequest.roomStateAvailable">
                    | <tag:listAction code="manage" url="${childRoomManagementUrl}" tabindex="2"/>
                </span>
            </td>
        </tr>
        </tbody>
        <tbody>
        <tr ng-hide="items.length">
            <td colspan="4" class="empty"><spring:message code="views.list.none"/></td>
        </tr>
        </tbody>
    </table>
    <pagination-pages ng-show="ready"><spring:message code="views.pagination.pages"/></pagination-pages>
</div>