<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>  
<%@page import="org.springframework.context.ApplicationContext"%>  
<%@page import="com.wizardyu.service.Lucene"%>  
<html>
<body>
	<h2>Hello World!</h2>
	<%
		ServletContext sc = this.getServletContext();  
	    ApplicationContext ac = WebApplicationContextUtils.getWebApplicationContext(sc);  
	    Lucene lucene = (Lucene) ac.getBean("luceneService");
	%>
</body>
</html>
