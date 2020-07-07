package de.thm.mni.vs.gruppe5.common;

public class TicketPatch {
    private String text;

    public TicketPatch(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "TicketPatch{" +
                "text='" + text + '\'' +
                '}';
    }
}
