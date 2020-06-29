package de.thm.mni.vs.gruppe5.supplier;

import de.thm.mni.vs.gruppe5.common.Config;

import java.util.Arrays;
import java.util.List;

public class CoolMechanics extends Supplier {

    // normally we would get the parts from our database but for the purpose
    // of this project and simplicity we just use these static ids
    private List<String> availableParts = Arrays.asList(
            "4028808972ec265e0172ec2661650010",
            "4028808972ec265e0172ec26612b0002",
            "4028808972ec265e0172ec26614b000a",
            "4028808972ec265e0172ec26612b0004",
            "4028808972ec265e0172ec266171001a",
            "4028808972ec265e0172ec26614a0008",
            "4028808972ec265e0172ec266159000e"
    );

    public static void main(String[] args) {
        new CoolMechanics();
    }

    public CoolMechanics() {
        super(Config.COOL_MECHANICS_SERVER_PORT);
    }

    @Override
    boolean isAvailable(String partId) {
        return availableParts.contains(partId);
    }
}
