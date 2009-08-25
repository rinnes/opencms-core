<%@page buffer="none" session="false" import="org.opencms.frontend.templatetwo.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%

	// The main template JSP of TemplateTwo. 
	// This template is highly configurable in style and layout. The configuration
	// is done in several configuration files of the types "ttstyle", "ttconfig",
	// "ttoptions" and "ttpreset". Those configuration files are managed by the
	// class CmsTemplateLayout.
	//
	// For details on the CmsTemplateLayout class, see the source code which can
	// be found at the following VFS location:
	// /system/modules/org.opencms.frontend.templatetwo/java_src/CmsTemplateLayout.java

	CmsTemplateLayout cms = new CmsTemplateLayout(pageContext, request, response);
	pageContext.setAttribute("cms", cms);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<title><cms:property name="Title" file="search" /></title>
	<meta name="description" content="<cms:property name="Description" file="search" />" >
	<meta name="keywords" content="<cms:property name="Keywords" file="search" />" >
	<meta http-equiv="Content-Type" content="text/html; charset=${cms.requestContext.encoding}" >
	<meta name="robots" content="index, follow" >
	<meta name="revisit-after" content="7 days" >

	<link href="<cms:link>/system/modules/org.opencms.workplace.ade.demo/resources/menus/style2/style.css</cms:link>" rel="stylesheet" type="text/css"> 
	
	<link href="<cms:link>/system/modules/org.opencms.workplace.ade.demo/resources/css/nav_left.css</cms:link>" rel="stylesheet" type="text/css">		
		
	<link href="<cms:link>/system/modules/org.opencms.workplace.ade.demo/resources/css/style.css?preset=/demo_en/_config_/layout/threecolumns&amp;style=/demo_en/_config_/layout/style</cms:link>" rel="stylesheet" type="text/css">
	<!--[if lte IE 6]>
		<link href="<cms:link>/system/modules/org.opencms.workplace.ade.demo/resources/css/patch_ie.css</cms:link>" rel="stylesheet" type="text/css">
	<![endif]-->

	<cms:enable-ade/>
</head>

<body>
	<div id="window">
		<div id="page">
	  		
			<!-- begin: header -->
		    <div id="header">
		    	<div id="topnav">
		    		<a href="#content" class="skip">Skip to Main Content</a>
		    		<c:if test="${!cms.options.value['TopNav'].isEmptyOrWhitespaceOnly}">
		    			<c:out value="${cms.options.value['TopNav']}" escapeXml="false" />
		    		</c:if>
		    	</div>
		    	<c:if test="${!cms.options.value['Header'].isEmptyOrWhitespaceOnly}">
		    		<c:out value="${cms.options.value['Header'].resolveMacros}" escapeXml="false" />
		    	</c:if>
			</div>
			<!-- end: header -->
			
			<!-- begin: main navigation -->
			<c:choose>
				<c:when test="${!empty cms.styleValue['nav.main']}">
					<cms:include file="${cms.styleValue['nav.main']}"/>
				</c:when>
				<c:otherwise>
					<cms:include file="%(link.weak:/system/modules/org.opencms.frontend.templatetwo/elements/menu/nav_style2.jsp:ee5498d9-dbe4-11dc-b087-3bdd2ea0b1ac)"/>
				</c:otherwise>
			</c:choose>
			<!-- end: main navigation -->
			
			<!-- begin: breadcrumb -->
			<div id="breadcrumb">
				<cms:include file="%(link.weak:/system/modules/org.opencms.frontend.templatetwo/elements/breadcrumb.jsp:e72c6f1c-dbb3-11dc-af66-3bdd2ea0b1ac)" />
			</div>
			<!-- end: breadcrumb -->
			
			<!-- begin: content area #main -->
			<div id="main">
			
			<!-- begin: left column -->
			<div id="col1">
				<div id="col1_content" class="clearfix">
					<c:if test="${cms.presetValue['column.left.visible'] == 'true'}">
						<!-- include the left navigation menu -->
						<c:if test="${cms.presetValue['nav.left.visible'] == 'true'}">
							<cms:include file="%(link.weak:/system/modules/org.opencms.frontend.templatetwo/elements/menu/nav_left.jsp:ee1ea4f4-d97c-11dc-bc65-3bdd2ea0b1ac)" />
						</c:if>
						
						<div id="left">
						<!-- include the boxes on the left side -->
						<cms:container name="left" type="leftColumn" maxElements="3" />
						</div>
					</c:if>
				</div>
			</div>
			<!-- end: left column -->
			
			<!-- begin: right column -->
			<div id="col3">
				<div id="col3_content" class="clearfix">
					<c:catch>
						<c:set var="page" value="${cms:vfs(pageContext).readXml[cms:vfs(pageContext).context.uri]}" />
					</c:catch>
					
					<!-- include the boxes on the right side -->
					<c:if test="${cms.presetValue['column.right.visible'] == 'true'}">
						<div id="right">
						<!-- include the boxes on the right side -->
						<cms:container name="right" type="rightColumn" maxElements="3" />
						</div>
					</c:if>
               </div>
            </div>
            <!-- end: right column -->
			
			<!-- begin: center column -->
			<div id="col2">
				<div id="col2_content" class="clearfix">
					<!-- anchor for accessible link to main content -->
					<a id="content" name="content"></a>
						<div id="maincnt">
					<cms:container name="maincnt" type="mainColumn" maxElements="2" />
						</div>
				</div>
				<div class="clear">&nbsp;</div>
			</div>
			<!-- end: center column -->
			<br clear="all" />
			<!-- begin: bottom column -->
			<div id="bottom_cont">
				<cms:container name="bottom_cont" type="bottomColumn" maxElements="3" />
			</div>
			<!-- end: bottom column -->
		</div>
		<!-- end: content area -->
		
		<!-- begin: #footer -->
		<div id="footer">
			<c:if test="${!cms.options.value['Footer'].isEmptyOrWhitespaceOnly}">
		    	<c:out value="${cms.options.value['Footer'].resolveMacros}" escapeXml="false" />
		    </c:if>
		</div>
		<!-- end: #footer -->
		
	</div>
	<!-- end: #page -->
</div>
</body>
</html>