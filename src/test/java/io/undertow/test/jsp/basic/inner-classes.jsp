<%@page import="java.util.*"%>

<%
class PhoneBean {
    private String area;
    private String number;

    public PhoneBean(String area, String number) {
        this.area = area;
        this.number = number;
    }

    public String getArea() {
        return area;
    }

    public String getNumber() {
        return number;
    }
}

class EmployeeBean {
    private String id;
    private List<PhoneBean> phoneBeans = new ArrayList<>();

    String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    List<PhoneBean> getPhoneBeans() {
        return phoneBeans;
    }
}
%>

<%
    EmployeeBean bean = new EmployeeBean();
    bean.setId("1");
    bean.getPhoneBeans().add(new PhoneBean("555", "2368"));
%>
<html>
    <head>
        <title>Inner classes test</title>
    </head>

    <body>
        <% for (PhoneBean phoneBean: bean.getPhoneBeans()) { %>
        <%= phoneBean.getArea() %>-<%= phoneBean.getNumber() %><br/>
        <% } %>
    </table>
</body>
