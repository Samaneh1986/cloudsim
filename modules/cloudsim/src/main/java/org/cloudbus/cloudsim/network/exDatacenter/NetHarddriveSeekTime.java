package org.cloudbus.cloudsim.network.exDatacenter;

import org.cloudbus.cloudsim.distributions.ContinuousDistribution;

public class NetHarddriveSeekTime implements ContinuousDistribution {
	private double avgNum;

	public NetHarddriveSeekTime(double avgSeekTime) {
		// TODO Auto-generated constructor stub
		this.avgNum = avgSeekTime;
	}

	@Override
	public double sample() {
		// TODO Auto-generated method stub
		double minNum = avgNum * 0.5;
		double maxNum = avgNum * 1.5;
		double rand = Math.random();
		
		return minNum + (rand * (maxNum - minNum));
	}

}
