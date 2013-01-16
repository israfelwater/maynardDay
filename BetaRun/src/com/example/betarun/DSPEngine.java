package com.example.betarun;

import java.lang.reflect.Array;

public class DSPEngine {
	private float[][] avgLadder, detLadder, twiddleFactor;
	private float[][] sampleCirBuffer; 
	public int[] noteFactor;
	private final int real = 0, twidIdx = 0;
	private final int imag = 1, jIdx = 1;
	private float[] spectrum, freqSpec, lastLPSample, imagSpectrum, realSpectrum;
	private int[] ladderIndex= {0,0,0,0,0,0,0,0}, partNoteFFTIndex;
	private int bufferSize;
	private int sampleFreq;
	private int[][] twiddleIdx = new int[12*8][2];
	private int[][] phaseCirBuffer;
	
	
	public DSPEngine(int BufferSize,int sampleRate){
		sampleFreq = sampleRate;
		bufferSize = BufferSize;
		detLadder = avgLadder = new float[8][bufferSize*2];
		sampleCirBuffer = new float[bufferSize][8];
		phaseCirBuffer= new int[96][bufferSize];
		realSpectrum = imagSpectrum = new float[96];
		twiddleFactor = calcTwiddleeDees(bufferSize);
		noteFactor = calcNoteBins(bufferSize, sampleRate); // set freqSpec in caclNoteBins
		spectrum = new float[96]; //base on 12 notes per 8 octaves
		partNoteFFTIndex = new int[8];
		lastLPSample = new float[8];
		
	}
	
	public float[] newSamples(float[] samples, int mode) throws Exception{
		
		//float[] sampleHP = new float[samples.length];
		
		//dual sum samples and add to ladder
		float[] sampleLP = LowPass(samples, mode);
		float[] sampleHP = DualSum(Subtract(samples,sampleLP));
		sampleLP = DualSum(sampleLP);
		
		int N = bufferSize>>1;
		for (int i = 0;i<N; i++){
			avgLadder[mode][i+(ladderIndex[mode]*N)] = sampleLP[i];
			detLadder[mode][i+(ladderIndex[mode]*N)] = sampleHP[i];
		}
		ladderIndex[mode]++; 
		if (ladderIndex[mode]>=4) {
			throw new Exception("Overran modeLadderIndexBuffer");
		}
	
		//Check OF
		if (mode==0){ //only once per cycle
			for (int rung = 0;rung<8;rung++){ 
				if (ladderIndex[rung] >= 2) { //Run the first ready buffer					 
					//calc part FFT 1/4 to 1/2 of samples
					// store return in proper place in spectrum
					PartFFT(detLadder[rung], rung);
					
					//use what was run to calculate lower freqs on next rung
					if (rung<=6){
						newSamples(avgLadder[rung], rung+1);
					}
					
					//we ran the ladder so shift it
					for (int j=0;j<bufferSize;j++){
						detLadder[rung][j]=detLadder[rung][j+bufferSize]; //shift cache down
						avgLadder[rung][j]=avgLadder[rung][j+bufferSize]; //shift cache down
					}
					ladderIndex[rung]-=2;
					break; //only once per cycle
				}
			}
		}
	
		//return spectrum
		return spectrum;
	}
	
	public float[] updateSpectrum(float[] samples, int mode) throws Exception{
		//float[] sampleHP = new float[samples.length];

		//dual sum samples and add to ladder
		float[] sampleLP = LowPass(samples, mode);
		PartNoteFFT(Subtract(samples,sampleLP), mode);
		float[] downSample = DualSum(sampleLP);
		
		/*
		int N = bufferSize>>1;
		for (int i = 0;i<N; i++){
			avgLadder[mode][i+(ladderIndex[mode]*N)] = sampleLP[i];
			detLadder[mode][i+(ladderIndex[mode]*N)] = sampleHP[i];
		}
		ladderIndex[mode]++; 
		if (ladderIndex[mode]>=4) {
			throw new Exception("Overran modeLadderIndexBuffer");
		}*/
			
		//Check OF
		if (mode<7){
			updateSpectrum(downSample, mode+1);
		}
		//PartNoteFFT(sampleHP, mode);
		
	
		//return spectrum
		return spectrum;
	}


	float[] Decibels(float[] sample) {
		for (float samples:sample){
			samples = (float) (Math.log10(samples/16.67047))/100;
		}
				
		return sample;
	}

	float[] DualSum(float[] samples){
		int N = samples.length>>1;
		float[] output = new float[N];
		for (int i = 0; i < N; i++){
			output[i] = (float) (samples[2*i] + samples[2*i+1]);
		}
		return output;
	}
	
	float[] DualDiff(float[] samples){
		for (int i = 0; i < samples.length>>1; i++){
			samples[i] = (float) (samples[2*i] - samples[2*i+1]);
		}
		return samples;
	}

