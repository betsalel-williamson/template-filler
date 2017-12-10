package com.templateFiller;

import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;


/**
 * Copyright (c) 12/10/17 Betsalel Williamson
 */
public class TemplateDocument {

    public enum DocumentType {
        TXT,
        DOCX
    }

    private final SimpleMapProperty docType;
    private final SimpleStringProperty url;

    public TemplateDocument(SimpleMapProperty docType, SimpleStringProperty url) {
        this.docType = docType;
        this.url = url;
    }
}
