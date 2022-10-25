package fr.antoinectx.roomview.models;

import java.util.ArrayList;
import java.util.List;

public class Donnees {
    private final List<Batiment> batiments;

    public Donnees() {
        batiments = new ArrayList<>();
        batiments.add(new Batiment("Batiment A", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam eleifend, metus ac tincidunt ultricies, massa lacus pretium nisl, sed feugiat tortor ante id ante. Ut auctor, mi quis hendrerit porta, est nisl tincidunt odio, nec placerat orci nulla at massa. Donec in felis quis nibh elementum fermentum. In hac habitasse platea dictumst. Ut sed turpis nec neque faucibus suscipit. Donec sit amet erat nec magna efficitur cursus. Aliquam erat volutpat. Phasellus sit amet dui in diam aliquet efficitur. Suspendisse potenti. In non nunc non lectus rutrum aliquet. Suspendisse potenti. Donec quis nisl vel magna euismod venenatis. Nulla facilisi. Aliquam erat volutpat. Maecenas ultricies libero eget tortor elementum, vel tincidunt mauris accumsan. Quisque sit amet erat sed felis tincidunt vehicula."));
        batiments.add(new Batiment("Batiment B", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam eleifend, metus ac tincidunt ultricies, massa lacus pretium nisl, sed feugiat tortor ante id ante. Ut auctor, mi quis hendrerit porta, est nisl tincidunt odio, nec placerat orci nulla at massa. Donec in felis quis nibh elementum fermentum. In hac habitasse platea dictumst. Ut sed turpis nec neque faucibus suscipit. Donec sit amet erat nec magna efficitur cursus. Aliquam erat volutpat. Phasellus sit amet dui in diam aliquet efficitur. Suspendisse potenti. In non nunc non lectus rutrum aliquet. Suspendisse potenti. Donec quis nisl vel magna euismod venenatis. Nulla facilisi. Aliquam erat volutpat. Maecenas ultricies libero eget tortor elementum, vel tincidunt mauris accumsan. Quisque sit amet erat sed felis tincidunt vehicula."));
        batiments.add(new Batiment("Batiment C", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam eleifend, metus ac tincidunt ultricies, massa lacus pretium nisl, sed feugiat tortor ante id ante. Ut auctor, mi quis hendrerit porta, est nisl tincidunt odio, nec placerat orci nulla at massa. Donec in felis quis nibh elementum fermentum. In hac habitasse platea dictumst. Ut sed turpis nec neque faucibus suscipit. Donec sit amet erat nec magna efficitur cursus. Aliquam erat volutpat. Phasellus sit amet dui in diam aliquet efficitur. Suspendisse potenti. In non nunc non lectus rutrum aliquet. Suspendisse potenti. Donec quis nisl vel magna euismod venenatis. Nulla facilisi. Aliquam erat volutpat. Maecenas ultricies libero eget tortor elementum, vel tincidunt mauris accumsan. Quisque sit amet erat sed felis tincidunt vehicula."));
        batiments.add(new Batiment("Batiment D", "slt c'est moi"));
    }

    public List<Batiment> getBatiments() {
        return batiments;
    }
}
