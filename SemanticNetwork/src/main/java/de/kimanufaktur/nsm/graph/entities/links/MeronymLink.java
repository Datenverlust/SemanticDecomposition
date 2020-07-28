package de.kimanufaktur.nsm.graph.entities.links;

import de.kimanufaktur.nsm.decomposition.graph.spreadingActivation.MarkerPassing.MarkerPassingConfig;

public class MeronymLink extends WeightedLink {
  @Override
  public double getWeight() {
    return MarkerPassingConfig.getMeronymLinkWeight();
  }
}
