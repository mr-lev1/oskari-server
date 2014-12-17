package fi.nls.oskari.fe.xml.util;

import javax.xml.bind.annotation.XmlAttribute;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class NillableType<T> extends Reference {

    public NillableType() {

    }
    

    @XmlAttribute(name = "nilReason", required = false)
    public String nilReason;

    @XmlAttribute(required = false, name = "nil", namespace = "http://www.w3.org/2001/XMLSchema-instance")
    public boolean nil;

    @JacksonXmlText(value = false)
    public String value;

    public NillableType(String value) {
        this.value = value;
    }
    

}