	float[] FFT(float[] samples){
		int N = bufferSize;
		float[] spectrum = new float[N];
		int g = N>>1;
				
		//TODO widdle this down
		for (int i = 0; i < N; i++){
			float realSpectrum = 0;
			float imagSpectrum = 0;
			for (int j = 0; j < N; j++){
				//realSpectrum += samples[j] * twiddleFactor[i][j][real];
				//imagSpectrum += samples[j] * twiddleFactor[i][j][imag];
				realSpectrum += samples[j] * twiddleFactor[(int) ((i*j%N)/N)*360][real];
				imagSpectrum += samples[j] * twiddleFactor[(int) ((i*j%N)/N)*360][imag];
			}
			realSpectrum /= N;
			imagSpectrum /= N;
			spectrum[i] = (float) Math.sqrt(realSpectrum*realSpectrum + imagSpectrum*imagSpectrum);
		}
		return spectrum;
	}
	
	/**
	 * Calculate the FFT on samples from the 1/4(+1) mark to
	 * the 1/2 mark, which is a single octave.
	 * @param samples
	 * @return
	 */
	float[] PartFFT(float[] samples, int mode){
		int N = bufferSize, firstIdx = (bufferSize>>2)+1, lastIdx =bufferSize>>1;
		int aNote = noteFactor[firstIdx], num2Norm = 0, specIdx=0;
		
		//set spectrum to zero, TODO addd a persist decay thing
		for (int i = 0; i<12; i++){
			spectrum[(7-mode)*12+i]=0;
		}
		
		//TODO twiddle this down sum more
		for (int i = firstIdx; i <= lastIdx; i++){
			float realSpectrum = 0;
			float imagSpectrum = 0;
			for (int j = 0; j < N; j++){
				//realSpectrum += samples[j] * twiddleFactor[i][j][real];
				//imagSpectrum += samples[j] * twiddleFactor[i][j][imag];
				int temp =(int) ((((float)i*j%N)/(float) N)*360.0);
				realSpectrum += samples[j] * twiddleFactor[temp][real];
				imagSpectrum += samples[j] * twiddleFactor[temp][imag];
			}
			realSpectrum /= N;
			imagSpectrum /= N;
			
			//store in spectrum
			if(aNote!=noteFactor[i]){
				if (num2Norm > 0){
					spectrum[(7-mode)*12+specIdx++]/=num2Norm;
					if (specIdx >= 12) break; //13th step (zero based)
				}
				aNote=noteFactor[i];
				num2Norm = 0;
			}
			spectrum[(7-mode)*12+specIdx]+=(float) Math.sqrt(realSpectrum*realSpectrum + imagSpectrum*imagSpectrum);
			num2Norm++;
		}
		return spectrum;
	}
	
	/**
	 * Calculate the FFT on samples for a single octave
	 * the 1/4(+1) to 1/2 mark, at the 12 note frequencies.
	 * remove what comes out and add what comes in on each pass.
	 * @param samples is a float buffer with the new samples in it
	 * @param mode is the mode running size of samples is dependent on this 
	 * but a larger samples could be supplied and not used.
	 * @return
	 */
	float[] PartNoteFFT(float[] samples, int mode){
		int N = bufferSize, firstIdx = partNoteFFTIndex[mode], lastIdx = firstIdx + (bufferSize>>mode);
		//int aNote = noteFactor[firstIdx], num2Norm = 0, specIdx=0;
		
		//set spectrum to zero, TODO addd a persist decay thing
		/*for (int i = 0; i<12; i++){
			spectrum[(7-mode)*12+i]=0;
		}*/
		
		//TODO twiddle this down sum more
		for (int i = (7-mode)*12; i < (7-mode)*12+12; i++){ //i is note position 0f 96
			//float realSpectrum = 0;
			//float imagSpectrum = 0;
			
			int k = twiddleIdx[i][jIdx]; //k is j but continued around the clock the number of 
			float normNoteFreq = freqSpec[i]*((float) N / (float) (sampleFreq>>(mode)));
			for (int j = firstIdx; j < lastIdx; j++){ //j is sample position of bufferSize>>mode
				//realSpectrum += samples[j] * twiddleFactor[i][j][real];
				//imagSpectrum += samples[j] * twiddleFactor[i][j][imag];
				twiddleIdx[i][twidIdx] =(int) ((((normNoteFreq*(float)k++)%N)/(float) N)*360.0);
				if (twiddleIdx[i][twidIdx] == 0) {
					twiddleIdx[i][jIdx] = 0; //when returned to zero start over
				} else {
					twiddleIdx[i][jIdx]++;
				}
				realSpectrum[i] -= sampleCirBuffer[j%N][mode] * twiddleFactor[phaseCirBuffer[i][j%N]][real]; //out with the old
				imagSpectrum[i] -= sampleCirBuffer[j%N][mode] * twiddleFactor[phaseCirBuffer[i][j%N]][imag];
				sampleCirBuffer[j%N][mode] = samples[j-firstIdx];
				phaseCirBuffer[i][j%N] = twiddleIdx[i][twidIdx];
				realSpectrum[i] += sampleCirBuffer[j%N][mode] * twiddleFactor[phaseCirBuffer[i][j%N]][real]; //in with the new
				imagSpectrum[i] += sampleCirBuffer[j%N][mode] * twiddleFactor[phaseCirBuffer[i][j%N]][imag];
			}
			
			float rSpec = realSpectrum[i] / N;
			float iSpec = imagSpectrum[i] / N;
			
			spectrum[i] = (float) Math.sqrt(rSpec*rSpec + iSpec*iSpec);
		}
		partNoteFFTIndex[mode] = (lastIdx)%N; //index passed last Idx 
		return spectrum;
	}
	
