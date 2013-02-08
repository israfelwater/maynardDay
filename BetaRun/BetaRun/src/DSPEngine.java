
public class DSPEngine {
	private short[][] ladderCache; 
	private int[] ladderIndex= {0,0,0,0,0,0,0,0};
	private int bufferSize;
	
	public DSPEngine(int BufferSize){
		bufferSize = BufferSize;
		ladderCache = new short[8][bufferSize*2];
		
	}
	
	public short[] newSamples(short[] samples, int mode){
		short[] averages = null;
		short[] sampleHP = new short[samples.length];
				
		short[] sampleLP = LowPass(samples);
				
		for (int i = 0; i<samples.length; i++){
			sampleHP[i] = (short) (samples[i] - sampleLP[i]);
		}
		
		
		sampleHP = HalfFFT(sampleHP);
		sampleLP = DualSum(sampleLP);
		
		for (int i = 0;i<bufferSize>>1; i++){
			ladderCache[mode][i+ladderIndex[mode]] = sampleLP[i];
		}
		ladderIndex[mode]++; 
		if (ladderIndex[mode]>=4) ladderIndex[mode]=0;
		
		if (mode == 0) { //only run twice per iteration
			for (int i = 0;i<8;i++){ 
				 if (ladderIndex[mode] % 2 != 0) { //Run the first ready buffer
					 mode = i;
					 averages = newSamples(ladderCache[i], mode);
					 break;
				 }
			}
		}
	
		//return new Modal(sampleHP, mode, averages.averages);
		return sampleHP;
	}
	
	short[] DualSum(short[] samples){
		for (int i = 0; i <= samples.length>>1; i++){
			samples[i] = (short) (samples[2*i] + samples[2*i+1]);
		}
		return samples;
	}

	short[] HalfFFT(short[] samples){
		short[] spectrum = new short[samples.length];
		int N = samples.length;
		int g = N>>1;
				
		//TODO widdle this down
		for (int i = 0; i < g; i++){
			for (int j = 0; j < samples.length; j++){
				spectrum[i] += samples[j] * Math.sqrt(
						Math.pow(Math.cos(-2*Math.PI*i*j/N),2)+ 
						Math.pow(Math.sin(-2*Math.PI*i*j/N),2));
			}
		}
		return spectrum;
	}
	
	short[] LowPass(short[] samples){
		//QMFB Qudrature Mirror Filter
		// values:    an array of numbers that will be modified in place
		// smoothing: the strength of the smoothing filter; 1=no change, larger values smoothes more
		int smoothing = 2;
		//short sample = samples[0]; // start with the first input
		for (int i=1; i<samples.length-1; i++){
			//int currentSample = samples[i];
		    //sample += (currentSample - sample) / smoothing;
		    //samples[i] = sample;
			samples[i] += samples[i+1]/smoothing;
		}
		return samples;
	}
}




class Modal {
	int mode;
	short[] details;
	short[] averages;
	
	public Modal(short[] theDetails, int theMode, short[] theAverages){
		mode = theMode;
		details = theDetails;
		averages = theAverages;
		
	}
		
}
