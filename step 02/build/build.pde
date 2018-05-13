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
float audioIndex = 0.005;
float audioIndexAmp = audioIndex;
float audioIndexStep = 0.75;

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

    stroke(0); 
    fill(255, 15);

    rect(xStart + (i * xSpace), yStart, rectS, indexCon);

    // Print values
    fill(20); noStroke();
    rect(xStart + (i * xSpace) + (valuePadding / 2), stageM + audioMax + (stageM / 2) + (valuePadding / 2), rectS - valuePadding, rectS - valuePadding );

    fill(#00aeFF);
    textAlign(CENTER);
    text(str((int)indexAvg), xStart + (i * xSpace) + (rectS / 2), stageM + audioMax + (stageM / 2) + (rectS  - (rectS / 3)));

    audioIndexAmp += audioIndexStep;      
  }

  audioIndexAmp = audioIndex;

  stroke(#DD6600); noFill();
  line(stageM, stageM + 100, width - stageM, stageM + 100);


}

// ================================================================

void stop() {
  audio.close();
  minim.stop();
  super.stop();


}

// ================================================================