	float[][] calcTwiddleeDees(int N){
		float[][] twiddleeDee = new float[360][2];
		//TODO widdle this down
		/*for (int i = 0; i < N; i++){
			for (int j = 0; j < N; j++){
				twiddleeDee[i][j][real] = (float) Math.cos(-2*Math.PI*i*j/N);
				twiddleeDee[i][j][imag] = (float) Math.sin(-2*Math.PI*i*j/N);
				//if (imag > real){
				//	twiddleeDee[i][j] = (float) -Math.sqrt(imag-real);
				//} else {
				//	twiddleeDee[i][j] = (float) Math.sqrt(real-imag);
				//}
			}
		}*/
		//new idea just do 360 calcs and use those rounded(remainder(i*j/N)*360)
		for (int i = 0; i < 360; i++){
			twiddleeDee[i][real] = (float) Math.cos(-2.0*Math.PI*(i/360.0));
			twiddleeDee[i][imag] = (float) Math.sin(-2.0*Math.PI*(i/360.0));
		}
		return twiddleeDee;
	}
	
	/**
	 * Calculate which samples go to which note.
	 * enumerate 1 through 12 Gb to F, 
	 * samples bufferSize/4+1 to bufferSize/2 
	 * 
	 * @param bufferSize
	 * @return
	 */
	private int[] calcNoteBins(int bufferSize, float sampleFreq) {
		// start at 440
		float aNote = 440;
		//create bins
		float lowNote = (float) (((bufferSize/4.0)+1.0)*(sampleFreq/bufferSize));
		float highNote = (float) ((bufferSize/2.0)*(sampleFreq/bufferSize));
		while (!(aNote<=highNote & aNote>=lowNote)){
			if (aNote<=lowNote){
				aNote*=2;
			}else if (aNote>=highNote){
				aNote/=2;
			}
		}
		aNote = (float) (aNote * Math.pow(2,-1/24));//shift to base of A
		int chromaticNumber = 0; //number associated with note starting A = 0, Ab=11
		//now aNote is in the right octave so scale down then up
		while (aNote>=lowNote){
			if (chromaticNumber <= 0) {chromaticNumber = 12;}
			chromaticNumber--;
			double temp = Math.pow(2,-1.0/12.0);
			aNote = (float) (aNote * temp);
		}
		
		freqSpec = new float[96];
		freqSpec[0] = (float) (aNote * Math.pow(2,1/24)); //shift up to middle of A
		freqSpec[0] /= (float) Math.pow(2.0,7.0); //shifted down 7 octaves
		//aNote is now the lowest note, so save frequency and scroll up
		for (int i = 1; i < 96; i++){
			freqSpec[i] = (float) (freqSpec[i-1] * Math.pow(2.0,1.0/12.0));
		}
		
		
		int[] noteBins = new int[bufferSize];
		for (int i = (bufferSize/4)+1; i<=bufferSize>>1; i++){
			if (i*(sampleFreq/bufferSize)>=aNote){
				aNote*=(float) Math.pow(2,1/12); //increment up
				chromaticNumber++;
				if (chromaticNumber >= 12) {chromaticNumber = 0;}
				
			}
			noteBins[i]=chromaticNumber;
		}
		
		return noteBins;
	}

	float[] LowPass(float[] samples, int mode){
		float[] output = new float[samples.length];
		
		double a0 = 1 + (4.0/Math.PI);
		double a1 = 1 - (4.0/Math.PI);
		double b0 = 1;
		double b1 = 1;
		
		//normallize
		a1 /= a0;
		b0 /= a0;
		b1 /= a0;
		
		
		//zero pad
		output[0] = (float)  (b0*samples[0]+b1*lastLPSample[mode]-a1*lastLPSample[mode]);
		for (int i=1; i<samples.length; i++){
			output[i] = (float) (b0*samples[i]+b1*samples[i-1]-a1*output[i-1]);
		}
		lastLPSample[mode] = output[samples.length-1];
		return output;
	}
	
	private float[] Subtract(float[] LHArray, float[] RHArray) {
		int N = Math.min(LHArray.length,RHArray.length);
		float[] output  = new float[N];
		
		for (int i=0; i<N;i++){
			output[i] = LHArray[i] - RHArray[i];
		}
		return output;
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
