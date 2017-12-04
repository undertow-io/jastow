package io.undertow.test.jsp.classref;

import io.undertow.test.jsp.classref.testname.MyTest;

public class Indirect {

    public Indirect() {
        MyTest myTest = new MyTest();
    }
}
