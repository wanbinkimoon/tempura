color bgC       = #2F2F2F;
String dataPATH = "../../data/";

// ================================================================

int     renderNum  = 0;
String  renderPATH = "../render/";

// ================================================================

import ddf.minim.*;
import ddf.minim.analysis.*;

// ================================================================

Minim minim;
AudioInput audio;
FFT audioFFT;

// ================================================================

int audioRange  = 12;
int audioMax = 100;

float audioAmp = 125.0;
float audioIndex = 0.05;
float audioIndexAmp = audioIndex;
float audioIndexStep = 0.75;

String settingsStr = "amplifier: " + audioAmp + " // index start: " + audioIndex +  " // index step: " + audioIndexStep;

int rectS       = 30;

// ================================================================

int stageM      = 100;
int stageW      = (audioRange * rectS) + (stageM * 2);
int stageH      = 700;

// ================================================================



int xStart       = stageM;
int yStart       = stageM;
int xSpace       = rectS;

int valuePadding = 4;

// ================================================================

void settings(){ 
  size(stageW, stageH);
}

// ================================================================

void setup() {
  background(bgC);

  minim = new Minim(this);
  audio = minim.getLineIn(Minim.STEREO);

  audioFFT = new FFT(audio.bufferSize(), audio.sampleRate());
  audioFFT.linAverages(audioRange);

  // audioFFT.window(FFT.NONE);
  audioFFT.window(FFT.GAUSS);
} 

// ================================================================
void draw() {
  background(bgC);

  audioFFT.forward(audio.mix);

  for (int i = 0; i < audioRange; ++i) {

    float indexAvg = (audioFFT.getAvg(i) * audioAmp) * audioIndexAmp;
    float indexCon = constrain(indexAvg, 0, audioMax);

    // Render bars
    noStroke();
    fill(255, 35);
    
    rect(xStart + (i * xSpace) + (valuePadding / 2), yStart + audioMax, rectS - valuePadding, - indexCon );
    // rect(xStart + (i * xSpace) + (valuePadding / 2), yStart, rectS - valuePadding, indexAvg );

    // Print values
    fill(20); noStroke();
    rect(xStart + (i * xSpace) + (valuePadding / 2), stageM + audioMax + valuePadding + (valuePadding / 2), rectS - valuePadding, rectS - valuePadding );

    fill(#00aeFF);
    textAlign(CENTER);
    text(str((int)indexAvg), xStart + (i * xSpace) + (rectS / 2), stageM + audioMax + valuePadding + (rectS  - (rectS / 3)));

    audioIndexAmp += audioIndexStep;      
  }

  audioIndexAmp = audioIndex;
  
  noFill();
  stroke(#BB6600); 
  line(stageM, stageM, width - stageM, stageM);
  stroke(#DD6600); 
  line(stageM, stageM + 100, width - stageM, stageM + 100);

  // Print values
  fill(20); noStroke();
  rect(stageM, stageM - (rectS + valuePadding),(audioRange * rectS), rectS);

  fill(#00AE55);
  textAlign(LEFT);
  text(settingsStr, stageM + (rectS / 4), stageM - (rectS + valuePadding) + (rectS  - (rectS / 3)));


}

// ================================================================

void stop() {
  audio.close();
  minim.stop();
  super.stop();


}

// ================================================================

