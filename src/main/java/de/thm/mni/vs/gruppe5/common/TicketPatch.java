package de.thm.mni.vs.gruppe5.common;

/**
 * Partial representation of support tickets used to update a ticket via the api
 */
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
