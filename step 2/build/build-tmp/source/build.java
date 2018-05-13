import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.*; 
import ddf.minim.analysis.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class build extends PApplet {

int bgC       = 0xff2F2F2F;
String dataPATH = "../../data/";

// ================================================================




// ================================================================

Minim minim;
AudioInput audio;
FFT audioFFT;

// ================================================================

int audioRange  = 256;
int audioMax = 100;

float audioAmp = 40.0f;
float audioIndex = 0.05f;
float audioIndexAmp = audioIndex;
float audioIndexStep = 0.05f;

int rectS       = 4;

// ================================================================

int stageM      = 100;
int stageW      = (audioRange * rectS) + (stageM * 2);
int stageH      = 700;

// ================================================================



int xStart      = stageM;
int yStart      = stageM;
int xSpace      = rectS;

// ================================================================

public void settings(){ 
  size(stageW, stageH);
}

// ================================================================

public void setup() {
  background(bgC);

  minim = new Minim(this);
  audio = minim.getLineIn(Minim.STEREO);

  audioFFT = new FFT(audio.bufferSize(), audio.sampleRate());
  audioFFT.linAverages(audioRange);

  // audioFFT.window(FFT.NONE);
  // audioFFT.window(FFT.GAUSS);
} 

// ================================================================
public void draw() {
  // background(bgC);

  // stroke(#0066FF); noFill();
  // line(0, stageM, width, stageM);
  // line(stageM, 0, stageM, height);
  // line(width - stageM, 0, width - stageM, height);

  audioFFT.forward(audio.mix);

  for (int i = 0; i < audioRange; ++i) {

    float indexAvg = (audioFFT.getAvg(i) * audioAmp) * audioIndexAmp;
    float indexCon = constrain(indexAvg, 0, audioMax);

    stroke(0); 
    fill(255, 25);

    rect(xStart + (i * xSpace), yStart, rectS, indexAvg);

    audioIndexAmp += audioIndexStep;      
  }

  audioIndexAmp = audioIndex;

  stroke(0xffDD6600); noFill();
  line(0, stageM + 100, width, stageM + 100);
}

// ================================================================

public void stop() {
  audio.close();
  minim.stop();
  super.stop();
}

// ================================================================

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "build" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
