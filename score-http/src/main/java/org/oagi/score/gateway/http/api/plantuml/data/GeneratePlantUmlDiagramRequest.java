package org.oagi.score.gateway.http.api.plantuml.data;

public class GeneratePlantUmlDiagramRequest {

    private String text;

    private String encodedText;

    private String format = "svg";

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public GeneratePlantUmlDiagramRequest withText(String text) {
        setText(text);
        return this;
    }

    public String getEncodedText() {
        return encodedText;
    }

    public void setEncodedText(String encodedText) {
        this.encodedText = encodedText;
    }

    public GeneratePlantUmlDiagramRequest withEncodedText(String encodedText) {
        setEncodedText(encodedText);
        return this;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public GeneratePlantUmlDiagramRequest withFormat(String format) {
        setFormat(format);
        return this;
    }

    public String getContentType() {
        String contentType;
        switch (getFormat()) {
            case "svg":
                contentType = "image/svg+xml";
                break;
            case "png":
                contentType = "image/png";
                break;
            case "txt":
                contentType = "text/plain";
                break;
            default:
                contentType = "application/octet-stream";
                break;
        }
        return contentType;
    }
}
