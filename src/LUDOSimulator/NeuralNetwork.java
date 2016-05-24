package LUDOSimulator;

import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

public class NeuralNetwork
{
	static String fileName = "weigths.dat";
	private int inputsnumber;
	private int layersnumber;
	Neuron[] network[]; 
	public ArrayList<Double> LastErrors = new ArrayList<Double>();

	public NeuralNetwork(int neuronsPerLayer[], int inputsnr)
	{
		//Validate the input data
		if (neuronsPerLayer.length < 2)
			throw new InvalidParameterException("Invalid number of layers");

		for (int i = 0; i < neuronsPerLayer.length; i++)
			if (neuronsPerLayer[i] < 1)
				throw new InvalidParameterException("Invalid number of neurons per layer " + i);

		//Create network
		inputsnumber = inputsnr;
		layersnumber = neuronsPerLayer.length;
		network = new Neuron[layersnumber][];

		for (int i = 0; i < layersnumber; i++){
			network[i] = new Neuron[neuronsPerLayer[i]];

			if (i == 0){
				//First hidden layer
				for (int j = 0; j < network[i].length; j++){
					network[i][j] = new Neuron(inputsnumber);
				}
			}
			else{
				//Other layers
				for (int j = 0; j < network[i].length; j++){
					network[i][j] = new Neuron(network[i - 1].length);
				}
			}
		}
	}

	public void saveWeights(){
		//Store the output data
		try {
			FileWriter fileWeights =new FileWriter(fileName);			
			for(int i=0; i<layersnumber; i++){
				for (int j = 0; j < network[i].length; j++){
					double outWeights[]=network[i][j].outputWeigths();
					for(int z=0; z<outWeights.length; z++){
						fileWeights.write(outWeights[z]+"\t");
					}
					fileWeights.write("\n");
				}
			}
			fileWeights.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public double[] activate(double inputs[])
	{
		for (int i = 0; i < layersnumber; i++){
			if (i == 0){
				//If it is the first hidden layer activate all neurons with inputs to the network
				for (int j = 0; j < network[i].length; j++){
					network[i][j].activate(inputs);
				}
			}
			else{
				//For all other layers activate all neurons with the neurons on the previous layer	 
				double previousLayerOutputs[] = new double[network[i - 1].length];
				for (int k = 0; k < previousLayerOutputs.length - 1; k++){
					previousLayerOutputs[k] = network[i - 1][k].output;
				}
				for (int j = 0; j < network[i].length; j++){
					network[i][j].activate(previousLayerOutputs);
				}
			}
		}
		double[] outputs = new double[network[layersnumber - 1].length];
		for (int j = 0; j < network[layersnumber - 1].length; j++){
			outputs[j] = network[layersnumber - 1][j].output;
		}
		return outputs;
	}

	public void backpropagate(double inputs[], double desiredoutputs[])
	{
		for (int i = layersnumber - 1; i >= 0; i--){
			if (i == layersnumber - 1){
				//Output layer
				double previousLayerOutputs[] = new double[network[i - 1].length];
				for (int k = 0; k < previousLayerOutputs.length - 1; k++){
					previousLayerOutputs[k] = network[i - 1][k].output;
				}
				for (int j = 0; j < network[i].length; j++){
					network[i][j].correctweights(previousLayerOutputs, desiredoutputs[j]);
				}
			}

			if (i == 0){
				//Input layer
				for (int j = 0; j < network[i].length; j++){
					network[i][j].correctweights(inputs, network[i + 1], j);
				}
			}

			if (i != 0 && i != layersnumber - 1){
				//Hidden layers 
				double previousLayerOutputs[] = new double[network[i - 1].length];
				for (int k = 0; k < previousLayerOutputs.length - 1; k++){
					previousLayerOutputs[k] = network[i - 1][k].output;
				}

				for (int j = 0; j < network[i].length; j++){
					network[i][j].correctweights(previousLayerOutputs, network[i+1], j);
				}
			}
		}
	}
}
