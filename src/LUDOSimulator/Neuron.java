package LUDOSimulator;

import java.math.*;
import java.security.InvalidParameterException;
import java.util.Random;

import javax.swing.text.Position;

public class Neuron
{
	int connectionsnr;
	int connectionsANDbias;
	public double bias= -1;
	public double momentum = 0.9;
	public double weights[];
	public double OldWUpdates[];
	public double output;
	public double DE_Ds;
	double learningrate = 0.7;

	public Neuron(int connections)
	{
		connectionsnr = connections;
		connectionsANDbias= connectionsnr+1;
		weights = new double[connectionsANDbias];
		OldWUpdates = new double [connectionsANDbias];
		
		// randomly initialize weights including bias
		Random rgenerator = new Random();
		for (int i = 0; i < connectionsnr; i++){
			weights[i] = rgenerator.nextDouble();
			OldWUpdates[i] = 0;
		}
		//One more weight for the bias
		weights[connectionsnr]= rgenerator.nextDouble();
		OldWUpdates[connectionsnr] = 0;
	}

	public double[] outputWeigths(){
		return weights;
	}
	
	// activate the neuron based on input values
	public void activate(double inputs[])
	{
		if (inputs.length != connectionsnr)
			throw new InvalidParameterException("Too many inputs");
		else{
			double sum = 0;
			for (int i = 0; i < inputs.length; i++){
				sum += inputs[i] * weights[i];
			}
			sum += bias*weights[connectionsnr];
			output = bipolarSigmoid(sum);
		}
	}

	private double bipolarSigmoid(double a)
	{
		return ((2.0 / (1.0 + Math.exp(-1.0 * a)))-1);
	}
	
	private double bipolarSigmoidDerivate(double a)
	{
		return 0.5*((1+bipolarSigmoid(a))*(1-bipolarSigmoid(a)));
	}

	//Correct weights on the output layer
	public void correctweights(double inputs[], double desiredoutput)
	{
		//Calculate DE/Ds
		DE_Ds = - 1 * bipolarSigmoidDerivate(output) * (desiredoutput - output);

		//For each connection to the neurons on the previous layer make the corrections
		for (int i = 0; i < connectionsnr; i++){
			weights[i] = weights[i] - learningrate * DE_Ds * inputs[i] + momentum * OldWUpdates[i];
			OldWUpdates [i]= - learningrate * DE_Ds * inputs[i];
		}
		//Correct the bias term
		weights[connectionsnr] = weights[connectionsnr] - learningrate * DE_Ds * (bias) + momentum * OldWUpdates[connectionsnr];
		OldWUpdates[connectionsnr] = - learningrate * DE_Ds * (bias);
	}
	
	
	//Correct the weights of a hidden layer
	public void correctweights(double inputs[], Neuron nextLayer[], int position)
	{
		double sum = 0;

		for (int k = 0; k < nextLayer.length; k++){
			sum = sum + nextLayer[k].DE_Ds * nextLayer[k].weights[position];
		}

		DE_Ds = -1 * bipolarSigmoidDerivate(output) * sum;

		for (int i = 0; i < connectionsnr; i++){
			weights[i] = weights[i] - learningrate * DE_Ds * inputs[i] + momentum * OldWUpdates[i];
			OldWUpdates [i]= - learningrate * DE_Ds * inputs[i];
		}
		//Correct the bias term
		weights[connectionsnr] = weights[connectionsnr] - learningrate * DE_Ds * (bias) + momentum * OldWUpdates[connectionsnr];
		OldWUpdates[connectionsnr] = - learningrate * DE_Ds * (bias);
	}
}
