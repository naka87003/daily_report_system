<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ page import="constants.ForwardConst"%>

<c:set var="actRep" value="${ForwardConst.ACT_REP.getValue()}" />
<c:set var="commIdx" value="${ForwardConst.CMD_INDEX.getValue()}" />
<c:set var="commShowLikes"
    value="${ForwardConst.CMD_SHOW_LIKES.getValue()}" />

<c:import url="/WEB-INF/views/layout/app.jsp">
    <c:param name="content">
        <h2>いいねした人一覧ページ</h2>
        <table id="like_list">
            <tbody>
                <tr>
                    <th class="like_name">氏名</th>
                    <th class="report_date">いいねした日時</th>
                </tr>
                <c:forEach var="like" items="${likes}" varStatus="status">
                    <fmt:parseDate value="${like.createdAt}"
                        pattern="yyyy-MM-dd'T'HH:mm:ss" var="creatDay" type="date" />

                    <tr class="row${status.count % 2}">
                        <td class="like_name"><c:out value="${like.employee.name}" /></td>
                        <td class="like_date"><fmt:formatDate value='${creatDay}'
                                pattern="yyyy-MM-dd HH:mm:ss" /></td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>

        <div id="pagination">
            （全 ${likes_count} 件）<br />
            <c:forEach var="i" begin="1"
                end="${((likes_count - 1) / maxRow) + 1}" step="1">
                <c:choose>
                    <c:when test="${i == page}">
                        <c:out value="${i}" />&nbsp;
                    </c:when>
                    <c:otherwise>
                        <a
                            href="<c:url value='?action=${actRep}&command=${commShowLikes}&id=${report.id}&page=${i}' />"><c:out
                                value="${i}" /></a>&nbsp;
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </div>
        <p>
            <a href="<c:url value='?action=${actRep}&command=${commIdx}' />">一覧に戻る</a>
        </p>

    </c:param>
</c:import>