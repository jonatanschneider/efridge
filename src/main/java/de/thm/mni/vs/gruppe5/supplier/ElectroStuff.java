package de.thm.mni.vs.gruppe5.supplier;

import de.thm.mni.vs.gruppe5.common.Config;

import java.util.Arrays;
import java.util.List;

public class ElectroStuff extends Supplier {

    // normally we would get the parts from our database but for the purpose
    // of this project and simplicity we just use these static ids
    private List<String> availableParts = Arrays.asList(
            "4028808972ec265e0172ec2661700016",
            "4028808972ec265e0172ec26614a0006",
            "4028808972ec265e0172ec266159000c",
            "4028808972ec265e0172ec2661650012",
            "4028808972ec265e0172ec2661700014",
            "4028808972ec265e0172ec2661710018",
            "4028808972ec265e0172ec26612b0000"
    );

    public static void main(String[] args) {
        new ElectroStuff();
    }

    public ElectroStuff() {
        super(Config.ELECTRO_STUFF_SERVER_PORT);
    }

    @Override
    boolean isAvailable(String partId) {
        return availableParts.contains(partId);
    }
}
