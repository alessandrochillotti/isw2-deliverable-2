package it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning;

import it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning.options.Sampling;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning.options.CostSensitive;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning.options.FeatureSelection;

public class Profile {
	
	private FeatureSelection featureSelection;
	private Sampling sampling;
	private CostSensitive costSensitive;
	
	public Profile(FeatureSelection featureSelection, Sampling sampling, CostSensitive costSensitive) {
		this.featureSelection = featureSelection;
		this.sampling = sampling;
		this.costSensitive = costSensitive;
	}
	
	public FeatureSelection getSelection() {
		return featureSelection;
	}
	
	public Sampling getSampling() {
		return sampling;
	}
	
	public CostSensitive getSensitive() {
		return costSensitive;
	}
}
